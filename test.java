@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain chain)
    throws IOException, ServletException {

    ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);

    String corrId = Optional.ofNullable(req.getHeader("CorrelationId"))
        .orElse(UUID.randomUUID().toString());
    MDC.put("CorrelationId", corrId);

    long startMs = System.currentTimeMillis();

    // 1️⃣ Log inbound immediately (before controller executes)
    logInbound(req, corrId);

    try {
        chain.doFilter(req, res); // invoke controller, services, etc.
    } finally {
        // 2️⃣ Log outbound after completion
        logOutbound(req, res, corrId, System.currentTimeMillis() - startMs);
        MDC.clear();
        res.copyBodyToResponse();
    }
}

private void logInbound(ContentCachingRequestWrapper req, String corrId) {
    String inbound = new StringBuilder()
        .append("\n================ INBOUND MESSAGE ================\n")
        .append("Address: ").append(req.getRequestURI()).append('\n')
        .append("Content-Type: ").append(nullToEmpty(req.getContentType())).append('\n')
        .append("ExchangeId: ").append(corrId).append('\n')
        .append("Headers: ").append(headersMap(req)).append('\n')
        .append("Payload: ").append(maskJwt(bodyString(req))).append('\n')
        .append("=================================================\n")
        .toString();
    log.info(inbound);
}

private void logOutbound(ContentCachingRequestWrapper req,
                         ContentCachingResponseWrapper res,
                         String corrId,
                         long tookMs) {
    String outbound = new StringBuilder()
        .append("\n================ OUTBOUND MESSAGE ===============\n")
        .append("Address: ").append(req.getRequestURI()).append('\n')
        .append("Content-Type: ").append(nullToEmpty(res.getContentType())).append('\n')
        .append("ResponseCode: ").append(res.getStatus()).append('\n')
        .append("ExchangeId: ").append(corrId).append('\n')
        .append("Time Taken (millis): ").append(tookMs).append('\n')
        .append("Headers: ").append(headersMap(res)).append('\n')
        .append("Payload: ").append(maskJwt(bodyString(res))).append('\n')
        .append("=================================================\n")
        .toString();
    log.info(outbound);
}