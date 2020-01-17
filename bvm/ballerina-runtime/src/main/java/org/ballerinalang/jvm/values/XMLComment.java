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
package org.ballerinalang.jvm.values;

import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.OMCommentImpl;
import org.ballerinalang.jvm.XMLNodeType;

/**
 * XML nodes containing comment data.
 *
 * @since 1.2.0
 */
public class XMLComment extends XMLNonElementItem {

    private String data;

    public XMLComment(String data) {
        this.data = data;
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
    public OMNode value() {
        OMCommentImpl omComment = new OMCommentImpl();
        omComment.setValue(this.data);
        return omComment;
    }

    @Override
    public String stringValue() {
        return "<!-- " + data + " -->";
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
