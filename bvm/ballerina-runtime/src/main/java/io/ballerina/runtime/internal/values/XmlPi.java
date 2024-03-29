/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.values.BLink;
import org.apache.axiom.om.OMNode;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * XML nodes containing processing instructions.
 *
 * @since 1.2.0
 */
public class XmlPi extends XmlNonElementItem {

    private String data;
    private String target;

    public XmlPi(String data, String target) {
        this.data = data;
        this.target = target;
        this.type = PredefinedTypes.TYPE_PROCESSING_INSTRUCTION;
    }

    public XmlPi(String data, String target, boolean readonly) {
        this.data = data;
        this.target = target;
        this.type = readonly ? PredefinedTypes.TYPE_READONLY_PROCESSING_INSTRUCTION :
                PredefinedTypes.TYPE_PROCESSING_INSTRUCTION;
    }

    @Override
    public IteratorValue getIterator() {
        XmlPi that = this;
        return new IteratorValue() {
            boolean read = false;
            @Override
            public boolean hasNext() {
                return !read;
            }

            @Override
            public Object next() {
                if (!read) {
                    this.read = true;
                    return that;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String getItemType() {
        return getNodeType().value();
    }

    @Override
    public String getTextValue() {
        return "";
    }

    public String getData() {
        return data;
    }

    public String getTarget() {
        return target;
    }


    @Override
    public XmlNodeType getNodeType() {
        return XmlNodeType.PI;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        if (isFrozen()) {
            return this;
        }
        return new XmlPi(data, target);
    }

    @Override
    public OMNode value() {
        return this.factory.createOMProcessingInstruction(null, this.target, this.data);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    /**
     * Deep equality check for XML Processing Instruction.
     *
     * @param o The XML on the right hand side
     * @param visitedValues Visited values in order to break cyclic references.
     * @return True if the XML values are equal, else false.
     */
    @Override
    public boolean equals(Object o, Set<ValuePair> visitedValues) {
        if (!(o instanceof XmlPi rhsXMLPi)) {
            return false;
        }
        return this.getData().equals(rhsXMLPi.getData()) && this.getTarget().equals(rhsXMLPi.getTarget());
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, target);
    }

    @Override
    public String stringValue(BLink parent) {
        return "<?" + target + " " + data + "?>";
    }
}
