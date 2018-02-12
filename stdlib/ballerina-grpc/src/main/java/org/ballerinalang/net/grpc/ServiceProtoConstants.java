package org.ballerinalang.net.grpc;

import com.google.protobuf.DescriptorProtos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daneshk on 1/20/18.
 */
public class ServiceProtoConstants {
    // Protocol syntax used for the gRPC service definition
    public static final String PROTOCOL_SYNTAX = "proto3";
    // Proto file extension
    public static final String PROTO_FILE_EXTENSION = ".proto";
    // Message Message index
    public static final int REQUEST_MESSAGE_INDEX = 0;
    // Response Message index
    public static final int RESPONSE_MESSAGE_INDEX = 1;
    // dot symbol for generate full class path
    public static final char CLASSPATH_SYMBOL = '.';
    // Proto contract directory.
    public static final String PROTO_BUF_DIRECTORY = "proto-def";
    // Invalid wire type.
    public static final int INVALID_WIRE_TYPE = -1;
    // Embedded messages, packed repeated fields wire type.
    public static final int MESSAGE_WIRE_TYPE = 2;
    public static final String UTF_8_CHARSET = "UTF-8";

    public static final Map<Integer, String> FIELD_TYPE_MAP;
    static {
        Map<Integer, String> sTypeMap = new HashMap<>();;
        sTypeMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE, "double");
        sTypeMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE, "float");
        sTypeMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE, "int64");
        sTypeMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE, "uint64");
        sTypeMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE, "int32");
        sTypeMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE, "fixed64");
        sTypeMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE, "fixed32");
        sTypeMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE, "bool");
        sTypeMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32_VALUE, "uint32");
        sTypeMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32_VALUE, "sint32");
        sTypeMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64_VALUE, "sint64");
        sTypeMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32_VALUE, "sfixed32");
        sTypeMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64_VALUE, "sfixed64");
        sTypeMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE, "string");
        sTypeMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES_VALUE, "bytes");
        FIELD_TYPE_MAP = Collections.unmodifiableMap(sTypeMap);
    }

    public static final Map<DescriptorProtos.FieldDescriptorProto.Type, Integer> WIRE_TYPE_MAP;
    static {
        Map<DescriptorProtos.FieldDescriptorProto.Type, Integer> wireMap = new HashMap<>();;
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE, 1);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT, 5);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32, 0);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64, 0);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32, 0);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64, 0);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32, 0);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64, 0);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32, 5);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64, 1);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32, 5);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64, 1);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL, 0);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING, 2);
        wireMap.put(DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES, 2);
        WIRE_TYPE_MAP = Collections.unmodifiableMap(wireMap);
    }
}
