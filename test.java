package <your.package>;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.AnnotationUtils;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

// imports for your app types
// import <your>.WSThreadSharedObj;
// import <your>.WSMethod;
// import <your>.Event;
// import <your>.UserVO;
// import <your>.responses.*; // BaseResponseMessage, ResponseMessage, MapResponse, etc.

public class WSMethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WSMethodInterceptor.class);

    public void beforeAdvice(JoinPoint joinPoint) {
        // start time
        Instant start = Instant.now();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // annotation lookup (unchanged behavior)
        WSMethod wsAnnotation = AnnotationUtils.findAnnotation(method, WSMethod.class);
        WSThreadSharedObj threadObj = WSThreadSharedObj.get();

        if (wsAnnotation != null) {
            Event event = (Event) AnnotationUtils.getValue(wsAnnotation, "eventType");
            threadObj.setEvent(event);
        }

        // threadObj fields used by your logs
        threadObj.setClassName(signature.getDeclaringType().getSimpleName());
        threadObj.setMethodName(method.getName());
        threadObj.put("startTime", start);

        // —— MDC population (Strings only) ——
        MDC.put("api_name", getAPIName(method));

        UserVO userVO = getUser(joinPoint);
        Optional.ofNullable(userVO).map(UserVO::getGroupId).map(String::valueOf).ifPresent(v -> MDC.put("group_id", v));
        Optional.ofNullable(userVO).map(UserVO::getUserId).map(String::valueOf).ifPresent(v -> MDC.put("user_id", v));
        Optional.ofNullable(userVO).map(UserVO::getLoginId).map(String::valueOf).ifPresent(v -> MDC.put("login_id", v));

        logger.info("***BEGIN {}.{}***", threadObj.getClassName(), threadObj.getMethodName());
    }

    public void afterAdvice(JoinPoint joinPoint, Object returnValue) {
        WSThreadSharedObj threadObj = WSThreadSharedObj.get();

        try {
            // status for logs/MDC (always String)
            String status = getStatus(returnValue);
            if (status != null) {
                MDC.put("status", status);
            }

            logger.info("***END {}.{}***", threadObj.getClassName(), threadObj.getMethodName());

            // duration in ms (pure java.time)
            Instant startTime = threadObj.get("startTime");
            if (startTime != null) {
                long tookMs = Duration.between(startTime, Instant.now()).toMillis();
                logger.info("Total Time(ms) taken for {}.{} execution: {}",
                        threadObj.getClassName(), threadObj.getMethodName(), tookMs);
            }
        } finally {
            // always clean up
            WSThreadSharedObj.clean();
            MDC.clear();
        }
    }

    // ---------- helpers ----------

    private String getAPIName(Method method) {
        if (method == null) {
            logger.error("Method is null while trying to get API name.");
            return "Unknown";
        }
        Path path = AnnotationUtils.findAnnotation(method, Path.class);
        if (path != null && path.value() != null) {
            return path.value();
        }
        return method.getName();
    }

    private UserVO getUser(JoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(a -> a instanceof UserVO)
                .map(UserVO.class::cast)
                .findFirst()
                .orElse(null);
    }

    /**
     * Normalize status as a String for MDC/logs without coupling to CXF internals.
     */
    private String getStatus(Object returnValue) {
        Object response = returnValue;

        // unwrap JAX-RS Response
        if (returnValue instanceof Response) {
            response = ((Response) returnValue).getEntity();
        }

        if (response == null) return null;

        // Your app's response types:
        if (response instanceof BaseResponseMessage) {
            return String.valueOf(((BaseResponseMessage) response).getStatusCode());
        }
        if (response instanceof ResponseMessage) {
            String s = ((ResponseMessage) response).getResponse();
            return (s != null && !s.isEmpty()) ? s : null;
        }
        if (response instanceof TokenImportResponse) {
            return String.valueOf(((TokenImportResponse) response).getStatus());
        }
        if (response instanceof OnboardSomeMessage) {
            return String.valueOf(((OnboardSomeMessage) response).getStatusCode());
        }
        if (response instanceof MapResponse) {
            Object statusCode = ((MapResponse) response).get("statusCode");
            return String.valueOf(Optional.ofNullable(statusCode).orElse(""));
        }
        // add other cases as needed
        return null;
    }
}