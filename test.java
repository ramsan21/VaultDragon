import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WSMethodInterceptorTest {

    // SUT
    private final WSMethodInterceptor interceptor = new WSMethodInterceptor();

    @Mock JoinPoint joinPoint;
    @Mock MethodSignature methodSignature;
    @Mock UserVO userVO;

    /** A real method with a real @WSMethod annotation for Spring to read */
    static class AnnotatedTarget {
        @WSMethod(eventType = Event.DUMMY)
        public void someBusinessMethod() {}
    }

    @Test
    void testBeforeAdvice_usesRealAnnotationAndPopulatesMdc() throws Exception {
        // Arrange: real annotated method
        Method method = AnnotatedTarget.class.getMethod("someBusinessMethod");

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);

        // target object is needed for class name resolution
        AnnotatedTarget target = new AnnotatedTarget();
        when(joinPoint.getTarget()).thenReturn(target);

        // interceptor also inspects args to extract the UserVO
        when(joinPoint.getArgs()).thenReturn(new Object[] { userVO });

        when(userVO.getGroupId()).thenReturn("group1");
        when(userVO.getUserId()).thenReturn("user1");
        when(userVO.getLoginId()).thenReturn("login1");

        // Act
        interceptor.beforeAdvice(joinPoint);

        // Assert: MDC populated from UserVO and method/class
        assertEquals("group1", MDC.get("group_id"));
        assertEquals("user1",  MDC.get("user_id"));
        assertEquals("login1", MDC.get("login_id"));
        assertEquals("someBusinessMethod", MDC.get("api_name")); // from getAPIName(signature.getMethod())

        // (Optional) if you expose thread object, you can assert its event etc.
        // WSThreadSharedObj threadObj = WSThreadSharedObj.get();
        // assertEquals(Event.DUMMY, threadObj.getEvent());
        // assertEquals(ClassUtils.getShortName(target.getClass()), threadObj.getClassName());
        // assertEquals("someBusinessMethod", threadObj.getMethodName());
    }
}