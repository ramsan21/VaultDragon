Your test is failing because the filter:
	1.	wraps the request/response (ContentCachingRequestWrapper / ContentCachingResponseWrapper) and
	2.	reads headers/body that your mocks don’t stub, then
	3.	calls filterChain.doFilter(..) with wrapped objects (not your original mocks).

So the usual verify(filterChain).doFilter(request, response) fails, and NPEs appear when the filter asks your mocks for header names, encodings, etc.

Here are two reliable ways to fix it. Pick one.

⸻

A) Use Spring mock objects (simplest & closest to real runtime)

@ExtendWith(MockitoExtension.class)
class PrettyApiLoggingFilterTest {

  private final PrettyApiLoggingFilter filter = new PrettyApiLoggingFilter();

  @Test
  void logsAndPassesThrough() throws Exception {
    // given a real-ish request/response
    MockHttpServletRequest req = new MockHttpServletRequest("POST", "/test");
    req.setContentType("application/json");
    req.setCharacterEncoding("UTF-8");
    req.addHeader("X-Service-JWT", "token");
    req.setContent("{\"hello\":\"world\"}".getBytes(StandardCharsets.UTF_8));

    MockHttpServletResponse res = new MockHttpServletResponse();

    // and a chain that "uses" the body & writes a response
    FilterChain chain = (servletReq, servletRes) -> {
      // simulate controller reading request (so ContentCachingRequestWrapper captures it)
      IOUtils.copy(servletReq.getInputStream(), new ByteArrayOutputStream());
      servletRes.setContentType("application/json");
      servletRes.getWriter().write("{\"ok\":true}");
      ((HttpServletResponse) servletRes).setStatus(200);
    };

    // when
    filter.doFilter(req, res, chain); // call OncePerRequestFilter#doFilter, not doFilterInternal

    // then
    assertEquals(200, res.getStatus());
    assertEquals("application/json", res.getContentType());
    assertEquals("{\"ok\":true}", res.getContentAsString());
    // you can also assert on logs with a LogCaptor if you want
  }
}

Notes:
	•	Call filter.doFilter(...) (the framework entrypoint).
	•	The chain must read the request body; otherwise the caching wrapper won’t capture it.
	•	No Mockito stubbing headaches.

⸻

B) Keep Mockito mocks, but stub everything the filter uses and verify with any(...)

@ExtendWith(MockitoExtension.class)
class PrettyApiLoggingFilterTest {

  @Mock HttpServletRequest request;
  @Mock HttpServletResponse response;
  @Mock FilterChain filterChain;

  private final PrettyApiLoggingFilter filter = new PrettyApiLoggingFilter();

  @Test
  void doFilterInternal_formats_and_calls_chain() throws Exception {
    // request stubs used by the filter
    when(request.getServletPath()).thenReturn("/test");
    when(request.getRequestURI()).thenReturn("/test");
    when(request.getMethod()).thenReturn("GET");
    when(request.getContentType()).thenReturn("application/json");
    when(request.getCharacterEncoding()).thenReturn("UTF-8");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(request.getHeader("CorrelationId")).thenReturn(null);

    // headers enumeration
    when(request.getHeaderNames()).thenReturn(
        Collections.enumeration(List.of("X-Service-JWT")));
    when(request.getHeaders("X-Service-JWT")).thenReturn(
        Collections.enumeration(List.of("token")));

    // empty body for GET
    when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream(new byte[0])));

    // response stubs used by the filter
    when(response.getStatus()).thenReturn(200);
    when(response.getHeaderNames()).thenReturn(Set.of("Content-Type"));
    when(response.getHeaders("Content-Type")).thenReturn(List.of("application/json"));
    when(response.getContentType()).thenReturn("application/json");
    ServletOutputStream out = new ServletOutputStream() {
      @Override public boolean isReady() { return true; }
      @Override public void setWriteListener(WriteListener writeListener) {}
      @Override public void write(int b) {}
    };
    when(response.getOutputStream()).thenReturn(out);

    // exercise (call framework method)
    filter.doFilter(request, response, filterChain);

    // verify chain called with WRAPPED args, not the raw mocks
    verify(filterChain).doFilter(
        any(HttpServletRequest.class),
        any(HttpServletResponse.class));
  }
}

Key fixes:
	•	Stub getHeaderNames(), getHeaders(name), getRequestURI(), getServletPath(), getCharacterEncoding(), getOutputStream(), etc.
	•	Verify with any(HttpServletRequest.class) / any(HttpServletResponse.class) because your filter passes wrappers to the chain.
	•	Prefer filter.doFilter(...) over calling doFilterInternal(...) directly.

⸻

Common pitfalls checklist
	•	✅ Call filter.doFilter(...), not doFilterInternal(...).
	•	✅ If you need request/response bodies in logs, make the chain read the request body and write the response body.
	•	✅ When using Mockito, verify with any(...) because of wrappers.
	•	✅ Stub getHeaderNames() and getHeaders(name) (NPE otherwise).
	•	✅ Provide getCharacterEncoding() and getOutputStream() stubs on the response.
	•	✅ If you added MDC, don’t forget to clear it in the filter (already in the sample).

If you paste the exact failure message/stacktrace, I’ll tailor the minimal stubs needed for your case.