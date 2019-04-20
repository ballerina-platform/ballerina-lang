/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.parser;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

public class SymbolResetter extends BLangNodeVisitor {

    private static final CompilerContext.Key<SymbolResetter> SYMBOL_CLEANER_KEY = new CompilerContext.Key<>();

    public static SymbolResetter getInstance(CompilerContext context) {
        SymbolResetter symbolResetter = context.get(SYMBOL_CLEANER_KEY);
        if (symbolResetter == null) {
            symbolResetter = new SymbolResetter(context);
        }
        return symbolResetter;
    }

    public SymbolResetter(CompilerContext context) {
        context.put(SYMBOL_CLEANER_KEY, this);
    }

    public void resetTopLevelNode(TopLevelNode node) {
        NodeKind kind = node.getKind();

        switch (kind) {
            case IMPORT:
                visit((BLangImportPackage) node);
                break;
            case FUNCTION:
                visit((BLangFunction) node);
                break;
            case TYPE_DEFINITION:
                visit((BLangTypeDefinition) node);
                break;
            case SERVICE:
                visit((BLangService) node);
                break;
            case VARIABLE:
                visit((BLangSimpleVariable) node);
                break;
            case ANNOTATION:
                visit((BLangAnnotation) node);
                break;
            case XMLNS:
                visit((BLangXMLNS) node);
                break;
            case CONSTANT:
                visit((BLangConstant) node);
                break;
            default:
                throw new RuntimeException("Unexpected top level node kind");
        }
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        annotationNode.symbol = null;
    }

    @Override
    public void visit(BLangConstant constant) {
        constant.symbol = null;
    }

    @Override
    public void visit(BLangErrorVariable errorVariable) {
        errorVariable.symbol = null;
    }

    @Override
    public void visit(BLangFunction function) {
        function.symbol = null;
        if (function.receiver != null) {
            visit(function.receiver);
        }

        function.body.stmts.forEach(statement -> statement.accept(this));
    }

    // Top level nodes.
    @Override
    public void visit(BLangImportPackage importPackage) {
        importPackage.symbol = null;
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS localXMLNS) {
        localXMLNS.symbol = null;
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS packageXMLNS) {
        packageXMLNS.symbol = null;
    }

    @Override
    public void visit(BLangRecordVariable recordVariable) {
        recordVariable.symbol = null;
    }

    @Override
    public void visit(BLangService serviceNode) {
        serviceNode.symbol = null;
    }

    // Variable definitions.
    @Override
    public void visit(BLangSimpleVariable simpleVariable) {
        simpleVariable.symbol = null;
        simpleVariable.type = null;
    }

    @Override
    public void visit(BLangTupleVariable tupleVariable) {
        tupleVariable.symbol = null;
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        typeDefinition.symbol = null;
    }

    @Override
    public void visit(BLangExpressionStmt expressionStmt) {
        expressionStmt.expr.accept(this);
    }
}
