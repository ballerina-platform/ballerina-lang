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
import org.ballerinalang.model.tree.statements.TryCatchFinallyNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.94
 *
 * @deprecated since 0.983.0, use trap instead.
 */
@Deprecated
public class BLangTryCatchFinally extends BLangStatement implements TryCatchFinallyNode {
    public BLangBlockStmt tryBody;
    public List<BLangCatch> catchBlocks;
    public BLangBlockStmt finallyBody;

    public BLangTryCatchFinally() {
        catchBlocks = new ArrayList<>();
    }

    public BLangTryCatchFinally(BLangBlockStmt tryBody, List<BLangCatch> catchBlocks, BLangBlockStmt finallyBody) {
        this.tryBody = tryBody;
        this.catchBlocks = catchBlocks;
        this.finallyBody = finallyBody;
    }

    @Override
    public BLangBlockStmt getBody() {
        return tryBody;
    }

    @Override
    public List<BLangCatch> getCatchBlocks() {
        return catchBlocks;
    }

    @Override
    public BLangBlockStmt getFinallyBody() {
        return finallyBody;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.TRY;
    }

    @Override
    public String toString() {
        String catchString = "";
        for (BLangCatch catchNode : catchBlocks) {
            catchString += String.valueOf(catchNode);
        }
        return "try{" + String.valueOf(tryBody) + "}" + catchString +
                (finallyBody != null ? "finally{" + String.valueOf(finallyBody) + "}" : "");
    }
}
