public MessageResponse signFile(SignFileRequest req) throws Exception {
    MessageResponse response = new MessageResponse();
    String inputFile = FilenameUtils.getFullPath(req.getInputFile()) + FilenameUtils.getName(req.getInputFile());
    String outputFile = FilenameUtils.getFullPath(req.getOutputFile()) + FilenameUtils.getName(req.getOutputFile());

    try (FileOutputStream out = new FileOutputStream(outputFile);
         ArmoredOutputStream outArmor = req.isArmor() ? new ArmoredOutputStream(out) : null;
         PGPCompressedDataGenerator pgpCompressedDataGenerator = new PGPCompressedDataGenerator(CompressionAlgorithmTags.ZLIB);
         BCPGOutputStream bout = new BCPGOutputStream(req.isArmor() ? outArmor : out);
         PGPLiteralDataGenerator lGen = new PGPLiteralDataGenerator();
         OutputStream lOut = lGen.open(bout, PGPLiteralData.BINARY, new File(inputFile));
         FileInputStream fIn = new FileInputStream(new File(inputFile))) {

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
    }

    return response;
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

