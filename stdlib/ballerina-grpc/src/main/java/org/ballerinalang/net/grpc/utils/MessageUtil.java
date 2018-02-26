package org.ballerinalang.net.grpc.utils;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageRegistry;
import org.ballerinalang.net.grpc.exception.UnsupportedFieldTypeException;

import java.util.Map;

/**.
 * Util class for message operationss.
 */
public class MessageUtil {
    public static Message generateProtoMessage(BValue responseValue,
                                                                   Descriptors.Descriptor outputType) {
        Message.Builder responseBuilder = Message.newBuilder(outputType.getName());
        int stringIndex = 0;
        int intIndex = 0;
        int floatIndex = 0;
        int boolIndex = 0;
        int refIndex = 0;
        for (Descriptors.FieldDescriptor fieldDescriptor : outputType.getFields()) {
            String fieldName = fieldDescriptor.getName();
            switch (fieldDescriptor.getType().toProto().getNumber()) {
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE: {
                    double value = 0F;
                    if (responseValue instanceof BStruct) {
                        value = ((BStruct) responseValue).getFloatField(floatIndex++);
                    } else {
                        if (responseValue instanceof BFloat) {
                            value = ((BFloat) responseValue).value();
                        }
                    }
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                    float value = 0F;
                    if (responseValue instanceof BStruct) {
                        value = Float.parseFloat(String.valueOf(((BStruct) responseValue).getFloatField(floatIndex++)));
                    } else {
                        if (responseValue instanceof BFloat) {
                            value = Float.parseFloat(String.valueOf(((BFloat) responseValue).value()));
                        }
                    }
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE:
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE:
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                    long value = 0;
                    if (responseValue instanceof BStruct) {
                        value = ((BStruct) responseValue).getIntField(intIndex++);
                    } else {
                        if (responseValue instanceof BInteger) {
                            value = ((BInteger) responseValue).value();
                        }
                    }
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE:
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                    int value = 0;
                    if (responseValue instanceof BStruct) {
                        value = Integer.parseInt(String.valueOf(((BStruct) responseValue).getIntField(intIndex++)));
                    } else {
                        if (responseValue instanceof BInteger) {
                            value = Integer.parseInt(String.valueOf(((BInteger) responseValue).value()));
                        }
                    }
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                    boolean value = false;
                    if (responseValue instanceof BStruct) {
                        value = ((BStruct) responseValue).getBooleanField(boolIndex++) > 0;
                    } else {
                        if (responseValue instanceof BBoolean) {
                            value = ((BBoolean) responseValue).value();
                        }
                    }
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                    String value = null;
                    if (responseValue instanceof BStruct) {
                        value = ((BStruct) responseValue).getStringField(stringIndex++);
                    } else {
                        if (responseValue instanceof BString) {
                            value = ((BString) responseValue).value();
                        }
                    }
                    responseBuilder.addField(fieldName, value);
                    break;
                }
                default: {
                    throw new UnsupportedFieldTypeException("Error while decoding request message. Field " +
                            "type is not supported : " + fieldDescriptor.getType());
                }
            }
        }
        return responseBuilder.build();
    }
    
    public static BValue generateRequestStruct(Message request, String fieldName, BType structType, Context context) {
        BValue bValue = null;
        int stringIndex = 0;
        int intIndex = 0;
        int floatIndex = 0;
        int boolIndex = 0;
        int refIndex = 0;
        
        if (structType instanceof BStructType) {
            BStruct requestStruct = createStruct(context, fieldName);
            for (BStructType.StructField structField : ((BStructType) structType).getStructFields()) {
                String structFieldName = structField.getFieldName();
                if (structField.getFieldType() instanceof BRefType) {
                    BType bType = structField.getFieldType();
                    if (MessageRegistry.getInstance().getMessageDescriptorMap().containsKey(bType.getName())) {
                        Message message = (Message) request.getFields().get(structFieldName);
                        requestStruct.setRefField(refIndex++, (BRefType) generateRequestStruct(message,
                                structFieldName, structField.getFieldType(), context));
                    }
                } else {
                    if (request.getFields().containsKey(structFieldName)) {
                        String fieldType = structField.getFieldType().getName();
                        switch (fieldType) {
                            case "string": {
                                requestStruct.setStringField(stringIndex++, (String) request.getFields().get
                                        (structFieldName));
                                break;
                            }
                            case "int": {
                                requestStruct.setIntField(intIndex++, (Long) request.getFields().get
                                        (structFieldName));
                                break;
                            }
                            case "float": {
                                Float value = (Float) request.getFields().get(structFieldName);
                                if (value != null) {
                                    requestStruct.setFloatField(floatIndex++, Double.parseDouble(value.toString()));
                                }
                                break;
                            }
                            case "double": {
                                Double value = (Double) request.getFields().get(structFieldName);
                                if (value != null) {
                                    requestStruct.setFloatField(floatIndex++, Double.parseDouble(value.toString()));
                                }
                                break;
                            }
                            case "boolean": {
                                requestStruct.setBooleanField(boolIndex++, (Integer) request.getFields().get
                                        (structFieldName));
                                break;
                            }
                            default: {
                                throw new UnsupportedFieldTypeException("Error while generating request struct. Field" +
                                        " type is not supported : " + fieldType);
                            }
                        }
                    }
                }
            }
            bValue = requestStruct;
        } else {
            Map<String, Object> fields = request.getFields();
            if (fields.size() == 1 && fields.containsKey("value")) {
                fieldName = "value";
            }
            if (request.getFields().containsKey(fieldName)) {
                String fieldType = structType.getName();
                switch (fieldType) {
                    case "string": {
                        bValue = new BString((String) request.getFields().get(fieldName));
                        break;
                    }
                    case "int": {
                        bValue = new BInteger((Long) request.getFields().get(fieldName));
                        break;
                    }
                    case "float": {
                        Float value = (Float) request.getFields().get(fieldName);
                        if (value != null) {
                            bValue = new BFloat(Double.parseDouble(value.toString()));
                        }
                        break;
                    }
                    case "double": {
                        Double value = (Double) request.getFields().get(fieldName);
                        if (value != null) {
                            bValue = new BFloat(Double.parseDouble(value.toString()));
                        }
                        break;
                    }
                    case "boolean": {
                        bValue = new BBoolean((Boolean) request.getFields().get(fieldName));
                        break;
                    }
                    default: {
                        throw new UnsupportedFieldTypeException("Error while generating request struct. Field " +
                                "type is not supported : " + fieldType);
                    }
                }
            }
        }
        
        return bValue;
    }
    
    private static BStruct createStruct(Context context, String fieldName) {
        BStructType structType = context.getProgramFile().getEntryPackage().getStructInfo(fieldName).getType();
        return new BStruct(structType);
    }
}
