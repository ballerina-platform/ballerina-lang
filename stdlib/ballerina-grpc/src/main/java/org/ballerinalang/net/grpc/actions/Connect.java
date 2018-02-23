package org.ballerinalang.net.grpc.actions;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.AbstractNativeAction;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.GRPCClientStub;
import org.ballerinalang.net.grpc.proto.definition.ServiceProto;
import org.ballerinalang.net.grpc.stubs.GRPCBlockingStub;
import org.ballerinalang.net.grpc.stubs.GRPCFutureStub;
import org.ballerinalang.net.grpc.stubs.GRPCNonBlockingStub;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * .
 * .
 */
@BallerinaAction(
        packageName = "ballerina.net.grpc",
        actionName = "connect",
        connectorName = "GRPCConnector",
        args = {
                @Argument(name = "stubType", type = TypeKind.ANY),
                @Argument(name = "describtorMap", type = TypeKind.MAP)
            
        },
        returnType = {
                @ReturnType(type = TypeKind.STRUCT, structType = "Connection", structPackage = "ballerina.net.grpc"),
                @ReturnType(type = TypeKind.STRUCT, structType = "ConnectorError",
                        structPackage = "ballerina.net.grpc"),
        },
        connectorArgs = {
                @Argument(name = "host", type = TypeKind.STRING),
                @Argument(name = "port", type = TypeKind.INT)
        }
)
public class Connect extends AbstractNativeAction {
    private static ServiceProto serviceProto;
    
    @Override
    public ConnectorFuture execute(Context context) {
        
        String host, stubType;
        BMap bMap;
        
        int port;
        BStruct outboundError = createStruct(context, "ConnectorError");
        ClientConnectorFuture ballerinaFuture = new ClientConnectorFuture();
        try {
            try {
                BConnector bConnector = (BConnector) getRefArgument(context, 0);
                host = bConnector.getStringField(0);
                port = (int) bConnector.getIntField(0);
                stubType = getStringArgument(context, 0);
                bMap = (BMap) getRefArgument(context, 1);
                // todo: 2/20/18 more depth structures
                //bjson = (BJSON) getRefArgument(context, 2);
                List<byte[]> depDescriptorData = new ArrayList<>();
                if (bMap.keySet().size() > 1) {
                    for (Object key : bMap.keySet()) {
                        depDescriptorData.add(hexStringToByteArray(bMap.get(key).stringValue()));
                    }
                }
                serviceProto = new ServiceProto(hexStringToByteArray(bMap.get(context.getProgramFile()
                        .getEntryPkgName())
                        .stringValue()), depDescriptorData);
            } catch (ArrayIndexOutOfBoundsException e) {
                outboundError.setStringField(0, "gRPC Connector Error :" + e.getMessage());
                ballerinaFuture.notifyReply(null, outboundError);
                return ballerinaFuture;
            }
            ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext(true)
                    .build();
            GRPCClientStub grpcClientStub = new GRPCClientStub(serviceProto);
            BStruct outboundResponse = createStruct(context, "Connection");
            outboundResponse.setStringField(0, host);
            outboundResponse.setIntField(0, port);
            if ("blocking".equalsIgnoreCase(stubType)) {
                GRPCBlockingStub grpcBlockingStub = grpcClientStub.newBlockingStub(channel);
                outboundResponse.addNativeData("stub", grpcBlockingStub);
            } else if ("non-blocking".equalsIgnoreCase(stubType)) {
                GRPCNonBlockingStub nonBlockingStub = grpcClientStub.newNonBlockingStub(channel);
                outboundResponse.addNativeData("stub", nonBlockingStub);
            } else if ("future".equalsIgnoreCase(stubType)) {
                GRPCFutureStub grpcFutureStub = grpcClientStub.newFutureStub(channel);
                outboundResponse.addNativeData("stub", grpcFutureStub);
            } else {
                outboundError.setStringField(0, "gRPC Connector Error : Invalid stub type");
                ballerinaFuture.notifyReply(null, outboundError);
                return ballerinaFuture;
            }
            ballerinaFuture.notifyReply(outboundResponse, null);
            return ballerinaFuture;
        } catch (RuntimeException e) {
            outboundError.setStringField(0, "gRPC Connector Error :" + e.getMessage());
            ballerinaFuture.notifyReply(null, outboundError);
            return ballerinaFuture;
        }
        
    }
    
    private static byte[] hexStringToByteArray(String s) {
        
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    
    private BStruct createStruct(Context context, String structName) {
        
        PackageInfo httpPackageInfo = context.getProgramFile()
                .getPackageInfo("ballerina.net.grpc");
        StructInfo structInfo = httpPackageInfo.getStructInfo(structName);
        BStructType structType = structInfo.getType();
        return new BStruct(structType);
    }
    
    public static ServiceProto getServiceProto() {
        return serviceProto;
    }
}
