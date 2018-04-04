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
package org.ballerinalang.net.grpc.proto.definition;

import com.google.protobuf.DescriptorProtos;
import org.ballerinalang.net.grpc.proto.ServiceProtoConstants;

/**
 * Method Definition Builder.
 *
 * @since 1.0.0
 */
public class Method {
    private DescriptorProtos.MethodDescriptorProto methodDescriptor;

    private Method(DescriptorProtos.MethodDescriptorProto methodDescriptor) {
        this.methodDescriptor = methodDescriptor;
    }

    public static Method.Builder newBuilder(String serviceName) {
        return new Method.Builder(serviceName);
    }

    public DescriptorProtos.MethodDescriptorProto getMethodDescriptor() {
        return methodDescriptor;
    }

    public String getMethodDefinition() {

        return "\trpc " + methodDescriptor.getName() + "(" + (methodDescriptor
                .getClientStreaming() ? "stream " : "") +
                methodDescriptor.getInputType() + ") returns (" +
                (methodDescriptor.getServerStreaming() ? "stream " : "") + methodDescriptor.getOutputType() +
                ")" + ";" + ServiceProtoConstants.NEW_LINE_CHARACTER;
    }

    /**
     * MethodDefinition.Builder
     */
    public static class Builder {

        public Method build() {
            return new Method(methodBuilder.build());
        }

        public Builder setClientStreaming(boolean flag) {
            methodBuilder.setClientStreaming(flag);
            return this;
        }

        public Builder setServerStreaming(boolean flag) {
            methodBuilder.setServerStreaming(flag);
            return this;
        }

        public Builder setInputType(String inputType) {
            methodBuilder.setInputType(inputType);
            return this;
        }

        public Builder setOutputType(String outputType) {
            methodBuilder.setOutputType(outputType);
            return this;
        }

        private Builder(String methodName) {
            methodBuilder = DescriptorProtos.MethodDescriptorProto.newBuilder();
            methodBuilder.setName(methodName);
        }

        private DescriptorProtos.MethodDescriptorProto.Builder methodBuilder;
    }
}
