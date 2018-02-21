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

import com.google.gson.Gson;
import com.google.protobuf.DescriptorProtos;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
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

/**.
 * .
 */
public class BalGenerate {

    public static final Logger LOG = LoggerFactory.getLogger(BalGenerate.class);
    private static final String NEW_LINE_CHARACTER = System.getProperty("line.separator");
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

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
     *
     * @param s .
     * @return .
     */
    private static byte[] hexStringToByteArray(String s) {

        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * todo.
     *
     * @param methodName         .
     * @param reqMessageName     .
     * @param resMessageName     .
     * @param methodID           .
     * @param firstAttributeName .
     * @param firstAttributeType .
     * @return .
     */
    private static String generateActions(String methodName, String reqMessageName, String resMessageName,
                                          int methodID, String firstAttributeName, String firstAttributeType) {

        String str2;
        if (firstAttributeName != null) {
            str2 = "        any value;" + NEW_LINE_CHARACTER +
                    "        if (castErr != null) {" + NEW_LINE_CHARACTER +
                    "            %s p1 = {};" + NEW_LINE_CHARACTER +
                    "            p1.%s,_=(%s)req;" + NEW_LINE_CHARACTER +
                    "            value = p1;" + NEW_LINE_CHARACTER +
                    "        } else {" + NEW_LINE_CHARACTER +
                    "            value = s;" + NEW_LINE_CHARACTER +
                    "        }" + NEW_LINE_CHARACTER;
            str2 = String.format(str2, reqMessageName, firstAttributeName, firstAttributeType);
        } else {
            str2 = "any value = s;" + NEW_LINE_CHARACTER;
        }
        String str = "action %s (grpc:Connection conn, any req) (any,error) {" + NEW_LINE_CHARACTER +
                "        var s, castErr = (%s)req;" + NEW_LINE_CHARACTER +
                "        %s " + NEW_LINE_CHARACTER +
                "        var respons, e = ep.execute(conn, value, %d);" + NEW_LINE_CHARACTER +
                "         if (e != null) {" + NEW_LINE_CHARACTER +
                "            println(\"Error from Stub: \" + e.msg);" + NEW_LINE_CHARACTER +
                "            error err = {msg:e.msg};" + NEW_LINE_CHARACTER +
                "            return null,err;" + NEW_LINE_CHARACTER +
                "        }" + NEW_LINE_CHARACTER +
                "        var responseMap, cEr = (map)respons;" + NEW_LINE_CHARACTER +
                "        if (cEr != null) {" + NEW_LINE_CHARACTER +
                "                       println(\"error: \" + cEr.msg);" + NEW_LINE_CHARACTER +
                "            error err = {msg:cEr.msg};" + NEW_LINE_CHARACTER +
                "            return null,err;" + NEW_LINE_CHARACTER +
                "        }" + NEW_LINE_CHARACTER +
                "        if ((lengthof responseMap) <= 1) {" + NEW_LINE_CHARACTER +
                "            return responseMap.message,null;" + NEW_LINE_CHARACTER +
                "        }" + NEW_LINE_CHARACTER +
                "        var response, conversionErr = <%s>responseMap;" + NEW_LINE_CHARACTER +
                "        if (conversionErr != null) {" + NEW_LINE_CHARACTER +
                "                        println(\"error: \" + conversionErr.msg);" + NEW_LINE_CHARACTER +
                "            error err = {msg:conversionErr.msg};" + NEW_LINE_CHARACTER +
                "            return null,err;" + NEW_LINE_CHARACTER +
                "        }" + NEW_LINE_CHARACTER +
                "        return response,null;" + NEW_LINE_CHARACTER +
                "    }";
        return String.format(str, methodName, reqMessageName, str2, methodID, resMessageName);
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
                "        var resp, convErr = ep.connect(stubType,descriptorMap,descriptorTree);" + NEW_LINE_CHARACTER +
                "        var conn, er = <grpc:Connection>resp;" + NEW_LINE_CHARACTER +
                "        var connErr, er = <grpc:GRPCConnectorError>convErr;" + NEW_LINE_CHARACTER +
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

        String str = "";
        for (int i = 0; i < attributesNameArr.length; i++) {
            str = str + String.format(NEW_LINE_CHARACTER + "%s %s;", attributesTypeArr[i], attributesNameArr[i]);
        }
        return str;
    }

    /**
     * todo.
     *
     * @param num .
     * @return .
     */
    private static String getTypeName(int num) {

        switch (num) {
            case 9: {
                return "string";
            }
            case 8: {
                return "int";
            }
            case 2: {
                return "float";
            }
            case 5: {
                return "boolean";
            }
            default: {
                return "";
            }
        }
    }

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
            BJSON descritorTree = generateDependencyTree(rootDescriptor, descriptorMap);
            generateBaaDefinitions(rootDescriptor, balOutPath, descriptorMap, descritorTree);
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
     * @param rootDescriptor       .
     * @param dependentDescriptors .
     * @return .
     * @throws IOException .
     */
    private BJSON generateDependencyTree(byte[] rootDescriptor, BMap dependentDescriptors) throws IOException {

        String payload = "";
        Gson gson = new Gson();
        InputStream targetStream = new ByteArrayInputStream(rootDescriptor);
        DescriptorProtos.FileDescriptorProto fileDescriptorSet = DescriptorProtos.FileDescriptorProto
                .parseFrom(targetStream);
        ProtoDescriptor protoDescriptor = new ProtoDescriptor(fileDescriptorSet.getPackage() + "." +
                fileDescriptorSet.getName());
        if (fileDescriptorSet.getDependencyCount() != 0) {
            for (int i = 0; i < fileDescriptorSet.getDependencyCount(); i++) {
                // todo: 2/20/18 Move to recursive function
                String descriptorString = dependentDescriptors.get("\"" + fileDescriptorSet.getDependency(i)
                        .replace("/", ".") + "\"").stringValue();
                descriptorString = descriptorString.substring(1, descriptorString.length() - 1);
                targetStream = new ByteArrayInputStream(hexStringToByteArray(descriptorString));
                DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto
                        .parseFrom(targetStream);
                if (proto.getDependencyCount() == 0) {
                    protoDescriptor.addDependency(new ProtoDescriptor(proto.getPackage() + proto.getName()));
                } else {
                    // todo: 2/20/18
                }
            }
        }
        payload = gson.toJson(protoDescriptor);
        return new BJSON(payload);
    }

    /**
     * .
     *
     * @param rootDescriptor .
     * @param balOutPath     .
     * @param descriptorMap  .
     * @param descritorTree  .
     */
    private void generateBaaDefinitions(byte[] rootDescriptor, String balOutPath, BMap descriptorMap,
                                        BJSON descritorTree) {

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
                String strout = methodDescriptorProto.getOutputType().split("\\.")[methodDescriptorProto
                        .getOutputType().split("\\.").length - 1];
                String strIn = methodDescriptorProto.getInputType().split("\\.")[methodDescriptorProto
                        .getInputType().split("\\.").length - 1];

                if (fileDescriptorSet.getMessageTypeList().size() > 0) {
                    actionList = actionList.append(NEW_LINE_CHARACTER).append(generateActions(methodDescriptorProto
                                    .getName(), getMappingBalType(strIn), getMappingBalType
                                    (strout), i, stringObjectMap.get(strIn).getFieldList().get(0).getName(),
                            getTypeName(stringObjectMap.get(strIn).getFieldList().get(0)
                                    .getType().getNumber())));
                } else {
                    actionList = actionList.append(NEW_LINE_CHARACTER).append(generateActions(methodDescriptorProto
                            .getName(), getMappingBalType(strIn), getMappingBalType
                            (strout), i, null, null));
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
                    NEW_LINE_CHARACTER + String.format("json descriptorTree = %s;", descritorTree.toString());
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

        if (protoType.equalsIgnoreCase("DoubleValue") || protoType
                .equalsIgnoreCase("FloatValue")) {
            return "float";
        } else if (protoType.equalsIgnoreCase("Int32Value") || protoType
                .equalsIgnoreCase("Int64Value") || protoType
                .equalsIgnoreCase("UInt32Value") || protoType
                .equalsIgnoreCase("UInt64Value")) {
            return "int";
        } else if (protoType.equalsIgnoreCase("BoolValue")) {
            return "boolean";
        } else if (protoType.equalsIgnoreCase("StringValue")) {
            return "string";
        } else if (protoType.equalsIgnoreCase("BytesValue")) {
            return "blob";
        } else {
            return protoType;
        }
    }
}
