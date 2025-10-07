This happens because the request body hasn’t been consumed yet when you print the “Inbound” log.
ContentCachingRequestWrapper only fills its cache after someone reads the body. If you log before the controller runs, the cache is still empty → you see "".

Fix (one liner idea)

Before you log inbound, trigger the wrapper to buffer the body yourself:

private static void primeCache(ContentCachingRequestWrapper req) throws IOException {
  // for JSON / raw bodies
  if (req.getContentAsByteArray().length == 0) {
    // reading once causes the wrapper to cache the bytes
    StreamUtils.copyToByteArray(req.getInputStream());
  }
  // for application/x-www-form-urlencoded (Spring reads parameters, not raw body)
  req.getParameterMap();
}

Then call this right before you build the inbound message:

ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);

primeCache(req);           // <-- add this
logInbound(req, corrId);   // now bodyString(req) will have content

chain.doFilter(req, res);
logOutbound(req, res, corrId, tookMs);
res.copyBodyToResponse();

Body helpers (unchanged)

private static String bodyString(ContentCachingRequestWrapper req) {
  byte[] buf = req.getContentAsByteArray();
  if (buf == null || buf.length == 0) return "<no body>";
  Charset cs = Optional.ofNullable(req.getCharacterEncoding())
      .map(Charset::forName).orElse(StandardCharsets.UTF_8);
  return new String(buf, cs);
}

Notes / gotchas
	•	This is safe: you’re reading from the wrapper’s stream, not the raw request; downstream code can still read the body because the wrapper serves it from the cached bytes.
	•	For form posts (application/x-www-form-urlencoded), Spring populates parameters and often never touches the raw InputStream. Calling getParameterMap() forces the wrapper to cache them.
	•	For multipart or large/binary bodies, you may want to skip or truncate. Example:

boolean printable = List.of("application/json","application/xml","text/plain",
                            "application/x-www-form-urlencoded")
                        .contains(Optional.ofNullable(req.getContentType()).orElse(""));


	•	Ensure your filter runs before others that might consume the stream: @Order(Ordered.HIGHEST_PRECEDENCE).

With this small primeCache(...) step, your inbound payload will be populated and printed correctly.