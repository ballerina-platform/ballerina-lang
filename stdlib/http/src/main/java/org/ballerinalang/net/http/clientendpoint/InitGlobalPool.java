package org.ballerinalang.net.http.clientendpoint;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpConstants;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.PoolConfiguration;

import static org.ballerinalang.net.http.HttpConstants.CONNECTION_MANAGER;

@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "initGlobalPool",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "ConnectionManager", structPackage = "ballerina"
                + "/http"),
        args = { @Argument(name = "poolConfig", type = TypeKind.RECORD, structType = "PoolConfiguration")},
        isPublic = true
)
public class InitGlobalPool extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BMap<String, BValue> globalPoolConfig = (BMap<String, BValue>) context
                .getRefArgument(HttpConstants.POOL_CONFIG_INDEX);
        PoolConfiguration globalPool = new PoolConfiguration();
        ConnectionManager connectionManager = new ConnectionManager(globalPool);
        globalPoolConfig.addNativeData(CONNECTION_MANAGER, connectionManager);
    }
}
