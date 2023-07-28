package io.quarkus.arc.test.interceptors

import io.quarkus.arc.Arc
import io.quarkus.arc.test.ArcTestContainer
import jakarta.annotation.Priority
import jakarta.inject.Singleton
import jakarta.interceptor.AroundInvoke
import jakarta.interceptor.Interceptor
import jakarta.interceptor.InterceptorBinding
import jakarta.interceptor.InvocationContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.assertEquals

class InterceptableNonsuspendMethodTest {
    companion object {
        @RegisterExtension
        val container = ArcTestContainer.builder()
                .beanClasses(MyInterceptorBinding::class.java, MyInterceptor::class.java, MyService::class.java)
                .transformUnproxyableClasses(true)
                .build()
    }

    @Test
    fun test() {
        val service = Arc.container().instance(MyService::class.java).get()
        assertEquals("intercepted: hello", service.hello())
    }

    @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    @InterceptorBinding
    annotation class MyInterceptorBinding

    @MyInterceptorBinding
    @Priority(1)
    @Interceptor
    class MyInterceptor {
        @AroundInvoke
        fun intercept(ctx: InvocationContext): Any {
            return "intercepted: ${ctx.proceed()}"
        }
    }

    // this class and the method are intentionally not `open` to trigger unproxyable classes transformation
    @Singleton
    class MyService {
        @MyInterceptorBinding
        fun hello(): String {
            return "hello"
        }
    }
}
