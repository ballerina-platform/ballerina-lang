package org.wso2.carbon.transport.http.netty.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.kernel.startupresolver.CapabilityProvider;
import org.wso2.carbon.transport.http.netty.internal.config.YAMLTransportConfigurationBuilder;

/**
 * Component which registers the CarbonTransport capability information.
 */
@Component(
        name = "org.wso2.carbon.transport.http.netty.internal.NettyInitializerCapabilityProvider",
        immediate = true,
        service = CapabilityProvider.class,
        property = "capability-name=org.wso2.carbon.transport.http.netty.listener.CarbonNettyServerInitializer"
)
public class NettyInitializerCapabilityProvider implements CapabilityProvider {

    @Activate
    protected void start(BundleContext bundleContext) {

    }

    @Override
    public int getCount() {
        return YAMLTransportConfigurationBuilder.build().getListenerConfigurations().size();
    }
}
