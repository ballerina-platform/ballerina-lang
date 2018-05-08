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

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.google.protobuf.DescriptorProtos;
import org.ballerinalang.net.grpc.exception.UnsupportedFieldTypeException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Util functions which are use when generating . bal stub
 */
public class BalGenerationUtils {
    private static final String TEMPLATES_SUFFIX = ".mustache";
    private static final String TEMPLATES_DIR_PATH_KEY = "templates.dir.path";
    private static final String DEFAULT_TEMPLATE_DIR = File.separator + "templates";
    public static final String DEFAULT_SKELETON_DIR = DEFAULT_TEMPLATE_DIR + File.separator + "skeleton";
    public static final String DEFAULT_SAMPLE_DIR = DEFAULT_TEMPLATE_DIR + File.separator + "skeleton";
    public static final String SKELETON_TEMPLATE_NAME = "clientStub";
    public static final String SAMPLE_TEMPLATE_NAME = "sample";
    
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
            hexChars[j * 2] = "0123456789ABCDEF".toCharArray()[v >>> 4];
            hexChars[j * 2 + 1] = "0123456789ABCDEF".toCharArray()[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    /**
     * This function returns the ballerina data type which is mapped to  protobuf data type.
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
     * Method is responsible of writing the bal string payload to .bal file.
     *
     * @param payload    .
     * @param balOutPath .
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static void writeFile(String payload, Path balOutPath) throws FileNotFoundException,
            UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(balOutPath.toFile(), "UTF-8");
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
    /**
     * Method is for getting ballerina data type which is map to the .proto data type.
     *
     * @param num .
     * @return .
     */
    public static String getLabelName(int num) {
        switch (num) {
            case DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED_VALUE: {
                return "[]";
            }
            case DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL_VALUE: {
                return null;
            }
            case DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED_VALUE: {
                throw new UnsupportedFieldTypeException("Required type is not supported yet." + num);
            }
            default: {
                throw new UnsupportedFieldTypeException("Error while decoding request message. Field " +
                        "label is not supported : " + num);
            }
        }
    }
    /**
     * Write ballerina definition of a <code>object</code> to a file as described by <code>template.</code>
     *
     * @param object       Context object to be used by the template parser
     * @param templateDir  Directory with all the templates required for generating the source file
     * @param templateName Name of the parent template to be used
     * @param outPath      Destination path for writing the resulting source file
     * @throws IOException when file operations fail
     */
    public static void writeBallerina(Object object, String templateDir, String templateName, String outPath)
            throws IOException {
        PrintWriter writer = null;
        try {
            Template template = compileTemplate(templateDir, templateName);
            Context context = Context.newBuilder(object).resolver(FieldValueResolver.INSTANCE).build();
            writer = new PrintWriter(outPath, "UTF-8");
            writer.println(template.apply(context));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
    
    private static Template compileTemplate(String defaultTemplateDir, String templateName) throws IOException {
        String templatesDirPath = System.getProperty(TEMPLATES_DIR_PATH_KEY, defaultTemplateDir);
        ClassPathTemplateLoader cpTemplateLoader = new ClassPathTemplateLoader((templatesDirPath));
        FileTemplateLoader fileTemplateLoader = new FileTemplateLoader(templatesDirPath);
        cpTemplateLoader.setSuffix(TEMPLATES_SUFFIX);
        fileTemplateLoader.setSuffix(TEMPLATES_SUFFIX);
        
        Handlebars handlebars = new Handlebars().with(cpTemplateLoader, fileTemplateLoader);
        handlebars.registerHelpers(StringHelpers.class);
        handlebars.registerHelper("equals", (object, options) -> {
            CharSequence result;
            Object param0 = options.param(0);
            
            if (param0 == null) {
                throw new IllegalArgumentException("found n'null', expected 'string'");
            }
            if (object != null && object.toString().equals(param0.toString())) {
                result = options.fn(options.context);
            } else {
                result = null;
            }
            
            return result;
        });
        
        return handlebars.compile(templateName);
    }
}
