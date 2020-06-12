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

import org.ballerinalang.net.grpc.ServerConnectorPortBindingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service file definition bean class.
 */
public class ServiceFile extends AbstractStub {
    private boolean enableEp = true;
    private String serviceName;
    private List<Method> unaryFunctions = new ArrayList<>();
    private List<Method> streamingFunctions = new ArrayList<>();


    private ServiceFile(String serviceName) {
        this.serviceName = serviceName;
    }

    public static ServiceFile.Builder newBuilder(String serviceName) {
        return new ServiceFile.Builder(serviceName);
    }

    public String getServiceName() {
        return serviceName;
    }

    public List<Method> getUnaryFunctions() {
        return Collections.unmodifiableList(unaryFunctions);
    }

    public List<Method> getStreamingFunctions() {
        return Collections.unmodifiableList(streamingFunctions);
    }

    public void setEnableEp(boolean enableEp) {
        this.enableEp = enableEp;
    }

    public boolean isEnableEp() {
        return enableEp;
    }

    /**
     * Service file definition builder.
     */
    public static class Builder {
        String serviceName;
        List<Method> methodList = new ArrayList<>();

        private static final Logger log = LoggerFactory.getLogger(ServerConnectorPortBindingListener.class);
        private static final PrintStream console = System.err;

        private Builder(String serviceName) {
            this.serviceName = serviceName;
        }

        public ServiceFile.Builder addMethod(Method method) {
            methodList.add(method);
            return this;
        }

        public ServiceFile build() {
            ServiceFile serviceFile = new ServiceFile(serviceName);
            for (Method method : methodList) {
                switch (method.getMethodType()) {
                case UNARY:
                case SERVER_STREAMING:
                    serviceFile.unaryFunctions.add(method);
                    break;
                case CLIENT_STREAMING:
                case BIDI_STREAMING:
                    serviceFile.streamingFunctions.add(method);
                    break;
                default:
                    String message = serviceName + "/" + method.getMethodName() + " is not implemented. Method type " +
                            "is unknown or not supported.";
                    log.error(message);
                    console.println(message);
                }
            }
            return serviceFile;
        }
    }
}
