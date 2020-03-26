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

import org.ballerinalang.model.Name;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.IndexBasedAccessNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @since 0.94
 */
@Deprecated
public class BLangXMLAttributeAccess extends BLangIndexBasedAccess implements IndexBasedAccessNode {

    public Map<Name, BXMLNSSymbol> namespaces;

    public BLangXMLAttributeAccess() {
        namespaces = new LinkedHashMap<Name, BXMLNSSymbol>();
    }

    @Override
    public String toString() {
        if (indexExpr == null) {
            return String.valueOf(expr) + "@";
        }

        return String.valueOf(expr) + "@[" + String.valueOf(indexExpr) + "]";
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.XML_ATTRIBUTE_ACCESS_EXPR;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }
}
