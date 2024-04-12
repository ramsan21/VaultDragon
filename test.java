import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PGPFileSignerEncryptor {
    public static void signAndEncryptFile(String fileToSign, String fileToEncrypt, String recipientEmail, String signingKeyPassphrase) {
        try {
            // Read the file content to be signed and encrypted
            byte[] fileBytes = Files.readAllBytes(Paths.get(fileToSign));

            // Create a ProcessBuilder to run the 'gpg --sign --encrypt' command
            ProcessBuilder pb = new ProcessBuilder("gpg", "--batch", "--yes", "--passphrase", signingKeyPassphrase, "--recipient", recipientEmail, "--sign", "--encrypt");
            Process process = pb.start();

            // Write the file content to the stdin of the 'gpg --sign --encrypt' process
            process.getOutputStream().write(fileBytes);
            process.getOutputStream().flush();
            process.getOutputStream().close();

            // Read the output from the 'gpg --sign --encrypt' process
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to finish and get the exit code
            int exitCode = process.waitFor();
            System.out.println("Process exit code: " + exitCode);

            // Save the signed and encrypted file
            byte[] encryptedFileBytes = Files.readAllBytes(Paths.get(fileToEncrypt));
            Files.write(Paths.get(fileToEncrypt + ".gpg"), encryptedFileBytes);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String fileToSign = "path/to/file.txt";
        String recipientEmail = "recipient@example.com";
        String signingKeyPassphrase = "your_signing_key_passphrase";
        signAndEncryptFile(fileToSign, fileToSign, recipientEmail, signingKeyPassphrase);
    }
}
