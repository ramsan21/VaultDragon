public String getUserID(long keyID, boolean bankKey) throws Exception {
        String publicKeyPath = bankKey ? getConfig().getBankPublicKeyPath() : getConfig().getClientPublicKeyPath();

        try (InputStream in = new FileInputStream(Objects.requireNonNull(publicKeyPath, "Public key path cannot be null"))) {
            PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(
                    PGPUtil.getDecoderStream(in), new JcaKeyFingerprintCalculator());

            PGPPublicKey key = Objects.requireNonNull(pgpPub.getPublicKey(keyID),
                    "Unable to get the Key from Public Keyring. KeyID = " + Long.toHexString(keyID));

            Iterator<String> userIds = key.getUserIDs();

            if (userIds.hasNext()) {
                return userIds.next();
            } else {
                log.error("Unable to find the associated user id for the keyID. KeyID = {}", Long.toHexString(keyID));
                return null;
            }
        }
    }
