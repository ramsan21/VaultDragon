import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PGPFileEncryptorDecryptor {
    public static void encryptFile(String fileToEncrypt, String recipientEmails, String outputFile, String signingKeyPassphrase) {
        displayAndExecuteGPGCommand("--encrypt", "--recipient", recipientEmails, "--output", outputFile, fileToEncrypt);
    }

    public static void decryptFile(String fileToDecrypt, String outputFile, String signingKeyPassphrase) {
        displayAndExecuteGPGCommand("--decrypt", "--output", outputFile, fileToDecrypt);
    }

    public static void signFile(String fileToSign, String outputFile, String signingKeyPassphrase, String localUserEmail) {
        displayAndExecuteGPGCommand("--sign", "--local-user", localUserEmail, "--output", outputFile, fileToSign);
    }

    public static void verifyFile(String fileToVerify, String signerEmails, String outputFile) {
        displayAndExecuteGPGCommand("--verify", "--trusted-key", signerEmails, "--output", outputFile, fileToVerify);
    }

    public static void signAndEncryptFile(String fileToSign, String fileToEncrypt, String recipientEmails, String signingKeyPassphrase, String localUserEmail) {
        displayAndExecuteGPGCommand("--sign", "--encrypt", "--recipient", recipientEmails, "--local-user", localUserEmail, "--output", fileToEncrypt, fileToSign);
    }

    public static void decryptAndVerifyFile(String fileToDecrypt, String outputFile, String signerEmails, String signingKeyPassphrase) {
        displayAndExecuteGPGCommand("--decrypt", "--verify", "--trusted-key", signerEmails, "--output", outputFile, fileToDecrypt);
    }

    private static void displayAndExecuteGPGCommand(String... args) {
        try {
            // Create a ProcessBuilder with the appropriate gpg command
            List<String> command = new ArrayList<>();
            command.add("C:\\Program Files (x86)\\GnuPG\\bin\\gpg.exe"); // Replace with the full path to gpg.exe
            command.add("--batch");
            command.add("--yes");

            for (String arg : args) {
                command.add(arg);
            }

            // Display the command
            System.out.println("Executing command: " + String.join(" ", command));

            // Execute the command
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            ExecutorService executor = Executors.newFixedThreadPool(2);

            // Handle input stream
            executor.execute(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // Handle error stream
            executor.execute(() -> {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);

            // Wait for the process to finish and get the exit code
            int exitCode = process.waitFor();
            System.out.println("Process exit code: " + exitCode);

            if (exitCode != 0) {
                throw new RuntimeException("GPG command failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Same main method as before
    }
}
