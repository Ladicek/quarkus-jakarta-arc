package io.quarkus.arc.test.cdi.lite.ext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.arc.test.ArcTestContainer;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import javax.enterprise.inject.build.compatible.spi.Parameters;
import javax.enterprise.inject.build.compatible.spi.Synthesis;
import javax.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import javax.enterprise.inject.build.compatible.spi.SyntheticBeanDisposer;
import javax.enterprise.inject.build.compatible.spi.SyntheticComponents;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

// TODO migrated to CDI TCK
public class SyntheticBeanInjectionPointTest {
    @RegisterExtension
    public ArcTestContainer container = ArcTestContainer.builder()
            .additionalClasses(MyDependentBean.class, MySingletonBean.class)
            .buildCompatibleExtensions(MyExtension.class)
            .build();

    @Test
    public void test() {
        InstanceHandle<MyDependentBean> handle = Arc.container().select(MyDependentBean.class).getHandle();
        try {
            handle.get();
        } catch (Exception e) {
            fail();
        }
        assertNotNull(MyDependentBeanCreator.lookedUp);

        try {
            handle.destroy();
        } catch (Exception ignored) {
        }
        assertNull(MyDependentBeanDisposer.lookedUp);

        try {
            Arc.container().select(MySingletonBean.class).get();
        } catch (Exception ignored) {
        }
        assertNull(MySingletonBeanCreator.lookedUp);
    }

    public static class MyExtension implements BuildCompatibleExtension {
        @Synthesis
        public void synthesise(SyntheticComponents syn) {
            syn.addBean(MyDependentBean.class)
                    .type(MyDependentBean.class)
                    .scope(Dependent.class)
                    .createWith(MyDependentBeanCreator.class)
                    .disposeWith(MyDependentBeanDisposer.class);

            syn.addBean(MySingletonBean.class)
                    .type(MySingletonBean.class)
                    .scope(Singleton.class)
                    .createWith(MySingletonBeanCreator.class);
        }
    }

    // ---

    static class MyDependentBean {
    }

    static class MySingletonBean {
    }

    public static class MyDependentBeanCreator implements SyntheticBeanCreator<MyDependentBean> {
        static InjectionPoint lookedUp = null;

        @Override
        public MyDependentBean create(Instance<Object> lookup, Parameters params) {
            lookedUp = lookup.select(InjectionPoint.class).get();
            return new MyDependentBean();
        }
    }

    public static class MyDependentBeanDisposer implements SyntheticBeanDisposer<MyDependentBean> {
        static InjectionPoint lookedUp = null;

        @Override
        public void dispose(MyDependentBean instance, Instance<Object> lookup, Parameters params) {
            lookedUp = lookup.select(InjectionPoint.class).get();
        }
    }

    public static class MySingletonBeanCreator implements SyntheticBeanCreator<MySingletonBean> {
        static InjectionPoint lookedUp = null;

        @Override
        public MySingletonBean create(Instance<Object> lookup, Parameters params) {
            lookedUp = lookup.select(InjectionPoint.class).get();
            return new MySingletonBean();
        }
    }
}
