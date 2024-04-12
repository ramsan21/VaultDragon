public static void importPublicKey(String publicKeyFile) {
        List<String> command = new ArrayList<>();
        command.add(GPG_PATH);
        command.add("--batch");
        command.add("--yes");
        command.add("--import");
        command.add(publicKeyFile);

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
Map<String, String> env = processBuilder.environment();
env.put("GPG_TTY", "/dev/tty");
