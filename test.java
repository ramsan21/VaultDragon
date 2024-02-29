public PGPPublicKey findEncryptionKey(List<CustomerKey> customerKeys) {
        for (CustomerKey customerKey : customerKeys) {
            PGPPublicKey publicKey = extractEncryptionKey(customerKey.getPublicKeyData());
            if (publicKey != null) {
                return publicKey;
            }
        }
        return null; // No encryption key found in any key
    }

    private PGPPublicKey extractEncryptionKey(byte[] publicKeyData) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(publicKeyData)) {
            PGPPublicKeyRingCollection keyRingCollection = new PGPPublicKeyRingCollection(inputStream);
            
            for (PGPPublicKeyRing keyRing : keyRingCollection) {
                for (PGPPublicKey publicKey : keyRing) {
                    if (publicKey.isEncryptionKey()) {
                        return publicKey;
                    }
                }
            }
        } catch (IOException | PGPException e) {
            e.printStackTrace(); // Handle exception appropriately
        }
        return null; // No encryption key found in this key
    }
