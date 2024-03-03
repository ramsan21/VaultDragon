public MessageResponse signFile(SignFileRequest req) throws Exception {
    MessageResponse response = new MessageResponse();
    String inputFile = FilenameUtils.getFullPath(req.getInputFile()) + FilenameUtils.getName(req.getInputFile());
    String outputFile = FilenameUtils.getFullPath(req.getOutputFile()) + FilenameUtils.getName(req.getOutputFile());

    FileOutputStream out = null;
    ArmoredOutputStream outArmor = null;
    BCPGOutputStream bout = null;
    OutputStream lOut = null;
    FileInputStream fIn = null;
    PGPCompressedDataGenerator pgpCompressedDataGenerator = null;

    try {
        out = new FileOutputStream(outputFile);
        outArmor = req.isArmor() ? new ArmoredOutputStream(out) : null;
        bout = new BCPGOutputStream(req.isArmor() ? outArmor : out);
        lOut = setupLiteralDataGenerator(bout, inputFile);
        fIn = new FileInputStream(new File(inputFile));
        pgpCompressedDataGenerator = new PGPCompressedDataGenerator(CompressionAlgorithmTags.ZLIB);

        PGPSignatureGenerator pgpSignatureGenerator = setupPGPSignatureGenerator(req);

        pgpSignatureGenerator.generateOnePassVersion(false).encode(bout);

        int ch;
        while ((ch = fIn.read()) >= 0) {
            lOut.write(ch);
            pgpSignatureGenerator.update((byte) ch);
        }

        pgpSignatureGenerator.generate().encode(bout);

        response.setStatusCode(StatusCode.SUCCESS.getCode());
        response.setSuccessMessage("Signed file [" + outputFile + "] created successfully.");
        response.setOutFileName(outputFile);
        log.info("Sign successful");

    } catch (Exception e) {
        log.error(e.getClass().getName(), e);
        response.setStatusCode(StatusCode.STAR_FUNC_FAIL.getCode());
        response.setErrorMessage("Error Msg:" + e.getMessage() + " PGP Signing Failed");
    } finally {
        try {
            if (pgpCompressedDataGenerator != null) {
                pgpCompressedDataGenerator.close();
            }
            if (lOut != null) {
                lOut.close();
            }
            if (bout != null) {
                bout.close();
            }
            if (outArmor != null) {
                outArmor.close();
            }
            if (out != null) {
                out.close();
            }
            if (fIn != null) {
                fIn.close();
            }
        } catch (IOException e) {
            log.warn("Failed to close input/output streams", e);
        }
    }

    return response;
}

private OutputStream setupLiteralDataGenerator(OutputStream out, String inputFile) throws IOException {
    PGPLiteralDataGenerator lGen = new PGPLiteralDataGenerator();
    return lGen.open(out, PGPLiteralData.BINARY, new File(inputFile));
}

private PGPSignatureGenerator setupPGPSignatureGenerator(SignFileRequest req) throws PGPException, NoSuchAlgorithmException, NoSuchProviderException {
    PGPPrivateKey pgpPrivateKey = this.keyChainHandler.findSecretKey(req.getIdentity());
    if (pgpPrivateKey == null) {
        throw new PGPException("Unable to get the secret key from keyring. Identity=" + req.getIdentity());
    }

    PGPPublicKey pgpPublicKey = this.keyChainHandler.getBankMastKey(req.getIdentity());
    Iterator<String> it = pgpPublicKey.getUserIDs();
    log.info("Hash algorithm used for signing = " + req.getHashAlgo());

    PGPSignatureGenerator pgpSignatureGenerator = new PGPSignatureGenerator(
            new JcaPGPContentSignerBuilder(PublicKeyAlgorithmTags.RSA_GENERAL,
                    algoUtil.getSymmetricCipherValueByName(req.getHashAlgo()))
                    .setProvider(config.getProviders())
                    .setDigestProvider(config.getProviders()));

    pgpSignatureGenerator.init(PGPSignature.BINARY_DOCUMENT, pgpPrivateKey);

    if (it.hasNext()) {
        String user = it.next();
        PGPSignatureSubpacketGenerator spen = new PGPSignatureSubpacketGenerator();
        spen.addSignerUserID(false, user.getBytes(StandardCharsets.UTF_8));
        pgpSignatureGenerator.setHashedSubpackets(spen.generate());
    }

    return pgpSignatureGenerator;
}
