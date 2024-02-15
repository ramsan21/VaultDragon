import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.PGPEncryptedData;
import org.bouncycastle.bcpg.PGPEncryptedDataGenerator;
import org.bouncycastle.bcpg.PGPLiteralData;
import org.bouncycastle.bcpg.PGPCompressedDataGenerator;
import org.bouncycastle.bcpg.PGPPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;

public class YourClassName {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public ResultObject encryptFile(String strInputFile, String strOutputFile, String strPKIdentity,
                                    boolean bInArmor, boolean withIntegrityCheck, String encAlgo)
            throws IOException, NoSuchProviderException {

        ResultObject resObj = new ResultObject();
        strOutputFile = checkOutputFile(strOutputFile);

        try (OutputStream out = new FileOutputStream(strOutputFile)) {
            OutputStream outArmor = bInArmor ? new ArmoredOutputStream(out) : null;

            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);

                PGPPublicKey encKey = keyCtrIr.getEncryptKey(strPKIdentity, false);
                if (encKey == null) {
                    resObj.setIntValue(PGActionConstants.ACTION_FAILURE);
                    resObj.setErrMsg("Unable to get the encrypt key from keyring. Identity=" + strPKIdentity);
                    resObj.setOutFileName(strOutputFile);
                    return resObj;
                }

                Poputil.writeFileToLiteralData(comData.open(bout), PGPLiteralData.BINARY, new File(strInputFile));
                comData.close();

                PGPEncryptedDataGenerator cPk;
                if (encAlgo == null || encAlgo.trim().isEmpty()) {
                    System.out.println("Default algorithm used for encryption");
                    cPk = new PGPEncryptedDataGenerator(PGPEncryptedData.CAST5, withIntegrityCheck, new SecureRandom());
                } else {
                    int secretAlgo = algoUtil.getSymmetricCipherValueByName(encAlgo);
                    System.out.println("Algorithm used for encryption = " + encAlgo);
                    cPk = new PGPEncryptedDataGenerator(secretAlgo, withIntegrityCheck, new SecureRandom());
                }

                cPk.addMethod(encKey);
                byte[] bytes = bout.toByteArray();

                try (OutputStream cout = (bInArmor) ? cPk.open(outArmor, bytes.length) : cPk.open(out, bytes.length)) {
                    cout.write(bytes);
                }

                resObj.setIntValue(PGActionConstants.ACTION_SUCCESS);
                resObj.setSuccessMsg("Encrypted file [" + strOutputFile + "] created successfully.");
                System.out.println("Encryption successful");

            } catch (Exception e) {
                System.err.println("Caught " + e.getClass().getName() + ". Error Msg:" + e.getMessage() + ": PGP Encryption Failed");
                resObj.setIntValue(PGActionConstants.ACTION_FAILURE);
                resObj.setErrMsg("Caught " + e.getClass().getName() + ". Error Msg:" + e.getMessage() + ": PGP Encryption Failed");
            }
        }

        return resObj;
    }

    private String checkOutputFile(String strOutputFile) throws IOException {
        try {
            System.out.println("PGP output dir path ==" + strOutputFile + "; Create File? " + (strOutputFile == null || strOutputFile.equals("")));
            System.out.println("OUT_STREAM_DIR PATH ==" + keyCtrIr.getPGConfigProperty(PGPConfigConstants.OUT_STREAM_DIR));
            
            if (strOutputFile == null || strOutputFile.equals("")) {
                String uniq = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().replace('@', '-') + "_";
                File sharedOutputFile = File.createTempFile("pgp_" + uniq, ".tmp", new File(keyCtrIr.getPGPConfigProperty(PGPConfigConstants.OUT_STREAM_DIR)));
                strOutputFile = sharedOutputFile.getAbsolutePath();
            }
        } catch (IOException e) {
            System.err.println("IOException Caught " + e.getClass().getName() + ": " + e.getMessage());
            throw e;
        }
        System.out.println("Returned Output File ==" + strOutputFile);
        return strOutputFile;
    }
}
