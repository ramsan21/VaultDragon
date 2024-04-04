private static KeyStore createDummyKeyStore(Config config) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException {
        KeyStore keyStore = KeyStore.getInstance(config.getKeyStoreType(), config.getProviderName());
        keyStore.load(null, null);

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", config.getProviderName());
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        X500Name issuer = new X500Name("CN=Dummy Issuer");
        X500Name subject = new X500Name("CN=Dummy Subject");
        long validityInDays = 365;
        Date notBefore = new Date(System.currentTimeMillis() - (validityInDays * 24 * 60 * 60 * 1000));
        Date notAfter = new Date(System.currentTimeMillis() + (validityInDays * 24 * 60 * 60 * 1000));

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(issuer, BigInteger.ONE, notBefore, notAfter, subject, keyPair.getPublic());
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(keyPair.getPrivate());
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certBuilder.build(signer));

        keyStore.setKeyEntry("dummyAlias", keyPair.getPrivate(), "dummyPassword".toCharArray(), new java.security.cert.Certificate[]{cert});

        File dummyKeyStoreFile = File.createTempFile("dummyKeyStore", ".keystore");
        dummyKeyStoreFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(dummyKeyStoreFile);
        keyStore.store(fos, "dummyPassword".toCharArray());
        fos.close();

        return keyStore;
    }
