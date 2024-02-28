import org.bouncycastle.bcpg.SignatureSubpacketTags;
import org.bouncycastle.openpgp.*;

import java.io.*;
import java.util.Date;

public class PGPKeyExpiration {

    public static void main(String[] args) {
        String keyRingFilePath = "path/to/your/keyring.gpg";

        try {
            Date expirationDate = getKeyExpirationDate(keyRingFilePath);
            if (expirationDate != null) {
                System.out.println("Key Expiration Date: " + expirationDate);
            } else {
                System.out.println("Key does not have an expiration date.");
            }
        } catch (IOException | PGPException e) {
            e.printStackTrace();
        }
    }

    private static Date getKeyExpirationDate(String keyRingFilePath) throws IOException, PGPException {
        try (InputStream keyRingStream = new FileInputStream(keyRingFilePath)) {
            PGPPublicKeyRingCollection keyRingCollection = new PGPPublicKeyRingCollection(keyRingStream, new JcaKeyFingerprintCalculator());

            for (PGPPublicKeyRing keyRing : keyRingCollection) {
                for (PGPPublicKey publicKey : keyRing) {
                    Date expirationDate = getSignatureExpirationDate(publicKey.getSelfSignature());
                    if (expirationDate != null) {
                        return expirationDate;
                    }
                }
            }
        }

        return null;
    }

    private static Date getSignatureExpirationDate(PGPSignature signature) {
        PGPSignatureSubpacketVector subpackets = signature.getHashedSubPackets();
        if (subpackets != null) {
            SignatureSubpacket subpacket = subpackets.getSubpacket(SignatureSubpacketTags.KEY_EXPIRE_TIME);
            if (subpacket != null) {
                long expirationTime = subpacket.getTime();
                if (expirationTime > 0) {
                    return new Date(expirationTime * 1000L);
                }
            }
        }
        return null;
    }
}
