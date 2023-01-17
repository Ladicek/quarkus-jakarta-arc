package io.quarkus.arc.arquillian;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class ArcExtension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(DeployableContainer.class, ArcDeployableContainer.class);
        builder.service(Protocol.class, ArcProtocol.class);
    }
}
