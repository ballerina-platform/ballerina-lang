/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.model.expressions;

import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.WhiteSpaceDescriptor;

import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;

/**
 * {@link XMLElementLiteral} represents an XML literal in Ballerina.
 *
 * @since 0.90
 */
public class XMLElementLiteral extends XMLLiteral {

    private Expression startTagName;
    private Expression endTagName;
    private List<KeyValueExpr> attributes;
    private XMLSequenceLiteral content;
    private XMLElementLiteral parent;
    
    // Namespaces visible to this attribute reference expression.
    private Map<String, Expression> namespaces;
    private Expression defaultNamespaceUri = null;

    public XMLElementLiteral(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, Expression tagName) {
        super(location, whiteSpaceDescriptor);
        this.startTagName = tagName;
    }

    public XMLElementLiteral(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, Expression tagName,
            List<KeyValueExpr> attributes) {
        this(location, whiteSpaceDescriptor, tagName);
        this.attributes = attributes;
    }

    public Expression getStartTagName() {
        return startTagName;
    }

    public void setStartTagName(Expression startTagName) {
        this.startTagName = startTagName;
    }

    public Expression getEndTagName() {
        return endTagName;
    }

    public void setEndTagName(Expression endTagName) {
        this.endTagName = endTagName;
    }

    public List<KeyValueExpr> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<KeyValueExpr> attributes) {
        this.attributes = attributes;
    }

    public XMLSequenceLiteral getContent() {
        return content;
    }

    public void setContent(XMLSequenceLiteral content) {
        this.content = content;
    }

    public Map<String, Expression> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Map<String, Expression> namespaces) {
        Expression defaultNs = namespaces.remove(XMLConstants.DEFAULT_NS_PREFIX);
        if (defaultNs != null) {
            defaultNamespaceUri = defaultNs;
        }
        this.namespaces = namespaces;
    }

    public Expression getDefaultNamespaceUri() {
        return defaultNamespaceUri;
    }

    public void setDefaultNamespaceUri(Expression defaultNamespaceUri) {
        this.defaultNamespaceUri = defaultNamespaceUri;
    }

    public XMLElementLiteral getParent() {
        return parent;
    }

    public void setParent(XMLElementLiteral parent) {
        this.parent = parent;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
