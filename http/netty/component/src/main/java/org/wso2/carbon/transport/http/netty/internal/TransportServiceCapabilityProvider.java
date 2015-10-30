package org.wso2.carbon.transport.http.netty.internal;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.kernel.startupresolver.CapabilityProvider;
import org.wso2.carbon.kernel.transports.CarbonTransport;
import org.wso2.carbon.transport.http.netty.internal.config.TransportConfigurationBuilder;

/**
 * Component which registers the CarbonTransport capability information.
 */
@Component(
        name = "org.wso2.carbon.transport.http.netty.internal.TransportServiceCapabilityProvider",
        immediate = true,
        service = CapabilityProvider.class
)
public class TransportServiceCapabilityProvider implements CapabilityProvider {

    @Override
    public String getName() {
        return CarbonTransport.class.getName();
    }

    @Override
    public int getCount() {
        return TransportConfigurationBuilder.build().getListenerConfigurations().size();
    }
}
