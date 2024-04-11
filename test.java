public void exportPublicKey(String userEmail, String outputFile) {
        ProcessBuilder processBuilder = new ProcessBuilder(gpgExecutablePath, "--export", "-a", userEmail);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

        try {
            Process process = processBuilder.start();
            
            try (OutputStream fos = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[1024];
                int length;
                
                // Read from the output stream of the process
                while ((length = process.getInputStream().read(buffer)) != -1) {
                    fos.write(buffer, 0, length);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Public key successfully exported to " + outputFile);
            } else {
                System.err.println("Failed to export public key, GPG exited with code " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }
    }
