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
import io.ballerina.runtime.api.values.BLink;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.OMCommentImpl;

import java.util.Map;
import java.util.Objects;

/**
 * XML nodes containing comment data.
 *
 * @since 1.2.0
 */
public class XMLComment extends XMLNonElementItem {

    private String data;

    public XMLComment(String data) {
        this.data = data;
        this.type = PredefinedTypes.TYPE_COMMENT;
    }

    public XMLComment(String data, boolean readonly) {
        this.data = data;
        this.type = readonly ? PredefinedTypes.TYPE_READONLY_COMMENT : PredefinedTypes.TYPE_COMMENT;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String getTextValue() {
        return data;
    }

    @Override
    public XMLNodeType getNodeType() {
        return XMLNodeType.COMMENT;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        if (isFrozen()) {
            return this;
        }
        return new XMLComment(data);
    }

    @Override
    public OMNode value() {
        OMCommentImpl omComment = new OMCommentImpl();
        omComment.setValue(this.data);
        return omComment;
    }

    @Override
    public String stringValue(BLink parent) {
        return "<!--" + data + "-->";
    }

    @Override
    public String informalStringValue(BLink parent) {
        return "`" + toString() + "`";
    }

    @Override
    public String expressionStringValue(BLink parent) {
        return "xml`" + toString() + "`";
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof XMLComment) {
            XMLComment that = (XMLComment) obj;
            return data.equals(that.data);

        }
        return false;
    }
}
