import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GPGExample {
    private static final String GPG_PATH = "C:\\Program Files (x86)\\GnuPG\\bin\\gpg.exe";

    public static void encryptFile(String fileToEncrypt, String recipientEmail, String outputFile) {
        List<String> command = new ArrayList<>();
        command.add(GPG_PATH);
        command.add("--batch");
        command.add("--yes");
        command.add("--encrypt");
        command.add("--recipient");
        command.add(recipientEmail);
        command.add("--output");
        command.add(outputFile);
        command.add(fileToEncrypt);

        executeCommand(command);
    }

    public static void decryptFile(String fileToDecrypt, String outputFile) {
        List<String> command = new ArrayList<>();
        command.add(GPG_PATH);
        command.add("--batch");
        command.add("--yes");
        command.add("--decrypt");
        command.add("--output");
        command.add(outputFile);
        command.add(fileToDecrypt);

        executeCommand(command);
    }

    public static void signFile(String fileToSign, String signerEmail, String outputFile) {
        List<String> command = new ArrayList<>();
        command.add(GPG_PATH);
        command.add("--batch");
        command.add("--yes");
        command.add("--sign");
        command.add("--local-user");
        command.add(signerEmail);
        command.add("--output");
        command.add(outputFile);
        command.add(fileToSign);

        executeCommand(command);
    }

    public static void verifyFile(String fileToVerify) {
        List<String> command = new ArrayList<>();
        command.add(GPG_PATH);
        command.add("--batch");
        command.add("--yes");
        command.add("--verify");
        command.add(fileToVerify);

        executeCommand(command);
    }

    public static void signAndEncryptFile(String fileToSign, String fileToEncrypt, String recipientEmail, String signerEmail) {
        List<String> command = new ArrayList<>();
        command.add(GPG_PATH);
        command.add("--batch");
        command.add("--yes");
        command.add("--sign");
        command.add("--encrypt");
        command.add("--recipient");
        command.add(recipientEmail);
        command.add("--local-user");
        command.add(signerEmail);
        command.add("--output");
        command.add(fileToEncrypt);
        command.add(fileToSign);

        executeCommand(command);
    }

    public static void decryptAndVerifyFile(String fileToDecrypt, String outputFile) {
        List<String> command = new ArrayList<>();
        command.add(GPG_PATH);
        command.add("--batch");
        command.add("--yes");
        command.add("--decrypt");
        command.add("--verify");
        command.add("--output");
        command.add(outputFile);
        command.add(fileToDecrypt);

        executeCommand(command);
    }

    private static void executeCommand(List<String> command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Command executed with exit code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Example usage
        String fileToEncrypt = "path/to/input/file.txt";
        String recipientEmail = "recipient@example.com";
        String encryptedOutputFile = "path/to/output/encrypted_file.gpg";
        encryptFile(fileToEncrypt, recipientEmail, encryptedOutputFile);

        String fileToDecrypt = "path/to/input/encrypted_file.gpg";
        String decryptedOutputFile = "path/to/output/decrypted_file.txt";
        decryptFile(fileToDecrypt, decryptedOutputFile);

        String fileToSign = "path/to/input/file.txt";
        String signerEmail = "signer@example.com";
        String signedOutputFile = "path/to/output/signed_file.sig";
        signFile(fileToSign, signerEmail, signedOutputFile);

        String fileToVerify = "path/to/input/signed_file.sig";
        verifyFile(fileToVerify);

        String fileToSignAndEncrypt = "path/to/input/file.txt";
        String signedAndEncryptedOutputFile = "path/to/output/signed_and_encrypted_file.gpg";
        signAndEncryptFile(fileToSignAndEncrypt, signedAndEncryptedOutputFile, recipientEmail, signerEmail);

        String fileToDecryptAndVerify = "path/to/input/signed_and_encrypted_file.gpg";
        String decryptedAndVerifiedOutputFile = "path/to/output/decrypted_and_verified_file.txt";
        decryptAndVerifyFile(fileToDecryptAndVerify, decryptedAndVerifiedOutputFile);
    }
}
