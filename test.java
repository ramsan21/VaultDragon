Awesome—here’s a copy-paste ready setup to add a Correlation ID end-to-end using Zalando Logbook in a Spring Boot app.

⸻

1) pom.xml (add Logbook)

<dependencies>
  <!-- … your other deps … -->

  <!-- Zalando Logbook: HTTP request/response logging -->
  <dependency>
    <groupId>org.zalando</groupId>
    <artifactId>logbook-spring-boot-starter</artifactId>
    <version>3.9.0</version>
  </dependency>
</dependencies>


⸻

2) application.yml (logging + logbook config)

# --- Log patterns: add CorrelationId from MDC to every line ---
logging:
  pattern:
    file:    "%-5p %d{ISO8601} [%t:%c{1}] [%X{CorrelationId}] - %msg%n"
    console: "%d{yyyy-MM-dd HH:mm:ss} [%t:%c{1}] [%X{CorrelationId}] - %msg%n"
  level:
    org.zalando.logbook: INFO

# --- Logbook: capture request/response with masking and exclusions ---
logbook:
  format:
    style: http           # or "json"
  predicate:
    exclude:
      - path: /actuator/health
      - path: /health
      - path: /pgp/health
  obfuscate:
    headers:
      - Authorization
      - X-API-KEY
  body-filter:
    json-path:
      - "$.password": "***"
      - "$.token": "***"

You’ll see Logbook’s inbound/outbound logs, and every other log line (your own log.info(...)) will show the same [CorrelationId] tag from MDC.

⸻

3) Code: Correlation ID filter (one per request)

package com.example.logging;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)  // run before Logbook's filter
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HDR = "X-Correlation-Id";  // use your preferred name
    public static final String MDC_KEY = "CorrelationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String cid = Optional.ofNullable(request.getHeader(HDR))
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString());

        // Put into MDC so it appears in ALL logs
        MDC.put(MDC_KEY, cid);

        // Echo back to the client and make it visible in Logbook headers
        response.setHeader(HDR, cid);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}

Ordering matters: keeping this at highest precedence ensures the header is present before Logbook runs, so Logbook prints it on the request/response lines too.

⸻

4) (Optional but recommended) Propagate the ID on outbound calls

RestTemplate

package com.example.logging;

import org.slf4j.MDC;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder b) {
        RestTemplate rt = b.build();
        rt.getInterceptors().add((req, body, exec) -> {
            String cid = MDC.get("CorrelationId");
            if (cid != null) {
                req.getHeaders().set("X-Correlation-Id", cid);
            }
            return exec.execute(req, body);
        });
        return rt;
    }
}

WebClient

package com.example.logging;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
            .filter(correlationFilter())
            .build();
    }

    private ExchangeFilterFunction correlationFilter() {
        return (request, next) -> {
            String cid = MDC.get("CorrelationId");
            ClientRequest mutated = ClientRequest.from(request)
                .headers(h -> { if (cid != null) h.set("X-Correlation-Id", cid); })
                .build();
            return next.exchange(mutated);
        };
    }
}


⸻

5) (Optional) Keep Logbook output tidy

If you want Logbook to clearly show the header, you already have it because the filter sets the header before Logbook runs. If you ever want to change its style, you can add a tiny formatter bean:

package com.example.logging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.*;

@Configuration
public class LogbookFormatConfig {

    @Bean
    public HttpLogFormatter httpLogFormatter() {
        // Prepend section titles; otherwise default formatting is fine
        return new DefaultHttpLogFormatter() {
            @Override
            public String format(Precorrelation p, HttpRequest req) {
                return "Inbound Message\n" + super.format(p, req);
            }
            @Override
            public String format(Correlation c, HttpResponse res) {
                return "Outbound Message\n" + super.format(c, res);
            }
        };
    }
}

(You can omit this—default Logbook formatting is already clean.)

⸻

Recap
	•	POM: add logbook-spring-boot-starter.
	•	YAML: add log patterns with %X{CorrelationId} and a small logbook: block.
	•	Filter: set/echo X-Correlation-Id and put it in MDC.
	•	Outbound: propagate to RestTemplate/WebClient if you call other services.

This gives you one unique Correlation ID tied to the entire request lifecycle, visible in:
	•	Every line of your application logs,
	•	Logbook’s request/response entries,
	•	Response headers returned to clients,
	•	Downstream calls (if you enable propagation).