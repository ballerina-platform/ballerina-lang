package org.wso2.carbon.transport.http.netty.listener;

import org.wso2.carbon.transport.http.netty.internal.NettyTransportDataHolder;

/**
 * This class can be used to override initializer.
 */
public class OverrideInitializer {

    private static OverrideInitializer instance = new OverrideInitializer();

    public static OverrideInitializer getInstance() {
        return instance;
    }

    public void setNewInitializer(String key, CarbonNettyInitializer initializer) {
        NettyTransportDataHolder.getInstance().addNettyChannelInitializer(key, initializer);
    }
}
