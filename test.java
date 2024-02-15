import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

import java.io.*;
import java.security.SecureRandom;

public class PgpEncryptFile {

    public ResultObject encryptFile(String inputFile, String outputFile, String publicKeyFile, boolean useArmor, boolean withIntegrityCheck, String encAlgo) {
        ResultObject result = new ResultObject();

        try {
            PGPPublicKey encKey = PgpUtils.readPublicKey(new FileInputStream(publicKeyFile));

            if (encKey == null) {
                result.setIntValue(PGActionConstants.ACTION_FAILURE);
                result.setErrMsg("Unable to get the encryption key from the keyring. Identity=" + publicKeyFile);
                result.setOutFileName(outputFile);
                return result;
            }

            OutputStream outputStream = new FileOutputStream(outputFile);

            if (useArmor) {
                outputStream = new ArmoredOutputStream(outputStream);
            }

            PGPCompressedDataGenerator compressedDataGenerator = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
            PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
            OutputStream encryptedOutputStream = literalDataGenerator.open(
                    new JcaPGPDataEncryptorBuilder(PGPEncryptedData.CAST5)
                            .setWithIntegrityPacket(withIntegrityCheck)
                            .setSecureRandom(new SecureRandom())
                            .build(), outputStream);

            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile))) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    encryptedOutputStream.write(buffer, 0, bytesRead);
                }
            }

            if (useArmor) {
                outputStream.close();
            }

            result.setIntValue(PGActionConstants.ACTION_SUCCESS);
            result.setSuccessMsg("Encrypted file [" + outputFile + "] created successfully.");
        } catch (Exception e) {
            result.setIntValue(PGActionConstants.ACTION_FAILURE);
            result.setErrMsg("Caught " + e.getClass().getName() + ". Error Msg: " + e.getMessage() + ": PGP Encryption Failed");
            e.printStackTrace(); // Log or handle the exception appropriately
        }

        return result;
    }

    // Other methods and classes, such as PgpUtils, should be defined based on your existing code.
}
