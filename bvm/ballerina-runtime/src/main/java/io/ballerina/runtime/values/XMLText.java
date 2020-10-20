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
package io.ballerina.runtime.values;

import io.ballerina.runtime.XMLNodeType;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BXML;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.CharacterDataImpl;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import static io.ballerina.runtime.util.BLangConstants.STRING_NULL_VALUE;

/**
 * XML nodes containing atomic content such as text, comment and processing instructions.
 *
 * @since 1.2.0
 */
public class XMLText extends XMLNonElementItem {

    private String data;

    public XMLText(String data) {
        // data is the content of xml comment or text node
        this.data = data;
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public String getItemType() {
        return getNodeType().value();
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public String getTextValue() {
        return data;
    }

    @Override
    public BXML strip() {
        if (!data.trim().isEmpty()) {
            return this;
        }
        return new XMLText("");
    }

    @Override
    public XMLNodeType getNodeType() {
        return XMLNodeType.TEXT;
    }

    @Override
    public OMNode value() {
        CharacterDataImpl characterData = new CharacterDataImpl();
        characterData.data = this.data;
        return characterData;
    }

    @Override
    public String stringValue(BLink parent) {
        try {
            return data;
        } catch (Throwable t) {
            handleXmlException("failed to get xml as string: ", t);
        }
        return STRING_NULL_VALUE;
    }

    @Override
    public IteratorValue getIterator() {
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
                    return data;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof XMLText) {
            XMLText that = (XMLText) obj;
            return data.equals(that.data);

        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public Type getType() {
        return PredefinedTypes.TYPE_TEXT;
    }
}
