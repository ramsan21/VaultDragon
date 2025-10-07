@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE) // run after other filters so status/headers are final
public class PrettyApiLoggingFilter extends OncePerRequestFilter {

  private static final Set<String> SKIP_PATHS = Set.of("/actuator/health", "/health");

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain chain) throws IOException, ServletException {

    // Wrap so we can read bodies *after* controller executes
    ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);

    // Correlation id (like your MDC usage)
    String corrId = Optional.ofNullable(req.getHeader("CorrelationId"))
        .orElse(UUID.randomUUID().toString());
    MDC.put("CorrelationId", corrId);

    long startMs = System.currentTimeMillis();
    try {
      chain.doFilter(req, res);
    } finally {
      try {
        if (!shouldSkip(req)) {
          long tookMs = System.currentTimeMillis() - startMs;

          // ===== Inbound (REQUEST) =====
          String inbound = new StringBuilder()
              .append("Inbound Message (SecureLoggingInInterceptor)\n")
              .append("--------------------------------------------------------------------------------\n")
              .append("Address: ").append(req.getRequestURI()).append('\n')
              .append("Content-Type: ").append(nullToEmpty(req.getContentType())).append('\n')
              .append("ExchangeId: ").append(corrId).append('\n')
              .append("Time Taken (millis): ").append(tookMs).append('\n')
              .append("Headers: ").append(headersMap(req)).append('\n')
              .append("Payload: ").append(maskJwt(bodyString(req))).append('\n')
              .toString();

          // ===== Outbound (RESPONSE) =====
          String outbound = new StringBuilder()
              .append("Outbound Message (SecureLoggingOutInterceptor)\n")
              .append("--------------------------------------------------------------------------------\n")
              .append("Content-Type: ").append(nullToEmpty(res.getContentType())).append('\n')
              .append("ResponseCode: ").append(res.getStatus()).append('\n')
              .append("ExchangeId: ").append(corrId).append('\n')
              .append("Time Taken (millis): ").append(tookMs).append('\n')
              .append("Headers: ").append(headersMap(res)).append('\n')
              .append("Payload: ").append(maskJwt(bodyString(res))).append('\n')
              .toString();

          // print separately to make it obvious which phase you’re looking at
          log.info(inbound);
          log.info(outbound);
        }
      } finally {
        MDC.clear();
        // IMPORTANT: write cached response body back to client
        res.copyBodyToResponse();
      }
    }
  }

  private static boolean shouldSkip(HttpServletRequest req) {
    String p = req.getServletPath();
    return SKIP_PATHS.stream().anyMatch(p::startsWith);
  }

  private static String nullToEmpty(String s) { return s == null ? "" : s; }

  private static String bodyString(ContentCachingRequestWrapper req) {
    byte[] buf = req.getContentAsByteArray();
    if (buf == null || buf.length == 0) return "<no body>";
    try { return new String(buf, req.getCharacterEncoding() != null ? req.getCharacterEncoding() : StandardCharsets.UTF_8); }
    catch (Exception e) { return "<unreadable>"; }
  }

  private static String bodyString(ContentCachingResponseWrapper res) {
    byte[] buf = res.getContentAsByteArray();
    if (buf == null || buf.length == 0) return "<no body>";
    try { return new String(buf, res.getCharacterEncoding() != null ? res.getCharacterEncoding() : StandardCharsets.UTF_8); }
    catch (Exception e) { return "<unreadable>"; }
  }

  private static Map<String, Object> headersMap(HttpServletRequest req) {
    Map<String, Object> m = new LinkedHashMap<>();
    Enumeration<String> names = req.getHeaderNames();
    while (names.hasMoreElements()) {
      String n = names.nextElement();
      m.put(n, Collections.list(req.getHeaders(n)));
    }
    return m;
  }

  private static Map<String, Object> headersMap(HttpServletResponse res) {
    Map<String, Object> m = new LinkedHashMap<>();
    for (String n : res.getHeaderNames()) {
      m.put(n, new ArrayList<>(res.getHeaders(n)));
    }
    return m;
  }

  // mimic your SecureLogEventSender masking
  private static String maskJwt(String s) {
    if (s == null) return null;
    // mask common token fields/headers
    s = s.replaceAll("(?i)(\"?(jwt|token|authorization)\"?\\s*[:=]\\s*\")([^\"]+)(\")", "$1***$4");
    s = s.replaceAll("(?i)(Bearer)\\s+[A-Za-z0-9-_\\.]+", "$1 ***");
    return s;
  }
}