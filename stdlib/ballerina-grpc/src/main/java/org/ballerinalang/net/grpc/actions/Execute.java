package org.ballerinalang.net.grpc.actions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.AbstractNativeAction;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.GRPCClientStub;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.exception.UnsupportedFieldTypeException;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code Get} is the GET action implementation of the HTTP Connector.
 */
@BallerinaAction(
        packageName = "ballerina.net.grpc",
        actionName = "execute",
        connectorName = "GRPCConnector",
        args = {
                @Argument(name = "conn", type = TypeKind.STRUCT, structType = "Connection", structPackage =
                        "ballerina.net.grpc"),
                @Argument(name = "payload", type = TypeKind.ANY),
                @Argument(name = "methodID", type = TypeKind.INT)
        },
        returnType = {
                @ReturnType(type = TypeKind.ANY),
                @ReturnType(type = TypeKind.STRUCT, structType = "GRPCConnectorError",
                        structPackage = "ballerina.net.grpc"),
        },
        connectorArgs = {
                @Argument(name = "host", type = TypeKind.STRING),
                @Argument(name = "port", type = TypeKind.INT)
        }
)
public class Execute extends AbstractNativeAction {

    private static final Logger logger = LoggerFactory.getLogger(Execute.class);

    @Override
    public ConnectorFuture execute(Context context) {

        ClientConnectorFuture ballerinaFuture = new ClientConnectorFuture();
        BStruct outboundError = createStruct(context);
        BStruct payloadBMap;
        BStruct connectionStub;
        Integer methodId;
        try {
            // todo: 2/15/18 Check type and cast
            payloadBMap = (BStruct) getRefArgument(context, 2);
            connectionStub = (BStruct) getRefArgument(context, 1);
            methodId = getIntArgument(context, 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            outboundError.setStringField(0, "gRPC Connector Error :" + e.getMessage());
            ballerinaFuture.notifyReply(null, outboundError);
            return ballerinaFuture;
        }
        Map<String, Object> payload = new HashMap<>();
//        for (Object key : payloadBMap.get) {
//            
//        }
        // todo: 2/15/18 typeee
        payload.put("name" , payloadBMap.getStringField(0));
        try {
            String fullName = GRPCClientStub.getGrpcServiceProto().getSet().getService(0)
                    .getMethodList().get(methodId)
                    .getInputType();
            String reqMessageName = fullName.split("\\.")[fullName.split("\\.").length - 1];
            GRPCClientStub.GRPCBlockingStub grpcBlockingStub = (GRPCClientStub.GRPCBlockingStub)
                    connectionStub.getNativeData("stub");
            Message message = (Message) Message.newBuilder(reqMessageName).build();
            message.setFieldValues(payload);
            Message messageRes = grpcBlockingStub.executeBlockingUnary(message, methodId);
            // todo: 2/15/18 Check type and cast and pass
            if (messageRes != null && messageRes.getDescriptor().getFields().size() == 0) {
                ballerinaFuture.notifyReply(new BMap<>(), null);
            } else {
                assert messageRes != null;
                ballerinaFuture.notifyReply(generateResponseMap(messageRes), null);
            }
            return ballerinaFuture;
        } catch (RuntimeException e) {
            outboundError.setStringField(0, "gRPC Connector Error :" + e.getMessage());
            ballerinaFuture.notifyReply(null, outboundError);
            return ballerinaFuture;
        }
    }

    private BStruct createStruct(Context context) {

        PackageInfo httpPackageInfo = context.getProgramFile()
                .getPackageInfo("ballerina.net.grpc");
        StructInfo structInfo = httpPackageInfo.getStructInfo("GRPCConnectorError");
        BStructType structType = structInfo.getType();
        return new BStruct(structType);
    }

//    private Object getValue(BValue bValue) {
//
//        switch (bValue.getType().getName()) {
//            case "string": {
//                return bValue.stringValue();
//            }
//            case "int": {
//                return Integer.parseInt(bValue.stringValue());
//            }
//            case "float": {
//                return Float.parseFloat(bValue.stringValue());
//            }
//            case "double": {
//                return Double.parseDouble(bValue.stringValue());
//            }
//            case "boolean": {
//                return Boolean.parseBoolean(bValue.stringValue());
//            }
//            default: {
//                throw new UnsupportedFieldTypeException("Error while generating request struct. Field " +
//                        "type is not supported : " + bValue.getType());
//            }
//        }
//    }

    private BMap generateResponseMap(Message messageRes) {

        if (messageRes == null) {
            return new BMap<>();
        } else {
            Map<String, Object> fieldsMap = messageRes.getFields();
            BMap<String, BValue> responseMap = new BMap<>();
            for (Map.Entry entry : fieldsMap.entrySet()) {
                String typeName = messageRes.getDescriptor().getFields().get(0).getType().name();
                responseMap.put((String) entry.getKey(), getValue(typeName, entry.getValue()));
            }
            return responseMap;
        }

    }

//    private void notifyError(Throwable throwable) {
//        BStruct httpConnectorError = createStruct(context, Constants.HTTP_CONNECTOR_ERROR,  Constants
//                .PROTOCOL_PACKAGE_HTTP);
//        httpConnectorError.setStringField(0, throwable.getMessage());
//        if (throwable instanceof ClientConnectorException) {
//            ClientConnectorException clientConnectorException = (ClientConnectorException) throwable;
//            httpConnectorError.setIntField(0, clientConnectorException.getHttpStatusCode());
//        }
//
//        ballerinaFuture.notifyReply(null, httpConnectorError);
//    }

    // todo: 2/12/18 support Strut & Enum Type
    private BValue getValue(String typeName, Object obj) {

        switch (typeName) {
            case "STRING": {
                return new BString((String) obj);
            }
            case "INT": {
                return new BInteger((int) obj);
            }
            case "FLOAT": {
                return new BFloat((float) obj);
            }
            case "DOUBLE": {
                return new BFloat((double) obj);
            }
            case "BOOLEAN": {
                return new BBoolean((boolean) obj);
            }
            default: {
                throw new UnsupportedFieldTypeException("Error while generating response. Field " +
                        "type is not supported : " + typeName);
            }
        }

    }
}
