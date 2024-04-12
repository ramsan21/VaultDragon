import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PGPFileEncryptorDecryptor {
    public static void encryptFile(String fileToEncrypt, String recipientEmails, String outputFile, String signingKeyPassphrase) {
        performGPGOperation("--encrypt", fileToEncrypt, outputFile, signingKeyPassphrase, "--recipient", recipientEmails.split(","), "--output", outputFile);
    }

    public static void decryptFile(String fileToDecrypt, String outputFile, String signingKeyPassphrase) {
        performGPGOperation("--decrypt", fileToDecrypt, outputFile, signingKeyPassphrase, "--output", outputFile);
    }

    public static void signFile(String fileToSign, String outputFile, String signingKeyPassphrase, String localUserEmail) {
        performGPGOperation("--sign", fileToSign, outputFile, signingKeyPassphrase, "--local-user", localUserEmail, "--output", outputFile);
    }

    public static void verifyFile(String fileToVerify, String signerEmails, String outputFile) {
        performGPGOperation("--verify", fileToVerify, outputFile, null, "--trusted-key", signerEmails.split(","), "--output", outputFile);
    }

    public static void signAndEncryptFile(String fileToSign, String fileToEncrypt, String recipientEmails, String signingKeyPassphrase, String localUserEmail) {
        performGPGOperation("--sign --encrypt", fileToSign, fileToEncrypt, signingKeyPassphrase, "--recipient", recipientEmails.split(","), "--local-user", localUserEmail, "--output", fileToEncrypt);
    }

    public static void decryptAndVerifyFile(String fileToDecrypt, String outputFile, String signerEmails, String signingKeyPassphrase) {
        performGPGOperation("--decrypt --verify", fileToDecrypt, outputFile, signingKeyPassphrase, "--trusted-key", signerEmails.split(","), "--output", outputFile);
    }

    private static void performGPGOperation(String gpgOperation, String inputFile, String outputFile, String signingKeyPassphrase, String... extraArgs) {
        try {
            // Read the file content to be processed
            byte[] fileBytes = Files.readAllBytes(Paths.get(inputFile));

            // Create a ProcessBuilder with the appropriate gpg command
            ProcessBuilder pb = new ProcessBuilder("gpg", "--batch", "--yes");
            List<String> command = pb.command();
            command.add(gpgOperation);

            if (signingKeyPassphrase != null) {
                command.add("--passphrase");
                command.add(signingKeyPassphrase);
            }

            for (String arg : extraArgs) {
                command.add(arg);
            }

            command.add(inputFile);

            Process process = pb.start();

            // Write the file content to the stdin of the gpg process
            process.getOutputStream().write(fileBytes);
            process.getOutputStream().flush();
            process.getOutputStream().close();

            // Read the output from the gpg process
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to finish and get the exit code
            int exitCode = process.waitFor();
            System.out.println("Process exit code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Encrypt a file
        String fileToEncrypt = "path/to/file.txt";
        String recipientEmails = "recipient1@example.com,recipient2@example.com";
        String encryptedOutputFile = "path/to/encrypted_file.gpg";
        String signingKeyPassphrase = "your_signing_key_passphrase";
        encryptFile(fileToEncrypt, recipientEmails, encryptedOutputFile, signingKeyPassphrase);

        // Decrypt a file
        String fileToDecrypt = "path/to/encrypted_file.gpg";
        String decryptedOutputFile = "path/to/decrypted_file.txt";
        decryptFile(fileToDecrypt, decryptedOutputFile, signingKeyPassphrase);

        // Sign a file
        String fileToSign = "path/to/file.txt";
        String signedOutputFile = "path/to/signed_file.txt.sig";
        String localUserEmail = "your_email@example.com";
        signFile(fileToSign, signedOutputFile, signingKeyPassphrase, localUserEmail);

        // Verify a file
        String fileToVerify = "path/to/signed_file.txt.sig";
        String signerEmails = "signer1@example.com,signer2@example.com";
        String verifiedOutputFile = "path/to/verified_file.txt";
        verifyFile(fileToVerify, signerEmails, verifiedOutputFile);

        // Sign and encrypt a file
        String fileToSignAndEncrypt = "path/to/file.txt";
        String signedAndEncryptedOutputFile = "path/to/signed_and_encrypted_file.gpg";
        signAndEncryptFile(fileToSignAndEncrypt, signedAndEncryptedOutputFile, recipientEmails, signingKeyPassphrase, localUserEmail);

        // Decrypt and verify a file
        String fileToDecryptAndVerify = "path/to/signed_and_encrypted_file.gpg";
        String decryptedAndVerifiedOutputFile = "path/to/decrypted_and_verified_file.txt";
        decryptAndVerifyFile(fileToDecryptAndVerify, decryptedAndVerifiedOutputFile, signerEmails, signingKeyPassphrase);
    }
}
