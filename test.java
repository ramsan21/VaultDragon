import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.Cipher;

public class EncryptionManager {

    private static final String ALGORITHM = "RSA";
    private static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    public static String removeSpace(String str) {
        return str.replaceAll("\\s+", "");
    }

    private byte[] stringToArrayBuffer(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }

    private String arrayBufferToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private byte[] encryptDataWithPublicKey(PublicKey key, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    private PublicKey getCryptoKey(String exponent, String modulus) throws Exception {
        byte[] decodedExponent = Base64.getUrlDecoder().decode(exponent);
        byte[] decodedModulus = Base64.getUrlDecoder().decode(modulus);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(new java.math.BigInteger(1, decodedModulus), new java.math.BigInteger(1, decodedExponent));
        KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
        return factory.generatePublic(spec);
    }

    private String addHashSetToPword(String randomString, String str) {
        return removeSpace(str) + "_-_" + removeSpace(randomString);
    }

    public String processEncryptResponse(String strToEncrypt, Map<String, String> response) throws Exception {
        String randomString = response.get("randomString");
        String modulus = response.get("modulus");
        String exponent = response.get("exponent");

        if (randomString == null || modulus == null || exponent == null) {
            throw new IllegalArgumentException("No expected Parameters");
        }

        PublicKey cryptoKey = getCryptoKey(exponent, modulus);
        String hashedStr = addHashSetToPword(randomString, strToEncrypt);
        byte[] encryptedData = encryptDataWithPublicKey(cryptoKey, stringToArrayBuffer(hashedStr));
        return arrayBufferToHex(encryptedData);
    }

    public Map<String, String> processEncryptResponse(Map<String, String> strToEncrypt, Map<String, String> response) throws Exception {
        String randomString = response.get("randomString");
        String modulus = response.get("modulus");
        String exponent = response.get("exponent");

        if (randomString == null || modulus == null || exponent == null) {
            throw new IllegalArgumentException("No expected Parameters");
        }

        PublicKey cryptoKey = getCryptoKey(exponent, modulus);

        return strToEncrypt.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    try {
                        String hashedStr = addHashSetToPword(randomString, entry.getValue());
                        byte[] encryptedData = encryptDataWithPublicKey(cryptoKey, stringToArrayBuffer(hashedStr));
                        return arrayBufferToHex(encryptedData);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        ));
    }
}
