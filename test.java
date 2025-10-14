Here’s a minimal, production-ready Java example that signs a CSR using a root CA private key stored in an nCipher (nShield) HSM. It uses the nCipher JCE provider (preferred) and Bouncy Castle to parse the CSR and build the X.509 certificate.

⸻

1) pom.xml (Bouncy Castle)

<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>example</groupId>
  <artifactId>csr-signer</artifactId>
  <version>1.0.0</version>
  <properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
  </properties>
  <dependencies>
    <!-- Bouncy Castle for CSR parsing and cert building -->
    <dependency>
      <groupId>org.bouncycastle</groupId>
      <artifactId>bcpkix-jdk18on</artifactId>
      <version>1.78.1</version>
    </dependency>
    <dependency>
      <groupId>org.bouncycastle</groupId>
      <artifactId>bcprov-jdk18on</artifactId>
      <version>1.78.1</version>
    </dependency>
    <!-- (Optional) Logging -->
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-simple</artifactId>
      <version>2.0.13</version>
    </dependency>
  </dependencies>
</project>

You do not add the nCipher JARs to Maven; they come with the HSM client and are installed on the host (e.g., com.ncipher.provider.km.nCipherKM). Make sure they’re on the runtime classpath.

⸻

2) Java signer (single file)

package example;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

public class CsrSigner {

    // ---- CONFIGURE THESE ----
    private static final String HSM_PROVIDER_NAME = "nCipherKM"; // nCipher JCE provider
    private static final String HSM_KEYSTORE_TYPE = "nCipherKM"; // keystore type for nCipher JCE
    private static final String ROOT_CA_ALIAS      = "RootCA-Key-Alias"; // alias of Root CA private key/cert in HSM
    private static final char[] KEY_PASSWORD       = null; // usually null with nCipher; auth is via OCS/softcard/ACLs
    private static final String SIG_ALG            = "SHA256withRSA"; // or "SHA384withECDSA" depending on your key
    private static final int VALIDITY_DAYS         = 825; // example: ~27 months
    // --------------------------

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java CsrSigner <csr.pem> [output-cert.pem]");
            System.exit(1);
        }
        String csrPath = args[0];
        String outPath = (args.length > 1) ? args[1] : "issued-cert.pem";

        // 1) Add providers: Bouncy Castle (helpful), nCipher JCE
        ensureProviders();

        // 2) Load HSM keystore & fetch Root CA private key + cert (issuer)
        KeyStore ks = KeyStore.getInstance(HSM_KEYSTORE_TYPE);
        ks.load(null, null); // nCipherKM keystore doesn't need stream; auth handled by HSM client/policy

        PrivateKey issuerKey = (PrivateKey) ks.getKey(ROOT_CA_ALIAS, KEY_PASSWORD);
        if (issuerKey == null) {
            throw new IllegalStateException("Root CA private key not found for alias: " + ROOT_CA_ALIAS);
        }
        Certificate issuerCert = ks.getCertificate(ROOT_CA_ALIAS);
        if (issuerCert == null) {
            throw new IllegalStateException("Root CA certificate not found for alias: " + ROOT_CA_ALIAS);
        }
        X509Certificate issuerX509 = (X509Certificate) issuerCert;

        // 3) Parse CSR
        PKCS10CertificationRequest csr = readCsrPem(csrPath);
        X500Name subject = csr.getSubject();
        SubjectPublicKeyInfo subjectPubKeyInfo = csr.getSubjectPublicKeyInfo();

        // 4) Build certificate
        Instant now = Instant.now();
        Date notBefore = Date.from(now.minus(1, ChronoUnit.MINUTES));
        Date notAfter  = Date.from(now.plus(VALIDITY_DAYS, ChronoUnit.DAYS));
        BigInteger serial = randomPositiveSerial();

        X500Name issuer = new X500Name(issuerX509.getSubjectX500Principal().getName());

        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
                issuer, serial, notBefore, notAfter, subject, subjectPubKeyInfo);

        // 5) Copy extensionRequest from CSR (if present)
        Extensions requested = getRequestedExtensions(csr);
        if (requested != null) {
            var oids = requested.getExtensionOIDs();
            for (var oid : oids) {
                var ext = requested.getExtension(oid);
                certBuilder.addExtension(oid, ext.isCritical(), ext.getParsedValue());
            }
        }

        // 6) Add basic sensible defaults if CSR didn’t include them
        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        // Subject Key Identifier
        certBuilder.addExtension(
                org.bouncycastle.asn1.x509.Extension.subjectKeyIdentifier,
                false,
                extUtils.createSubjectKeyIdentifier(subjectPubKeyInfo));
        // Authority Key Identifier
        certBuilder.addExtension(
                org.bouncycastle.asn1.x509.Extension.authorityKeyIdentifier,
                false,
                extUtils.createAuthorityKeyIdentifier(SubjectPublicKeyInfo.getInstance(issuerX509.getPublicKey().getEncoded())));

        // IMPORTANT: If this is a true Root CA, you usually should NOT issue end-entity certs directly.
        // Prefer an INTERMEDIATE CA. If you *must*, ensure BasicConstraints CA:false for end-entity,
        // and CA:true for intermediates. If you want to force end-entity:
        // certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));

        // 7) Create ContentSigner bound to HSM private key (signature happens in HSM)
        ContentSigner signer = new JcaContentSignerBuilder(SIG_ALG)
                .setProvider(HSM_PROVIDER_NAME)
                .build(issuerKey);

        // 8) Sign and convert
        X509CertificateHolder holder = certBuilder.build(signer);
        X509Certificate issued = new JcaX509CertificateConverter()
                .setProvider((Provider) Security.getProvider("BC"))
                .getCertificate(holder);

        // 9) (Optional) Validate signature chain locally
        issued.verify(issuerX509.getPublicKey());

        // 10) Write PEM
        writePemCert(issued, outPath);
        System.out.println("Issued certificate written to: " + outPath);
    }

    private static void ensureProviders() throws Exception {
        // Add BC if not present
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
        // Add nCipher JCE provider (preferred route)
        if (Security.getProvider(HSM_PROVIDER_NAME) == null) {
            // Requires nCipher JCE on classpath (e.g., com.ncipher.provider.km.nCipherKM)
            Security.addProvider((Provider)Class.forName("com.ncipher.provider.km.nCipherKM").getDeclaredConstructor().newInstance());
        }

        // --- Alternative via SunPKCS11 (only if you can’t use nCipherKM) ---
        // String pkcs11Config = """
        //   name = nfast
        //   library = /opt/nfast/toolkits/pkcs11/libcknfast.so
        //   slotListIndex = 0
        // """;
        // Provider p11 = Security.getProvider("SunPKCS11").configure(pkcs11Config);
        // Security.addProvider(p11);
        // Then use KeyStore.getInstance("PKCS11", p11) and .getKey(alias, pin)
    }

    private static PKCS10CertificationRequest readCsrPem(String path) throws IOException {
        try (Reader r = new BufferedReader(new FileReader(path));
             PEMParser parser = new PEMParser(r)) {
            Object obj = parser.readObject();
            if (obj instanceof PKCS10CertificationRequest csr) {
                return csr;
            }
            throw new IllegalArgumentException("Not a PKCS#10 CSR: " + path);
        }
    }

    private static Extensions getRequestedExtensions(PKCS10CertificationRequest csr) {
        for (Attribute attr : csr.getAttributes()) {
            if (attr.getAttrType().equals(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
                return Extensions.getInstance(attr.getAttrValues().getObjectAt(0));
            }
        }
        return null;
    }

    private static BigInteger randomPositiveSerial() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        bytes[0] &= 0x7F; // positive
        return new BigInteger(bytes);
    }

    private static void writePemCert(X509Certificate cert, String path) throws Exception {
        String base64 = Base64.getMimeEncoder(64, "\n".getBytes())
                              .encodeToString(cert.getEncoded());
        try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
            out.println("-----BEGIN CERTIFICATE-----");
            out.println(base64);
            out.println("-----END CERTIFICATE-----");
        }
    }
}


