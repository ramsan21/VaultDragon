public static PGPPublicKeyRingCollection addOrUpdatePublicKeyRing(PGPPublicKeyRingCollection keyRingCollection, PGPPublicKeyRing keyRing) {
        try {
            List<PGPPublicKeyRing> keyRings = new ArrayList<>();
            boolean isUpdated = false;

            // Iterate through the existing key rings
            for (Iterator<PGPPublicKeyRing> it = keyRingCollection.getKeyRings(); it.hasNext(); ) {
                PGPPublicKeyRing existingKeyRing = it.next();

                // If the key ring is already present, update it
                if (existingKeyRing.getPublicKey().getKeyID() == keyRing.getPublicKey().getKeyID()) {
                    keyRings.add(keyRing);
                    isUpdated = true;
                } else {
                    keyRings.add(existingKeyRing);
                }
            }

            // If the key ring was not found, add it to the collection
            if (!isUpdated) {
                keyRings.add(keyRing);
            }

            // Create a new PGPPublicKeyRingCollection with the updated key rings
            return new PGPPublicKeyRingCollection(keyRings);
        } catch (PGPException | IOException e) {
            e.printStackTrace();
            return keyRingCollection;
        }
    }

TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256
