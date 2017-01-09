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
package org.wso2.siddhi.query.api.execution.query.output.stream;

import org.wso2.siddhi.query.api.execution.io.Transport;
import org.wso2.siddhi.query.api.execution.io.map.Mapping;

public class PublishStream extends OutputStream {

    private Transport transport;
    private Mapping mapping;

    public PublishStream(Transport transport, Mapping mapping) {
        this.transport = transport;
        this.mapping = mapping;
    }

    public PublishStream(Transport transport, OutputEventType outputEventType, Mapping mapping) {
        this(transport, mapping);
        this.outputEventType = outputEventType;
    }

    public Transport getTransport() {
        return transport;
    }

    public Mapping getMapping() {
        return mapping;
    }

    @Override
    public String toString() {
        return "PublishStream{" +
                "transport=" + transport +
                ", outputEventType=" + outputEventType +
                ", mapping=" + mapping +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PublishStream that = (PublishStream) o;
        if (transport != null ? !transport.equals(that.transport) : that.transport != null) return false;
        if (outputEventType != that.outputEventType) return false;
        return !(mapping != null ? !mapping.equals(that.mapping) : that.mapping != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (transport != null ? transport.hashCode() : 0);
        result = 31 * result + (outputEventType != null ? outputEventType.hashCode() : 0);
        result = 31 * result + (mapping != null ? mapping.hashCode() : 0);
        return result;
    }
}
