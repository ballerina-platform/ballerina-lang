/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc.builder;

import com.google.protobuf.DescriptorProtos;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.net.grpc.exception.UnsupportedFieldTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * .
 * .
 */
public class BalGenerate {
    
    public static final Logger LOG = LoggerFactory.getLogger(BalGenerate.class);
    private static final String NEW_LINE_CHARACTER = System.getProperty("line.separator");
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    
    /**
     * .
     * .
     *
     * @param rootDescriptor       .
     * @param dependentDescriptors .
     * @param balOutPath           .
     */
    public void generate(byte[] rootDescriptor, byte[][] dependentDescriptors, String balOutPath) {
        try {
            BMap descriptorMap = generateDependencyMap(rootDescriptor, dependentDescriptors);
            generateBalDefinitions(rootDescriptor, balOutPath, descriptorMap);
        } catch (IOException e) {
            throw new RuntimeException(" Error ", e);
        }
        
    }
    /**
     * .
     *
     * @param rootDescriptor       .
     * @param dependentDescriptors .
     * @return .
     * @throws IOException .
     */
    private BMap generateDependencyMap(byte[] rootDescriptor, byte[][] dependentDescriptors) throws IOException {
        
        InputStream targetStream = new ByteArrayInputStream(rootDescriptor);
        DescriptorProtos.FileDescriptorProto fileDescriptorSet = DescriptorProtos.FileDescriptorProto
                .parseFrom(targetStream);
        BMap<String, BString> descriptorMap = new BMap<String, BString>();
        descriptorMap.put("\"" + fileDescriptorSet.getPackage() + "." + fileDescriptorSet.getName() + "\"",
                new BString("\"" + bytesToHex(rootDescriptor) + "\""));
        
        for (byte[] str : dependentDescriptors) {
            if (str.length > 0) {
                targetStream = new ByteArrayInputStream(str);
                fileDescriptorSet = DescriptorProtos.FileDescriptorProto
                        .parseFrom(targetStream);
                descriptorMap.put("\"" + fileDescriptorSet.getPackage() + "." + fileDescriptorSet.getName() + "\"",
                        new BString("\"" + bytesToHex(str) + "\""));
            }
        }
        return descriptorMap;
    }
    /**
     * .
     *
     * @param data .
     * @return .
     */
    private static String bytesToHex(byte[] data) {
        
        char[] hexChars = new char[data.length * 2];
        for (int j = 0; j < data.length; j++) {
            int v = data[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * .
     * .
     *
     * @param methodName         .
     * @param reqMessageName     .
     * @param resMessageName     .
     * @param methodID           .
     * @param reqStructFieldName .
     * @param reqStructFieldType .
     * @param resStructFieldName .
     * @param resStructFieldType .
     * @return .
     */
    private static String generateActions(String methodName, String reqMessageName, String resMessageName,
                                          int methodID, String reqStructFieldName, String reqStructFieldType,
                                          String resStructFieldName, String resStructFieldType) {
        String template_prt1 = "", template_prt2 = "";
        String template =
                "    action %s (grpc:Connection conn, any req) (any, error) {" + NEW_LINE_CHARACTER +
                        "" + NEW_LINE_CHARACTER +
                        "        %s" + NEW_LINE_CHARACTER +
                        "" + NEW_LINE_CHARACTER +
                        "        var res, err3 = ep.execute(conn, value, %d);" + NEW_LINE_CHARACTER +
                        "        if (err3 != null) {" + NEW_LINE_CHARACTER +
                        "            error e = {msg:err3.msg};" + NEW_LINE_CHARACTER +
                        "            return null, e;" + NEW_LINE_CHARACTER +
                        "        }" + NEW_LINE_CHARACTER +
                        "" + NEW_LINE_CHARACTER +
                        "        %s" + NEW_LINE_CHARACTER +
                        "" + NEW_LINE_CHARACTER +
                        "        return value, null;" + NEW_LINE_CHARACTER +
                        "    }";
        
        if (resStructFieldName != null) {
            String template5 =
                            "var response, err4 = (%s)res;" + NEW_LINE_CHARACTER +
                            "        if (err4 != null) {" + NEW_LINE_CHARACTER +
                            "            %s resObj = {};" + NEW_LINE_CHARACTER +
                            "            resObj.%s, err5 = (%s)req;" + NEW_LINE_CHARACTER +
                            "            if (err5 != null) {" + NEW_LINE_CHARACTER +
                            "                error e = {msg:err5.msg};" + NEW_LINE_CHARACTER +
                            "                return null, e;" + NEW_LINE_CHARACTER +
                            "            }" + NEW_LINE_CHARACTER +
                            "            value = resObj;" + NEW_LINE_CHARACTER +
                            "        } else {" + NEW_LINE_CHARACTER +
                            "            value = response;" + NEW_LINE_CHARACTER +
                            "        }" + NEW_LINE_CHARACTER;
            template_prt2 = String.format(template5, resMessageName, resMessageName, resStructFieldName,
                    resStructFieldType);
        }
        if (reqStructFieldName != null) {
            String template2 =
                            "var request, err = (%s)req;" + NEW_LINE_CHARACTER +
                            "        any value;" + NEW_LINE_CHARACTER +
                            "        if (err != null) {" + NEW_LINE_CHARACTER +
                            "            %s reqObj = {};" + NEW_LINE_CHARACTER +
                            "            reqObj.%s, err2 = (%s)req;" + NEW_LINE_CHARACTER +
                            "            if (err2 != null) {" + NEW_LINE_CHARACTER +
                            "                error e = {msg:err2.msg};" + NEW_LINE_CHARACTER +
                            "                return null, e;" + NEW_LINE_CHARACTER +
                            "            }" + NEW_LINE_CHARACTER +
                            "            value = reqObj;" + NEW_LINE_CHARACTER +
                            "        } else {" + NEW_LINE_CHARACTER +
                            "            value = request;" + NEW_LINE_CHARACTER +
                            "        }" + NEW_LINE_CHARACTER;
            template_prt1 = String.format(template2, reqMessageName, reqMessageName, reqStructFieldName,
                    reqStructFieldType);
        }
        if (reqStructFieldName == null) {
            String template3 =
                            "var request, err = (%s)req;" + NEW_LINE_CHARACTER +
                            "        any value;" + NEW_LINE_CHARACTER +
                            "        if (err != null) {" + NEW_LINE_CHARACTER +
                            "            error e = {msg:err.msg};" + NEW_LINE_CHARACTER +
                            "            return null, e;" + NEW_LINE_CHARACTER +
                            "        } else {" + NEW_LINE_CHARACTER +
                            "            value = request;" + NEW_LINE_CHARACTER +
                            "        }" + NEW_LINE_CHARACTER;
            template_prt1 = String.format(template3, reqMessageName);
        }
        if (resStructFieldName == null) {
            String template4 =
                            "var response, err4 = (%s)res;" + NEW_LINE_CHARACTER +
                            "        if (err4 != null) {" + NEW_LINE_CHARACTER +
                            "            error e = {msg:err4.msg};" + NEW_LINE_CHARACTER +
                            "            return null, e;" + NEW_LINE_CHARACTER +
                            "        } else {" + NEW_LINE_CHARACTER +
                            "            value = response;" + NEW_LINE_CHARACTER +
                            "        }" + NEW_LINE_CHARACTER;
            template_prt2 = String.format(template4, resMessageName);
        }
        return String.format(template, methodName, template_prt1, methodID, template_prt2);
    }
    
    
    /**
     * todo.
     *
     * @param actionList    .
     * @param packageName   .
     * @param connectorName .
     * @return .
     */
    private static String generateConnector(String actionList, String packageName, String connectorName) {
        
        String str = "package %s;" + NEW_LINE_CHARACTER +
                "import ballerina.net.grpc;" + NEW_LINE_CHARACTER +
                NEW_LINE_CHARACTER +
                "public connector %s(string host, int port){" + NEW_LINE_CHARACTER +
                " endpoint<grpc:GRPCConnector> ep {" + NEW_LINE_CHARACTER +
                "        create grpc:GRPCConnector(host, port);" + NEW_LINE_CHARACTER +
                "    }" + NEW_LINE_CHARACTER +
                NEW_LINE_CHARACTER +
                "    action connect (string stubType) (grpc:Connection, error) {" + NEW_LINE_CHARACTER +
                "        var resp, convErr = ep.connect(stubType,descriptorMap);" + NEW_LINE_CHARACTER +
                "        var conn, er = <grpc:Connection>resp;" + NEW_LINE_CHARACTER +
                "        var connErr, er = <grpc:ConnectorError>convErr;" + NEW_LINE_CHARACTER +
                "        if (connErr != null) {" + NEW_LINE_CHARACTER +
                "            println(\"Error: \" + connErr.msg);" + NEW_LINE_CHARACTER +
                "            error err = {msg:connErr.msg};" + NEW_LINE_CHARACTER +
                "            return null,err;" + NEW_LINE_CHARACTER +
                "        }" + NEW_LINE_CHARACTER +
                "        return conn, null;" + NEW_LINE_CHARACTER +
                "    }" + NEW_LINE_CHARACTER +
                "%s" +
                NEW_LINE_CHARACTER +
                "}" + NEW_LINE_CHARACTER;
        return String.format(str, packageName, connectorName, actionList);
    }
    
    /**
     * todo.
     *
     * @param name       .
     * @param attributes .
     * @return .
     */
    private static String generateStruct(String name, String attributes) {
        
        String str = String.format(NEW_LINE_CHARACTER +
                "struct %s {" + NEW_LINE_CHARACTER +
                "%s" + NEW_LINE_CHARACTER +
                "}" + NEW_LINE_CHARACTER, name, attributes);
        return str;
    }
    
    /**
     * todo.
     *
     * @param attributesNameArr .
     * @param attributesTypeArr .
     * @return .
     */
    private static String generateAttributes(String[] attributesNameArr, String[] attributesTypeArr) {
        
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < attributesNameArr.length; i++) {
            str.append(String.format(NEW_LINE_CHARACTER + "%s %s;", attributesTypeArr[i], attributesNameArr[i]));
        }
        return str.toString();
    }
    
    /**
     * todo.
     *
     * @param num .
     * @return .
     */
    private static String getTypeName(int num) {
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
            default: {
                throw new UnsupportedFieldTypeException("Error while decoding request message. Field " +
                        "type is not supported : " + num);
            }
        }
    }


//    /**
//     * .
//     *
//     * @param rootDescriptor       .
//     * @param dependentDescriptors .
//     * @return .
//     * @throws IOException .
//     */
//    private BJSON generateDependencyTree(byte[] rootDescriptor, BMap dependentDescriptors) throws IOException {
//
//        String payload = "";
//        Gson gson = new Gson();
//        InputStream targetStream = new ByteArrayInputStream(rootDescriptor);
//        DescriptorProtos.FileDescriptorProto fileDescriptorSet = DescriptorProtos.FileDescriptorProto
//                .parseFrom(targetStream);
//        ProtoDescriptor protoDescriptor = new ProtoDescriptor(fileDescriptorSet.getPackage() + "." +
//                fileDescriptorSet.getName());
//        if (fileDescriptorSet.getDependencyCount() != 0) {
//            for (int i = 0; i < fileDescriptorSet.getDependencyCount(); i++) {
//                // todo: 2/20/18 Move to recursive function
//                String descriptorString = dependentDescriptors.get("\"" + fileDescriptorSet.getDependency(i)
//                        .replace("/", ".") + "\"").stringValue();
//                descriptorString = descriptorString.substring(1, descriptorString.length() - 1);
//                targetStream = new ByteArrayInputStream(hexStringToByteArray(descriptorString));
//                DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto
//                        .parseFrom(targetStream);
//                if (proto.getDependencyCount() == 0) {
//                    protoDescriptor.addDependency(new ProtoDescriptor(proto.getPackage() + proto.getName()));
//                } else {
//                    // todo: 2/20/18
//                }
//            }
//        }
//        payload = gson.toJson(protoDescriptor);
//        return new BJSON(payload);
//    }
    
    /**
     * .
     *
     * @param rootDescriptor .
     * @param balOutPath     .
     * @param descriptorMap  .
     */
    public void generateBalDefinitions(byte[] rootDescriptor, String balOutPath, BMap descriptorMap) {
        try {
            File targetFile = new File(balOutPath);
            File parent = targetFile.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + parent);
            }
            InputStream targetStream = new ByteArrayInputStream(rootDescriptor);
            DescriptorProtos.FileDescriptorProto fileDescriptorSet = DescriptorProtos.FileDescriptorProto
                    .parseFrom(targetStream);
            java.util.List<DescriptorProtos.DescriptorProto> list = fileDescriptorSet.getMessageTypeList();
            java.util.List<DescriptorProtos.MethodDescriptorProto> methodList = fileDescriptorSet.getService(0)
                    .getMethodList();
            StringBuilder actionList = new StringBuilder();
            int i = 0;
            Map<String, DescriptorProtos.DescriptorProto> stringObjectMap = attrybuteList(fileDescriptorSet
                    .getMessageTypeList());
            for (DescriptorProtos.MethodDescriptorProto methodDescriptorProto : methodList) {
                // todo: 2/13/18 proper array
                String strOut = methodDescriptorProto.getOutputType().split("\\.")[methodDescriptorProto
                        .getOutputType().split("\\.").length - 1];
                String strIn = methodDescriptorProto.getInputType().split("\\.")[methodDescriptorProto
                        .getInputType().split("\\.").length - 1];
                
                if (fileDescriptorSet.getMessageTypeList().size() > 0) {
                    // TODO: 2/22/18 Handle null
                    actionList = actionList.append(NEW_LINE_CHARACTER).append(generateActions(methodDescriptorProto
                                    .getName(), getMappingBalType(strIn), getMappingBalType
                                    (strOut), i, stringObjectMap.get(strIn).getFieldList().get(0).getName(),
                            getTypeName(stringObjectMap.get(strIn).getFieldList().get(0)
                                    .getType().getNumber()), stringObjectMap.get(strOut).getFieldList().get(0)
                                    .getName(),
                            getTypeName(stringObjectMap.get(strOut).getFieldList().get(0)
                                    .getType().getNumber())));
                } else {
                    actionList = actionList.append(NEW_LINE_CHARACTER).append(generateActions(methodDescriptorProto
                            .getName(), getMappingBalType(strIn), getMappingBalType
                            (strOut), i, null, null, null,
                            null));
                }
                i++;
            }
            StringBuilder structList = new StringBuilder();
            for (DescriptorProtos.DescriptorProto descriptorProto : list) {
                String[] attributesNameArr = new String[descriptorProto.getFieldCount()];
                String[] attributesTypeArr = new String[descriptorProto.getFieldCount()];
                int j = 0;
                for (DescriptorProtos.FieldDescriptorProto fieldDescriptorProto : descriptorProto
                        .getFieldList()) {
                    attributesNameArr[j] = fieldDescriptorProto.getName();
                    attributesTypeArr[j] = !fieldDescriptorProto.getTypeName().equals("") ? fieldDescriptorProto
                            .getTypeName().split("\\.")[fieldDescriptorProto.getTypeName().split("\\.")
                            .length - 1] :
                            getTypeName(fieldDescriptorProto.getType().getNumber());
                    j++;
                }
                String attributeList = generateAttributes(attributesNameArr, attributesTypeArr);
                structList = structList.append(NEW_LINE_CHARACTER).append(generateStruct(descriptorProto.getName(),
                        attributeList));
            }
            String balPayload = generateConnector(actionList.toString(), fileDescriptorSet.getPackage(),
                    fileDescriptorSet.getService(0).getName() + "_stub") + structList
                    + NEW_LINE_CHARACTER + String.format("map descriptorMap ={%s};", mapToString(descriptorMap)) +
                    NEW_LINE_CHARACTER;
            writeFile(balPayload, balOutPath);
        } catch (IOException e) {
            throw new RuntimeException("error ", e);
        }
    }
    
    /**
     * .
     *
     * @param list .
     * @return .
     */
    private Map<String, DescriptorProtos.DescriptorProto> attrybuteList(java.util.List<DescriptorProtos
            .DescriptorProto> list) {
        
        Map<String, DescriptorProtos.DescriptorProto> stringObjectMap = new HashMap<>();
        for (DescriptorProtos.DescriptorProto proto : list) {
            stringObjectMap.put(proto.getName(), proto);
        }
        return stringObjectMap;
    }
    
    /**
     * todo.
     *
     * @param bMap .
     * @return .
     */
    
    private String mapToString(BMap bMap) {
        
        StringBuilder payload = new StringBuilder();
        for (Object key : bMap.keySet()) {
            payload.append(NEW_LINE_CHARACTER).append(key).append(":").append(bMap.get(key)).append(",");
        }
        return payload.substring(0, payload.length() - 1);
    }
    
    /**
     * todo.
     *
     * @param payload    .
     * @param balOutPath .
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    private void writeFile(String payload, String balOutPath) throws FileNotFoundException,
            UnsupportedEncodingException {
        
        PrintWriter writer = new PrintWriter(balOutPath, "UTF-8");
        writer.print(payload);
        writer.close();
    }
    
    /**
     * todo.
     *
     * @param protoType .
     * @return .
     */
    private String getMappingBalType(String protoType) {
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
            default: {
                return protoType;
            }
        }
    }
}
