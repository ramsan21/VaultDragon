@Bean
public Step runShellCommandAndModifyFileStep() {
    return stepBuilderFactory.get("runShellCommandAndModifyFileStep")
        .tasklet((contribution, chunkContext) -> {

            // Step 1: Run the shell command and capture the output
            String[] command = {
                "bash", "-c",
                "awk -F '\\001' '$1 == \"D\" {print $1}' ALL_STARSECURITY_AUDIT_LOG_20250508_D_I_0.dat | wc -l"
            };

            Process process = new ProcessBuilder(command).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Shell command failed");
            }

            // Save output to variable
            int recordCount = Integer.parseInt(output.trim());
            System.out.println("Record count: " + recordCount);

            // Step 2: Read and replace last line starting with "T"
            Path filePath = Paths.get("ALL_STARSECURITY_AUDIT_LOG_20250508_D_I_0.dat");
            List<String> lines = Files.readAllLines(filePath);

            if (!lines.isEmpty()) {
                int lastIndex = lines.size() - 1;
                if (lines.get(lastIndex).startsWith("T")) {
                    // Example replacement: Replace "T" line with "T|<recordCount>"
                    lines.set(lastIndex, "T|" + recordCount);
                    Files.write(filePath, lines);
                    System.out.println("Replaced last T line with updated content.");
                }
            }

            return RepeatStatus.FINISHED;
        })
        .build();
}