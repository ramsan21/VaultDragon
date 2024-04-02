import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

public class CertificateGeneration {
    public void generateCertificate(String identity, String sigAlg, String noOfYears, String cirPasswd, PGPConfig config) {
        try {
            Security.addProvider(new BouncyCastleProvider());

            // KeyPair generation
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(config.getKeyAlgo(), config.getProviderName());
            keyGenerator.initialize(config.getKeyLength(), new SecureRandom());
            KeyPair keyPair = keyGenerator.generateKeyPair();

            // Certificate valid from now to noOfYears
            Date notBefore = new Date();
            Date notAfter = getExpiryDate(notBefore, noOfYears);

            // Certificate Issuer and Subject
            X500Name issuerAndSubjectName = new X500Name(String.format("C=SG, O=SCB, CN=%s, OU=SCB", identity));

            // Certificate Serial Number
            BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());

            // Certificate Builder
            JcaX509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                    issuerAndSubjectName,
                    serialNumber,
                    notBefore,
                    notAfter,
                    issuerAndSubjectName,
                    keyPair.getPublic());

            // Content Signer
            ContentSigner contentSigner = new JcaContentSignerBuilder(sigAlg).build(keyPair.getPrivate());

            // Certificate
            X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(certificateBuilder.build(contentSigner));

            // Keystore handling
            KeyStore keystore = loadOrCreateKeystore(config.getBaseKeyPath(), cirPasswd, config);
            keystore.setKeyEntry(identity, keyPair.getPrivate(), cirPasswd.toCharArray(), new X509Certificate[]{certificate});
            try (OutputStream out = new FileOutputStream(config.getBaseKeyPath())) {
                keystore.store(out, cirPasswd.toCharArray());
            }

            // PGPSignatureSubpacketGenerator setup omitted for brevity
            // Export public key, import, etc. as required

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Date getExpiryDate(Date startDate, String noOfYears) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        try {
            int years = Integer.parseInt(noOfYears.trim());
            calendar.add(Calendar.YEAR, years);
        } catch (NumberFormatException e) {
            // Handle error or set a default expiry
        }
        return calendar.getTime();
    }

    private KeyStore loadOrCreateKeystore(String path, String password, PGPConfig config) throws Exception {
        KeyStore keystore;
        try {
            // Attempt to load existing keystore
            keystore = KeyStore.getInstance(config.getKeyStoreType(), config.getProviderName());
            try (InputStream in = new FileInputStream(path)) {
                keystore.load(in, password.toCharArray());
            }
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            // Create a new keystore if loading failed
            keystore = KeyStore.getInstance(config.getKeyStoreType(), config.getProviderName());
            keystore.load(null, password.toCharArray());
        }
        return keystore;
    }
}
