/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.codelenses;

import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;

/**
 * Find the endpoints visiting functions and services of a BLangCompilationUnit.
 *
 * @since 0.990.3
 */
public class EndpointFindVisitor extends LSNodeVisitor {

    private List<BLangNode> endpoints;

    public EndpointFindVisitor() {
        this.endpoints = new ArrayList<>();
    }

    public List<BLangNode> getEndpoints() {
        return endpoints;
    }

    @Override
    public void visit(BLangCompilationUnit compilationUnit) {
        List<TopLevelNode> topLevelNodes = compilationUnit.getTopLevelNodes();

        topLevelNodes.stream()
                .filter(CommonUtil.checkInvalidTypesDefs())
                .forEach(topLevelNode -> acceptNode((BLangNode) topLevelNode));
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        List<BLangSimpleVariable> variables = resolveEndpoints(varNode);
        this.endpoints.addAll(variables);
    }

    @Override
    public void visit(BLangSimpleVariableDef varNode) {
        if (varNode.var != null) {
            List<BLangSimpleVariable> variables = resolveEndpoints(varNode.var);
            this.endpoints.addAll(variables);
        }
    }

    @Override
    public void visit(BLangFunction funcNode) {
        if (funcNode.body != null) {
            acceptNode(funcNode.body);
        }
    }

    @Override
    public void visit(BLangBlockFunctionBody body) {
        for (BLangStatement stmt : body.stmts) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(BLangService serviceNode) {
        serviceNode.serviceClass.getFunctions().stream()
                .filter(bLangFunction -> (bLangFunction.symbol.flags & Flags.RESOURCE) == Flags.RESOURCE)
                .forEach(this::acceptNode);
    }

    private void acceptNode(BLangNode node) {
        node.accept(this);
    }

    @Deprecated
    private List<BLangSimpleVariable> resolveEndpoints(BLangSimpleVariable variable) {
//        List<BLangSimpleVariable> list = new ArrayList<>();
//        boolean isClientObj = CommonUtil.isClientObject(variable.symbol);
//        if (isClientObj) {
//            list.add(variable);
//        }
//        return list;
        
        return new ArrayList<>();
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        blockNode.stmts.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangIf ifNode) {
        this.acceptNode(ifNode.body);
        if (ifNode.elseStmt != null) {
            this.acceptNode(ifNode.elseStmt);
        }
    }

    @Override
    public void visit(BLangWhile whileNode) {
        this.acceptNode(whileNode.body);
    }

    @Override
    public void visit(BLangWorker workerNode) {
        this.acceptNode(workerNode.body);
    }

    @Override
    public void visit(BLangForeach foreach) {
        this.acceptNode(foreach.body);
    }
}
