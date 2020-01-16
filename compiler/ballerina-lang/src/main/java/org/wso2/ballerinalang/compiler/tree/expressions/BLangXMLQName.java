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

import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.XMLQNameNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * Implementation of SimpleVariableReferenceNode.
 *
 * @since 0.94
 */
public class BLangXMLQName extends BLangExpression implements XMLQNameNode {

    public BLangIdentifier localname;
    public BLangIdentifier prefix;
    public String namespaceURI;
    public BXMLNSSymbol nsSymbol;
    public boolean isUsedInXML = true;

    public BLangXMLQName() {    
    }

    public BLangXMLQName(String localname, String prefix) {
        this.localname = new BLangIdentifier();
        this.localname.value = localname;

        this.prefix = new BLangIdentifier();
        this.prefix.value = prefix;
    }

    public BLangXMLQName(BLangIdentifier localname) {
        this.localname = localname;
        this.prefix = new BLangIdentifier();
    }
    
    @Override
    public BLangIdentifier getLocalname() {
        return localname;
    }

    @Override
    public void setLocalname(IdentifierNode localname) {
        this.localname = (BLangIdentifier) localname;
    }

    @Override
    public String getNamespaceUri() {
        return null;
    }

    @Override
    public void setNamespaceUri(String namespaceUri) {
        this.namespaceURI = namespaceUri;
    }

    @Override
    public BLangIdentifier getPrefix() {
        return prefix;
    }

    @Override
    public void setPrefix(IdentifierNode prefix) {
        this.prefix = (BLangIdentifier) prefix;
    }

    @Override
    public String toString() {
        return "BLangXMLQName: " + (prefix == null ? "" : "(" + prefix + ") ") + localname;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.XML_QNAME;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || !(other instanceof BLangXMLQName)) {
            return false;
        }

        BLangXMLQName otherQname = (BLangXMLQName) other;
        return localname.equals(otherQname.localname) && prefix.equals(otherQname.prefix);
    }
}
