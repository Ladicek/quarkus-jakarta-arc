package io.quarkus.arc.test.observers.inheritance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.Arc;
import io.quarkus.arc.test.ArcTestContainer;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 *         <br>
 *         Date: 18/07/2019
 */
public class ObserverInheritanceTest {

    @RegisterExtension
    public static ArcTestContainer container = new ArcTestContainer(ObservingBean.THIS.class, EmittingBean.class,
            ObservingBean.class,
            ObservingSubBean.class,
            NonObservingSubBean.class);

    ObservingBean observingBean;
    ObservingSubBean observingSubBean;
    NonObservingSubBean nonObservingSubBean;

    EmittingBean emittingBean;

    @SuppressWarnings("serial")
    @BeforeEach
    public void setUp() {
        observingBean = Arc.container().instance(ObservingBean.class, new ObservingBean.THIS.Literal()).get();
        observingSubBean = Arc.container().instance(ObservingSubBean.class).get();
        nonObservingSubBean = Arc.container().instance(NonObservingSubBean.class).get();
        emittingBean = Arc.container().instance(EmittingBean.class).get();
    }

    @Test
    public void observesShouldBeFired() {
        emittingBean.trigger();
        assertThat(observingBean.getValue()).isNotNull().isEqualTo(EmittingBean.VALUE);
    }

    @Test
    public void observesShouldBeFiredInSubclass() {
        emittingBean.trigger();
        assertThat(observingSubBean.getValue()).isNotNull().isEqualTo(EmittingBean.VALUE);
    }

    @Test
    public void observesShouldNotBeFiredInSubclassIfOverriden() {
        emittingBean.trigger();
        assertThat(nonObservingSubBean.getValue()).isNull();
    }
}
