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
package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.CatchNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;

/**
 * @since 0.94
 *
 * @deprecated since 0.983.0, use trap instead.
 */
@Deprecated
public class BLangCatch extends BLangNode implements CatchNode {
    public BLangSimpleVariable param;
    public BLangBlockStmt body;

    public BLangCatch() {
    }

    public BLangCatch(BLangSimpleVariable param, BLangBlockStmt body) {
        this.param = param;
        this.body = body;
    }

    @Override
    public BLangSimpleVariable getParameter() {
        return param;
    }

    @Override
    public BLangBlockStmt getBody() {
        return body;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.CATCH;
    }

    @Override
    public String toString() {
        return NodeKind.CATCH + "(" + String.valueOf(param) + "){" + String.valueOf(body) + "}";
    }
}
