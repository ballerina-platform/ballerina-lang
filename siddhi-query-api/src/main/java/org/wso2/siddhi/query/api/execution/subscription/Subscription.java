/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.query.api.execution.subscription;

import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.execution.ExecutionElement;
import org.wso2.siddhi.query.api.execution.query.output.stream.InsertIntoStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.ReturnStream;
import org.wso2.siddhi.query.api.execution.subscription.map.Mapper;

import java.util.ArrayList;
import java.util.List;

public class Subscription implements ExecutionElement {

    private Transport transport;
    private OutputStream outputStream = new ReturnStream();
    private List<Annotation> annotations = new ArrayList<Annotation>();
    private Mapper mapper;

    private Subscription(Transport transport) {
        this.transport = transport;
    }

    public static Subscription Subscribe(Transport transport) {
        return new Subscription(transport);
    }

    public Subscription map(Mapper mapper) {
        this.mapper = mapper;
        return this;
    }

    public Subscription insertInto(String outputStreamId, OutputStream.OutputEventType outputEventType) {
        this.outputStream = new InsertIntoStream(outputStreamId, outputEventType);
        return this;
    }

    public Subscription insertInto(String outputStreamId) {
        this.outputStream = new InsertIntoStream(outputStreamId);
        return this;
    }

    public Subscription annotation(Annotation annotation) {
        annotations.add(annotation);
        return this;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }


    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public Transport getTransport() {
        return transport;
    }

    public Mapper getMapper() {
        return mapper;
    }

}
