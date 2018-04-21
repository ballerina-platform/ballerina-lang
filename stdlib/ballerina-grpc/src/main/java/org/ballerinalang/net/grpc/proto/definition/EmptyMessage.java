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
import com.google.protobuf.Descriptors;
import org.ballerinalang.net.grpc.exception.GrpcServerException;

import java.util.List;

/**
 * Empty Message Definition Builder.
 *
 * @since 1.0.0
 */
public class EmptyMessage extends Message {
    private Descriptors.Descriptor descriptor;

    private EmptyMessage(Descriptors.Descriptor descriptor) {
        this.descriptor = descriptor;
        this.messageName = descriptor.getFullName();
    }

    @Override
    public DescriptorProtos.DescriptorProto getDescriptorProto() {
        if (descriptor != null) {
            return descriptor.toProto();
        }
        return null;
    }

    public static EmptyMessage.Builder newBuilder() {
        return new EmptyMessage.Builder();
    }

    @Override
    public MessageKind getMessageKind() {
        return MessageKind.EMPTY;
    }

    @Override
    public String getDependency() {
        return "google/protobuf/empty.proto";
    }

    /**
     * MessageDefinition.Builder
     */
    public static class Builder {

        public Message build() throws GrpcServerException {
            Descriptors.Descriptor descriptor = getDescriptor();
            return new EmptyMessage(descriptor);
        }

        private Builder() { }

        public Descriptors.Descriptor getDescriptor() throws GrpcServerException {
            List<Descriptors.Descriptor> descriptors = com.google.protobuf.EmptyProto.getDescriptor()
                    .getMessageTypes();
            if (descriptors.size() == 0) {
                throw new GrpcServerException("Error while deriving message definition. Cannot find descriptor for " +
                        "empty message");
            } else {
                return descriptors.get(0);
            }
        }
    }
}
