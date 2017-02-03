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

package org.wso2.siddhi.query.api.execution;

import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.execution.io.Transport;
import org.wso2.siddhi.query.api.execution.query.output.ratelimit.OutputRate;
import org.wso2.siddhi.query.api.execution.query.output.stream.InsertIntoStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.ReturnStream;
import org.wso2.siddhi.query.api.execution.io.map.Mapping;

import java.util.ArrayList;
import java.util.List;

public class Subscription implements ExecutionElement {

    private Transport transport;
    private OutputStream outputStream = new ReturnStream();
    private List<Annotation> annotations = new ArrayList<Annotation>();
    private Mapping mapping;
    private OutputRate outputRate;

    private Subscription(Transport transport) {
        this.transport = transport;
    }

    public static Subscription Subscribe(Transport transport) {
        return new Subscription(transport);
    }

    public Subscription map(Mapping mapping) {
        this.mapping = mapping;
        return this;
    }

    public Subscription outStream(OutputStream outputStream) {
        this.outputStream = outputStream;
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

    public void output(OutputRate outputRate) {
        this.outputRate = outputRate;
    }

    public OutputRate getOutputRate() {
        return outputRate;
    }


    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public Transport getTransport() {
        return transport;
    }

    public Mapping getMapping() {
        return mapping;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subscription that = (Subscription) o;

        if (transport != null ? !transport.equals(that.transport) : that.transport != null) return false;
        if (outputStream != null ? !outputStream.equals(that.outputStream) : that.outputStream != null) return false;
        if (annotations != null ? !annotations.equals(that.annotations) : that.annotations != null) return false;
        if (mapping != null ? !mapping.equals(that.mapping) : that.mapping != null) return false;
        return !(outputRate != null ? !outputRate.equals(that.outputRate) : that.outputRate != null);

    }

    @Override
    public int hashCode() {
        int result = transport != null ? transport.hashCode() : 0;
        result = 31 * result + (outputStream != null ? outputStream.hashCode() : 0);
        result = 31 * result + (annotations != null ? annotations.hashCode() : 0);
        result = 31 * result + (mapping != null ? mapping.hashCode() : 0);
        result = 31 * result + (outputRate != null ? outputRate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "transport=" + transport +
                ", outputStream=" + outputStream +
                ", annotations=" + annotations +
                ", mapping=" + mapping +
                ", outputRate=" + outputRate +
                '}';
    }
}
