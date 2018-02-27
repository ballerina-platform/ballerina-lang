/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc.builder.utils;

import com.google.protobuf.DescriptorProtos;
import org.ballerinalang.net.grpc.exception.UnsupportedFieldTypeException;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.net.grpc.builder.BalGenConstants.FILE_SEPERATOR;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.HEX_ARRAY;

/**
 * Util functions which are use when generating . bal stub
 */
public class BalGenerationUtils {
    
    
    /**
     * Convert byte array to readable byte string.
     *
     * @param data byte array of proto file
     * @return readable string of byte array
     */
    public static String bytesToHex(byte[] data) {
        
        char[] hexChars = new char[data.length * 2];
        for (int j = 0; j < data.length; j++) {
            int v = data[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    /**
     * This function returns the ballerina data type which is mapped to  protobuff data type.
     *
     * @param protoType .proto data type
     * @return Ballerina data type.
     */
    public static String getMappingBalType(String protoType) {
        switch (protoType) {
            case "DoubleValue":
            case "FloatValue": {
                return "float";
            }
            case "Int32Value":
            case "Int64Value":
            case "UInt64Value":
            case "UInt32Value": {
                return "int";
            }
            case "BoolValue": {
                return "boolean";
            }
            case "StringValue": {
                return "string";
            }
            case "BytesValue": {
                return "blob";
            }
            default: { // to handle structs
                return protoType;
            }
        }
    }
    
    /**
     * This function returns the ballerina data type which is mapped to  protobuff data type.
     *
     * @param protoType .proto data type
     * @return Ballerina data type.
     */
    public static boolean isStructType(String protoType) {
        switch (protoType) {
            case "float":
            case "int":
            case "boolean":
            case "string":
            case "blob": {
                return false;
            }
            default: { // to handle structs
                return true;
            }
        }
    }
    
    /**
     * Method is responsible of writing the bal string payload to .bal file.
     *
     * @param payload    .
     * @param balOutPath .
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static void writeFile(String payload, String fileName, Path balOutPath) throws FileNotFoundException,
            UnsupportedEncodingException {
        String filePath;
        if ("".equals(balOutPath.toString())) {
            filePath = balOutPath.toString() + fileName;
        } else {
            filePath = balOutPath.toString() + FILE_SEPERATOR + fileName;
        }
        Path path = Paths.get(filePath);
        PrintWriter writer = new PrintWriter(path.toFile(), "UTF-8");
        writer.print(payload);
        writer.close();
    }
    
    /**
     * Method is responsible for convert globally defined struct List to map.
     *
     * @param list .
     * @return .
     */
    public static Map<String, DescriptorProtos.DescriptorProto> attributeListToMap(java.util.List<DescriptorProtos
            .DescriptorProto> list) {
        
        Map<String, DescriptorProtos.DescriptorProto> stringObjectMap = new HashMap<>();
        for (DescriptorProtos.DescriptorProto proto : list) {
            stringObjectMap.put(proto.getName(), proto);
        }
        return stringObjectMap;
    }
    
    /**
     * Method is for getting ballerina data type which is map to the .proto data type.
     *
     * @param num .
     * @return .
     */
    public static String getTypeName(int num) {
        switch (num) {
            case DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE_VALUE: {
                return "float";
            }
            case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT_VALUE: {
                return "float";
            }
            case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64_VALUE:
            case DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64_VALUE:
            case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64_VALUE: {
                return "int";
            }
            case DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32_VALUE:
            case DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32_VALUE: {
                return "struct";
            }
            case DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL_VALUE: {
                return "boolean";
            }
            case DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING_VALUE: {
                return "string";
            }
            case DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE_VALUE: {
                return "struct";
            }
            default: {
                throw new UnsupportedFieldTypeException("Error while decoding request message. Field " +
                        "type is not supported : " + num);
            }
        }
    }
    
}
