Great—since you’ve chosen Approach C (Filter with ContentCaching*), the fix is mainly about when you read the body.

Right now you call logInbound(req, corrId); before chain.doFilter(req, res);. At that moment the body cache is still empty, so req.getContentAsByteArray() returns []. You must log the request body after the chain runs (controller has consumed the stream and the wrapper has cached it). You can still log “inbound headers/line” before the chain if you want them to appear first.

Here’s a ready-to-paste version showing the exact changes:

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // ensure it wraps early
@Slf4j
public class RequestFilter extends OncePerRequestFilter {

    private static final Set<String> SKIP_PATHS = Set.of("/pgp/health", "/health");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);

        String corrId = Optional.ofNullable(req.getHeader("CorrelationId"))
                .orElse(UUID.randomUUID().toString());
        MDC.put("CorrelationId", corrId);

        long startMs = System.currentTimeMillis();

        try {
            // (A) If you want an "Inbound" line at the top, log only line+headers here (NO BODY)
            if (!shouldSkip(req)) {
                logInboundHeadersOnly(req, corrId);
            }

            // Let controller/filters read the body; wrappers will cache it
            chain.doFilter(req, res);

            // (B) Now the caches are populated -> log bodies
            if (!shouldSkip(req)) {
                logInboundBody(req, corrId); // <-- moved AFTER chain
                logOutbound(res, corrId, System.currentTimeMillis() - startMs);
            }
        } finally {
            MDC.clear();
            // IMPORTANT: write cached body back to client
            res.copyBodyToResponse();
        }
    }

    private boolean shouldSkip(HttpServletRequest req) {
        String path = req.getRequestURI();
        return SKIP_PATHS.contains(path);
    }

    /** Header/line only; safe to run before chain */
    private void logInboundHeadersOnly(ContentCachingRequestWrapper req, String corrId) {
        StringBuilder sb = new StringBuilder();
        sb.append("Inbound Message\n");
        write(sb, "Address", req.getRequestURI());
        write(sb, "HttpMethod", req.getMethod());
        write(sb, "Content-Type", nullToEmpty(req.getContentType()));
        write(sb, "ExchangeId", corrId);
        write(sb, "Headers", formatHeaders(req));
        log.info(sb.toString());
    }

    /** Body must be logged AFTER chain.doFilter */
    private void logInboundBody(ContentCachingRequestWrapper req, String corrId) throws IOException {
        if (!isLoggableContentType(req.getContentType())) return;

        String body = toBody(req.getContentAsByteArray(),
                Optional.ofNullable(req.getCharacterEncoding()).orElse(StandardCharsets.UTF_8.name()));

        if (!body.isBlank()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Inbound Body\n");
            write(sb, "ExchangeId", corrId);
            write(sb, "Payload", mask(truncate(body)));
            log.info(sb.toString());
        }
    }

    private void logOutbound(ContentCachingResponseWrapper res, String corrId, long tookMs) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Outbound Message\n");
        write(sb, "Status", String.valueOf(res.getStatus()));
        write(sb, "ExchangeId", corrId);
        write(sb, "TimeTaken(ms)", String.valueOf(tookMs));

        if (isLoggableContentType(res.getContentType())) {
            String body = toBody(res.getContentAsByteArray(),
                    Optional.ofNullable(res.getCharacterEncoding()).orElse(StandardCharsets.UTF_8.name()));
            if (!body.isBlank()) {
                write(sb, "Payload", mask(truncate(body)));
            }
        }
        log.info(sb.toString());
    }

    // ---- helpers ----

    private boolean isLoggableContentType(String ct) {
        if (ct == null) return false;
        String c = ct.toLowerCase();
        return c.contains("application/json") || c.contains("text/") || c.contains("xml") || c.contains("form-urlencoded");
        // skip multipart/*, octet-stream, etc.
    }

    private String toBody(byte[] bytes, String charset) {
        try { return new String(bytes, charset); }
        catch (Exception e) { return new String(bytes, StandardCharsets.UTF_8); }
    }

    private String mask(String s) {
        // redact obvious secrets (JWT, passwords). Example:
        return s.replaceAll("(\"authorization\"\\s*:\\s*\")([^\"]+)(\")", "$1***$3");
    }

    private String truncate(String s) {
        int max = 5000; // cap to avoid huge logs
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    private String formatHeaders(HttpServletRequest req) {
        Map<String, String> map = Collections.list(req.getHeaderNames()).stream()
                .collect(Collectors.toMap(h -> h, req::getHeader));
        // redact sensitive headers
        map.replaceAll((k, v) -> k.equalsIgnoreCase("authorization") ? "***" : v);
        return map.toString();
    }

    private void write(StringBuilder sb, String key, String value) {
        sb.append(key).append(": ").append(value == null ? "" : value).append('\n');
    }

    private String nullToEmpty(String s) { return s == null ? "" : s; }
}

What changed vs your screenshot
	•	Removed the pre-chain logInbound(req, corrId) that tried to read the body too early.
	•	Added logInboundHeadersOnly(...) before the chain (so you still get an “Inbound” line at the top).
	•	Added logInboundBody(...) after the chain (this time the body is available).
	•	Kept res.copyBodyToResponse() in finally.
	•	Guarded by isLoggableContentType and length caps; masked sensitive data.

If you don’t need the “inbound headers first” line, you can simply remove logInboundHeadersOnly and keep only the two post-chain calls.

If you want, I can adapt the masking (e.g., skip /actuator/**, hide query params, drop large multipart uploads) or change the log format to exactly match your previous SecureSlf4jEventSender message style.