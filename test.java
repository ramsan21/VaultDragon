@PostMapping("/reset")
    public ResponseEntity<Void> reset() {
        // Trigger POST reset API
        ResponseEntity<Void> response = restTemplate.postForEntity("https://example.com/reset", null, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            // Make 10 API calls to the same request at once with HTTPS disabled
            ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
            List<Future<Void>> futures = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                Future<Void> future = executor.submit(() -> {
                    try {
                        restTemplate.setHttps(false);
                        restTemplate.postForObject("http://example.com/api", null, Void.class);
                    } finally {
                        restTemplate.setHttps(true);
                    }
                });
                futures.add(future);
            }

            // Wait for all 10 API calls to complete
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    // Handle any exceptions that occur during the API calls
                    e.printStackTrace();
                }
            }
        } else {
            // Handle the error response from the POST reset API
            System.err.println("Error triggering POST reset API: " + response.getStatusCode());
        }

        return ResponseEntity.ok().build();
    }
}
