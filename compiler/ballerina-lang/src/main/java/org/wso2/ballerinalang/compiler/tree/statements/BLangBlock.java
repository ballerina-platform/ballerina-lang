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
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.BlockStatementNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * @since 0.94
 */
public class BLangBlock extends BLangStatement implements BlockNode {

    public BLangBlockStmt body;

    public BLangBlock() {
    }

    public BLangBlock(BLangBlockStmt body) {

        this.body = body;
    }

    @Override
    public BLangBlockStmt getBody() {
        return body;
    }

    @Override
    public void setBody(BlockStatementNode body) {
        this.body = (BLangBlockStmt) body;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.BLOCK;
    }

    @Override
    public String toString() {
        return "{"
                + (body != null ? String.valueOf(body) : "") + "}";
    }
}
