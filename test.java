public static void encryptFile(String fileToEncrypt, String recipientEmails, String outputFile, String signingKeyPassphrase) {
    displayAndExecuteGPGCommand("--encrypt", "\"" + fileToEncrypt + "\"", "\"" + outputFile + "\"", signingKeyPassphrase, "--recipient", recipientEmails.split(","));
}

private static void displayAndExecuteGPGCommand(String gpgOperation, String inputFile, String outputFile, String signingKeyPassphrase, String... extraArgs) {
    try {
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
        command.add("-o");
        command.add(outputFile);

        // Display the command
        System.out.println("Executing command: " + String.join(" ", command));

        // Execute the command
        Process process = pb.start();

        // Write the file content to the stdin of the gpg process
        byte[] fileBytes = Files.readAllBytes(Paths.get(inputFile.replace("\"", "")));
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

        if (exitCode != 0) {
            throw new RuntimeException("GPG command failed with exit code: " + exitCode);
        }
    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
    }
}
