public MessageResponse signFile(SignFileRequest req) throws Exception {
    MessageResponse response = new MessageResponse();
    String inputFile = FilenameUtils.getFullPath(req.getInputFile()) + FilenameUtils.getName(req.getInputFile());
    String outputFile = FilenameUtils.getFullPath(req.getOutputFile()) + FilenameUtils.getName(req.getOutputFile());

    try (FileOutputStream out = new FileOutputStream(outputFile);
         ArmoredOutputStream outArmor = req.isArmor() ? new ArmoredOutputStream(out) : null;
         BCPGOutputStream bout = new BCPGOutputStream(req.isArmor() ? outArmor : out);
         OutputStream lOut = setupLiteralDataGenerator(bout, inputFile);
         FileInputStream fIn = new FileInputStream(new File(inputFile));
         PGPCompressedDataGenerator pgpCompressedDataGenerator = new PGPCompressedDataGenerator(CompressionAlgorithmTags.ZLIB)) {

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

private OutputStream setupLiteralDataGenerator(OutputStream out, String inputFile) throws IOException {
    PGPLiteralDataGenerator lGen = new PGPLiteralDataGenerator();
    return lGen.open(out, PGPLiteralData.BINARY, new File(inputFile));
}

private PGPSignatureGenerator setupPGPSignatureGenerator(SignFileRequest req) throws PGPException, NoSuchAlgorithmException, NoSuchProviderException {
    // The same setupPGPSignatureGenerator method as before
}
