/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree.clauses;

import org.ballerinalang.model.clauses.LetClauseNode;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of "let" clause statement.
 *
 * @since 1.2.0
 */
public class BLangLetClause extends BLangNode implements LetClauseNode {

    public List<BLangLetVariable> getLetVarDeclarations() {
        return letVarDeclarations;
    }

    public void addLetVarDeclarations(List<BLangLetVariable> letVarDeclarations) {
        this.letVarDeclarations = letVarDeclarations;
    }

    public List<BLangLetVariable> letVarDeclarations = new ArrayList<>();

    public BLangLetClause() {
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.LET;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "let " + letVarDeclarations.stream().map(BLangSimpleVariable::toString)
                .collect(Collectors.joining("\n"));
    }
}
