/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.XMLQNameNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * Implementation of SimpleVariableReferenceNode.
 *
 * @since 0.94
 */
public class BLangXMLQName extends BLangExpression implements XMLQNameNode {

    public String localname;
    public String namespaceUri;
    public String prefix;

    @Override
    public String getLocalname() {
        return localname;
    }

    @Override
    public void setLocalname(String localname) {
        this.localname = localname;
    }

    @Override
    public String getNamespaceUri() {
        return namespaceUri;
    }

    @Override
    public void setNamespaceUri(String namespaceUri) {
        this.namespaceUri = namespaceUri;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return "BLangXMLQName: " + (namespaceUri == null ? "" : "(" + namespaceUri + ") ") +
                (prefix == null ? "" : "(" + prefix + ") ") + localname;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.XML_QNAME;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }
}
