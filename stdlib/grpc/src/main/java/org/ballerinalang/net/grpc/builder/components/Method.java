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
package org.ballerinalang.net.grpc.builder.components;

import com.google.api.AnnotationsProto;
import com.google.api.HttpRule;
import com.google.protobuf.DescriptorProtos;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.MethodDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.BODY_PARAMETER;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.HTTP_PATTERN_NOT_SET;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.INITIAL_PARENT_PREFIX;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.MESSAGE_PARAMETER;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.PATH_PARAMETER;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.PATH_PARAMETER_PATTERN_REGEX;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.PRIMITIVE_FIELD_NAME;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.QUERY_PARAMETER;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.checkPrimitiveType;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.getMappingBalType;

/**
 * Method definition bean class.
 *
 * @since 0.982.0
 */
public class Method {
    private String methodName;
    private String methodId;
    private String inputType;
    private boolean primitiveInput;
    private String outputType;
    private MethodDescriptor.MethodType methodType;
    private String httpMethod;
    private String httpPath;
    private String httpBody;
    private List<Param> paramSet;

    private Method(String methodName, String methodId, String inputType, boolean primitiveInput, String outputType,
                   MethodDescriptor.MethodType methodType, String httpMethod, String httpPath, String httpBody,
                   List<Param> paramSet) {
        this.methodName = methodName;
        this.methodType = methodType;
        this.methodId = methodId;
        this.inputType = inputType;
        this.primitiveInput = primitiveInput;
        this.outputType = outputType;
        this.httpMethod = httpMethod;
        this.httpPath = httpPath;
        this.httpBody = httpBody;
        this.paramSet = paramSet;
    }

    public static Method.Builder newBuilder(String methodId, Map<String, Message> messageMap) {
        return new Method.Builder(methodId, messageMap);
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodId() {
        return methodId;
    }

    public String getInputType() {
        return inputType;
    }

    public boolean getPrimitiveInput() {
        return primitiveInput;
    }

    public String getOutputType() {
        return outputType;
    }

    public MethodDescriptor.MethodType getMethodType() {
        return methodType;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getHttpPath() {
        return httpPath;
    }

    public String getHttpBody() {
        return httpBody;
    }

    public List<Param> getParamSet() {
        return paramSet;
    }

    public boolean containsEmptyType() {
        return inputType == null || outputType == null;
    }

    /**
     * Method Definition.Builder.
     */
    public static class Builder {
        String methodId;
        DescriptorProtos.MethodDescriptorProto methodDescriptor;
        Map<String, Message> messageMap;

        private Builder(String methodId, Map<String, Message> messageMap) {
            this.methodId = methodId;
            this.messageMap = messageMap;
        }

        public Builder setMethodDescriptor(DescriptorProtos.MethodDescriptorProto methodDescriptor) {
            this.methodDescriptor = methodDescriptor;
            return this;
        }

        public Method build() {
            MethodDescriptor.MethodType methodType = MessageUtils.getMethodType(methodDescriptor);
            String methodName = methodDescriptor.getName();
            String inputType = methodDescriptor.getInputType();
            inputType = inputType != null ? getMappingBalType(inputType) : null;
            boolean primitiveInput = inputType != null && checkPrimitiveType(inputType);
            String outputType = methodDescriptor.getOutputType();
            outputType = outputType != null ? getMappingBalType(outputType) : null;
            HttpRule httpExtension = methodDescriptor.getOptions().getExtension(AnnotationsProto.http);
            String httpMethod = httpExtension.getPatternCase().name();
            String httpPath = resolveHttpPath(httpExtension);
            String httpBody = httpExtension.getBody();
            List<Param> paramSet = getParamList(inputType, messageMap, httpExtension);
            return new Method(methodName, methodId, inputType, primitiveInput, outputType, methodType, httpMethod,
                    httpPath, httpBody, paramSet);
        }

        private String resolveHttpPath(HttpRule httpExtension) {
            int number = httpExtension.getPatternCase().getNumber();
            // the following http methods are mapped thee numbers
            // 0:no extension / 2:get /3:put /4:post /5:delete /6:patch
            switch (number) {
                case 2:
                    return httpExtension.getGet();
                case 3:
                    return httpExtension.getPut();
                case 4:
                    return httpExtension.getPost();
                case 5:
                    return httpExtension.getDelete();
                case 6:
                    return httpExtension.getPatch();
                default:
                    return "";
            }
        }

        private List<Param> getParamList(String inputType, Map<String, Message> messageMap, HttpRule httpExtension) {
            String httpMethod = httpExtension.getPatternCase().name();
            if (httpMethod.equals(HTTP_PATTERN_NOT_SET) || inputType == null) {
               return new ArrayList<>();
            }
            String httpPath = resolveHttpPath(httpExtension);
            String httpBody = httpExtension.getBody();
            String parentName = INITIAL_PARENT_PREFIX + inputType;
            List<String> pathParamList = getPathParamList(httpPath);
            Message inputMessage = messageMap.get(inputType);
            if (inputMessage == null) {
                String paramSource = QUERY_PARAMETER;
                if (pathParamList.stream().anyMatch(pathParam -> pathParam.equals(PRIMITIVE_FIELD_NAME))) {
                    paramSource = PATH_PARAMETER;
                } else if (httpBody.equals(PRIMITIVE_FIELD_NAME)) {
                    paramSource = BODY_PARAMETER;
                }
                Param newParam = new Param(PRIMITIVE_FIELD_NAME, parentName, inputType, paramSource, false);
                List<Param> paramList = new ArrayList<>();
                paramList.add(newParam);
                return paramList;
            }
            return buildParamList(inputMessage, messageMap, parentName, pathParamList, httpBody);
        }

        private List<Param> buildParamList(Message inputMessage, Map<String, Message> messageMap,
                                  String parentName, List<String> pathParamList, String httpBody) {
            List<Param> paramList = new ArrayList<>();
            for (Field inputField : inputMessage.getFieldList()) {
                String fieldName = inputField.getFieldName();
                String fieldType = inputField.getFieldType();
                boolean repeatedParam = false;
                if (inputField.getFieldLabel() != null) {
                    repeatedParam = true;
                }
                String paramSource = QUERY_PARAMETER;
                if (pathParamList.stream().anyMatch(pathParam -> pathParam.equals(fieldName))) {
                    paramSource = PATH_PARAMETER;
                } else if (fieldName.equals(httpBody)) {
                    paramSource = BODY_PARAMETER;
                } else if (messageMap.containsKey(fieldType)) {
                    Message newMessage = messageMap.get(fieldType);
                    String newParentName = parentName + "." + fieldName;
                    List<Param> messageParamList = buildParamList(newMessage, messageMap, newParentName,
                            pathParamList, httpBody);
                    paramList.addAll(messageParamList);
                    paramSource = MESSAGE_PARAMETER;
                }
                Param newParam = new Param(fieldName, parentName, fieldType, paramSource, repeatedParam);
                paramList.add(newParam);
            }
            return paramList;
        }

        private List<String> getPathParamList (String httpPath) {
            List<String> pathList = new ArrayList<>();
            Pattern p = Pattern.compile(PATH_PARAMETER_PATTERN_REGEX);
            Matcher m = p.matcher(httpPath);
            while (m.find()) {
                pathList.add(m.group());
            }
            return pathList;
        }
    }
}
