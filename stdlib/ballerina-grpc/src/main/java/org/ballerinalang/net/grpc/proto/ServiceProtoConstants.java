package org.ballerinalang.net.grpc.proto;

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
    // Descriptor file extension
    public static final String DESC_FILE_EXTENSION = ".desc";
    // Message Message index
    public static final int CONNECTION_INDEX = 0;

    // dot symbol for generate full class path
    public static final char CLASSPATH_SYMBOL = '.';
    // Proto contract directory.
    public static final String PROTO_BUF_DIRECTORY = "proto-def";
    // Invalid wire type.
    public static final int INVALID_WIRE_TYPE = -1;
    // Embedded messages, packed repeated fields wire type.
    public static final int MESSAGE_WIRE_TYPE = 2;
    public static final String UTF_8_CHARSET = "UTF-8";
    // New line character constant.
    public static final String NEW_LINE_CHARACTER = System.getProperty("line.separator");


    // proto wrapper message constants
    public static final String WRAPPER_DOUBLE_MESSAGE = "DoubleValue";
    public static final String WRAPPER_FLOAT_MESSAGE = "FloatValue";
    public static final String WRAPPER_INT64_MESSAGE = "Int64Value";
    public static final String WRAPPER_UINT64_MESSAGE = "UInt64Value";
    public static final String WRAPPER_INT32_MESSAGE = "Int32Value";
    public static final String WRAPPER_UINT32_MESSAGE = "UInt32Value";
    public static final String WRAPPER_BOOL_MESSAGE = "BoolValue";
    public static final String WRAPPER_STRING_MESSAGE = "StringValue";
    public static final String WRAPPER_BYTES_MESSAGE = "BytesValue";

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
}
