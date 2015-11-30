package org.wso2.carbon.transport.http.netty.listener;

import org.wso2.carbon.messaging.CarbonTransportServerInitializer;
import org.wso2.carbon.messaging.OverrideInitializer;
import org.wso2.carbon.transport.http.netty.internal.NettyTransportDataHolder;

/**
 * This class can be used to override initializer.
 */
public class OverrideInitializerImpl  implements OverrideInitializer {

    private static OverrideInitializer instance = new OverrideInitializerImpl();

    public static OverrideInitializer getInstance() {
        return instance;
    }

    public void setNewInitializer(String key, CarbonTransportServerInitializer initializer) {
        NettyTransportDataHolder.getInstance().addNettyChannelInitializer(key, initializer);
    }
}
