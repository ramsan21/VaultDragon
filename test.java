try {
            RestTemplate restTemplate = new RestTemplate();

            KeyStore keyStore = KeyStore.getInstance("jks");

            File keyFile = new File(KEY_STORE_FILE);
            FileSystemResource fileSystemResource = new FileSystemResource(keyFile);

            InputStream inputStream = fileSystemResource.getInputStream();
            keyStore.load(inputStream,
                    Objects.requireNonNull(KEY_STORE_PASS).toCharArray());

            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(new SSLContextBuilder()
                    .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                    .loadKeyMaterial(keyStore,
                            KEY_PASS.toCharArray()).build(),
                    NoopHostnameVerifier.INSTANCE);

            HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory)
                    .build();

            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

            restTemplate.setRequestFactory(requestFactory);

            return restTemplate;

        } catch (Exception e) {
            logger.debug("SSL keystore exception", e);
            throw new BlahBlahException(e);
        }


