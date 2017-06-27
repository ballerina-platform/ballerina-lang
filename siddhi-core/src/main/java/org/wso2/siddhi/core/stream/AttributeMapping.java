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

package org.wso2.siddhi.core.stream;

/**
 * Holder object to store payloadMapping information for a given Siddhi
 * {@link org.wso2.siddhi.query.api.definition.Attribute}
 */
public class AttributeMapping {
    private String rename;
    private String payloadMapping = null;
    private String transportMapping = null;

    public AttributeMapping(String rename, String mapping) {
        this.rename = rename;
        if (mapping.trim().startsWith("trp:")) {
            this.transportMapping = mapping.trim().substring(4);
        } else {
            this.payloadMapping = mapping;
        }
    }

    public String getPayloadMapping() {
        return payloadMapping;
    }

    public String getRename() {
        return rename;
    }

    public String getTransportMapping() {
        return transportMapping;
    }

    @Override
    public String toString() {
        return "AttributeMapping{" +
                "rename='" + rename + '\'' +
                ", payloadMapping='" + payloadMapping + '\'' +
                ", transportMapping='" + transportMapping + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AttributeMapping that = (AttributeMapping) o;

        if (rename != null ? !rename.equals(that.rename) : that.rename != null) {
            return false;
        }
        if (payloadMapping != null ? !payloadMapping.equals(that.payloadMapping) : that.payloadMapping != null) {
            return false;
        }
        return transportMapping != null ? transportMapping.equals(that.transportMapping) : that.transportMapping ==
                null;
    }

    @Override
    public int hashCode() {
        int result = rename != null ? rename.hashCode() : 0;
        result = 31 * result + (payloadMapping != null ? payloadMapping.hashCode() : 0);
        result = 31 * result + (transportMapping != null ? transportMapping.hashCode() : 0);
        return result;
    }
}
