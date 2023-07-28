package io.quarkus.arc.test.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.annotation.Priority;
import jakarta.inject.Singleton;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InterceptorBinding;
import jakarta.interceptor.InvocationContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.Arc;
import io.quarkus.arc.test.ArcTestContainer;

public class InterceptionWithBytecodeTransformationTest {
    @RegisterExtension
    public static ArcTestContainer container = ArcTestContainer.builder()
            .beanClasses(MyInterceptorBinding.class, MyInterceptor.class, MyService.class)
            .transformUnproxyableClasses(true)
            .build();

    @Test
    public void test() {
        MyService service = Arc.container().instance(MyService.class).get();
        assertEquals("intercepted: hello", service.hello());
    }

    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @InterceptorBinding
    @interface MyInterceptorBinding {
    }

    @MyInterceptorBinding
    @Priority(1)
    @Interceptor
    static class MyInterceptor {
        @AroundInvoke
        Object intercept(InvocationContext ctx) throws Exception {
            return "intercepted: " + ctx.proceed();
        }
    }

    // this class and the method are intentionally `final` to trigger unproxyable class transformation
    @Singleton
    static final class MyService {
        @MyInterceptorBinding
        final String hello() {
            return "hello";
        }
    }
}
