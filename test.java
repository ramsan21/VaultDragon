Yep—there are a few cleaner ways to log request + response (with bodies) in Spring Boot without fighting a null request body in your filter.

Option A (easiest): Use Zalando Logbook

It’s a drop-in HTTP logger that captures method, URI, headers, and bodies (with masking) for both request and response.
	1.	Add the starter

<!-- pom.xml -->
<dependency>
  <groupId>org.zalando</groupId>
  <artifactId>logbook-spring-boot-starter</artifactId>
  <version>3.9.0</version>
</dependency>

	2.	Minimal config (application.yml)

logbook:
  format:
    style: http # or json
  obfuscate:
    headers: [Authorization, X-API-KEY]
  predicate:
    exclude:
      - path: /actuator/health
      - path: /pgp/health
logging:
  level:
    org.zalando.logbook: INFO

That’s it—Logbook registers a filter internally and logs in/out bodies safely. You can plug in your own formatter/writer later if you want your custom message style.

⸻

Option B: AOP around controllers (no raw InputStream)

If your controllers use @RequestBody and return a body, you can log the already-deserialized objects—no wrapper tricks.

@Aspect
@Component
@Slf4j
public class ControllerLoggingAspect {

  @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
  public void restControllers() {}

  @Around("restControllers()")
  public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
    String controller = pjp.getSignature().getDeclaringTypeName();
    String method = pjp.getSignature().getName();

    // Inbound
    Object[] args = pjp.getArgs();
    log.info("Inbound -> {}.{} args={}", controller, method, safeJson(args));

    long t0 = System.currentTimeMillis();
    Object result = pjp.proceed();
    long dt = System.currentTimeMillis() - t0;

    // Outbound
    log.info("Outbound <- {}.{} took={}ms body={}", controller, method, dt, safeJson(result));
    return result;
  }

  private String safeJson(Object o) {
    try { return new ObjectMapper().writeValueAsString(o); }
    catch (Exception e) { return String.valueOf(o); }
  }
}

Pros: dead simple, no request-body-null issues.
Cons: you won’t see raw bytes (useful for multipart/files); you see mapped objects.

⸻

Option C: Keep your Filter, but fix the “null body”

If you prefer a filter, the null happens because the request InputStream can be read only once. Use Spring’s caching wrappers and read them after the controller runs.

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // run early
public class HttpLogFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain chain) throws IOException, ServletException {

    ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);

    // 1) DO NOT read req body here – it's still empty.
    chain.doFilter(req, res); // controller consumes and caching collects

    // 2) AFTER chain: bodies are available
    byte[] reqBody = req.getContentAsByteArray();
    byte[] resBody = res.getContentAsByteArray();

    String reqPayload = new String(reqBody, req.getCharacterEncoding() != null ? req.getCharacterEncoding() : StandardCharsets.UTF_8.name());
    String resPayload = new String(resBody, res.getCharacterEncoding() != null ? res.getCharacterEncoding() : StandardCharsets.UTF_8.name());

    log.info("Inbound {} {} payload={}", req.getMethod(), req.getRequestURI(), truncate(mask(reqPayload)));
    log.info("Outbound status={} payload={}", res.getStatus(), truncate(mask(resPayload)));

    // 3) IMPORTANT: write body back to client
    res.copyBodyToResponse();
  }

  private String mask(String s) { /* redact JWTs, etc. */ return s; }
  private String truncate(String s) { return s.length() > 5000 ? s.substring(0,5000)+"…": s; }
}

Key tips to avoid “null”:
	•	Wrap with ContentCachingRequestWrapper/ResponseWrapper.
	•	Log after chain.doFilter(...).
	•	Always call res.copyBodyToResponse() at the end.
	•	Make sure no earlier filter already consumed the stream without caching.
	•	For large/multipart uploads, consider skipping body logging.

⸻

Which should you pick?
	•	Want a zero-friction solution with proper masking? → Logbook (Option A).
	•	Want to log business objects, not raw bytes? → AOP (Option B).
	•	Want full control and minimal deps? → Fixed Filter (Option C).

If you tell me your preference (A/B/C), I’ll drop in a ready-to-paste config tailored to your endpoints (e.g., exclude /health, mask JWT headers, cap size, etc.).