⸻

How to use
	1.	Ensure the nShield/nCipher client is installed and initialized on the host:

	•	Appropriate OCS/softcard login performed (or Security World policies allow key use).
	•	The Root CA keypair + certificate is present in the nCipher keystore under alias RootCA-Key-Alias.

	2.	Put your CSR as csr.pem (PEM/PKCS#10).
	3.	Run:

mvn -q -DskipTests package
java -cp target/csr-signer-1.0.0.jar:<path-to-ncipher-jars> example.CsrSigner csr.pem issued-cert.pem

Replace <path-to-ncipher-jars> with your nCipher JCE JAR(s) location (often provided by the HSM client install). If your environment exposes the provider via the system extension directory or boot classpath, you can omit.

⸻

Notes & best practices
	•	Prefer issuing via an Intermediate CA (keep the Root offline/HSM-segregated; sign only intermediates). If you must issue end-entity from root, ensure BasicConstraints is CA:false for the leaf.
	•	The code copies requested extensions from the CSR (subjectAltName, keyUsage, etc.). You can override or enforce policy by adding/removing extensions before signing.
	•	SIG_ALG must match your key type:
	•	RSA: SHA256withRSA (or PSS variants if your ecosystem supports).
	•	EC: SHA256withECDSA (or 384/521 as appropriate).
	•	If you can’t use nCipherKM, the SunPKCS11 path (commented) works with libcknfast.so; you’ll then use a PKCS11 keystore and supply a PIN if required by your setup.

⸻

If you share:
	•	your key type (RSA/ECC),
	•	desired extensions/policies, and
	•	whether you need a full chain (PEM bundle),

I’ll tailor this to your exact environment (including SANs, KU/EKU, CRL/OCSP AIA, pathLen for intermediates, and a quick test harness).