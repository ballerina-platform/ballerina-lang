/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree;

import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangCaptureBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorCauseBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorFieldBindingPatterns;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorMessageBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangFieldBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangListBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangMappingBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangNamedArgBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangRestBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangSimpleBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangWildCardBindingPattern;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimitClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangMatchClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnConflictClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderKey;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangIntersectionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStreamType;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

/**
 * TODO: Fix me.
 *
 * @since 2.0.0
 */
public abstract class BLangNodeAnalyzer<T> {

    public abstract void analyzeNode(BLangNode node, T props);

    // Base Nodes

    public void visit(BLangAnnotation node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangAnnotationAttachment node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangBlockFunctionBody node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangClassDefinition node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangCompilationUnit node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangErrorVariable node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangExprFunctionBody node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangExternalFunctionBody node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangFunction node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangIdentifier node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangImportPackage node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangMarkdownDocumentation node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangMarkdownReferenceDocumentation node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangPackage node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangRecordVariable node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangResourceFunction node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangRetrySpec node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangService node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangSimpleVariable node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangTableKeySpecifier node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangTableKeyTypeConstraint node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangTestablePackage node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangTupleVariable node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangTypeDefinition node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangXMLNS node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangXMLNS.BLangLocalXMLNS node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangXMLNS.BLangPackageXMLNS node, T props) {
        analyzeNode(node, props);
    }

    // Binding-patterns

    public void visit(BLangCaptureBindingPattern node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangErrorBindingPattern node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangErrorCauseBindingPattern node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangErrorFieldBindingPatterns node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangErrorMessageBindingPattern node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangFieldBindingPattern node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangListBindingPattern node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangMappingBindingPattern node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangNamedArgBindingPattern node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangRestBindingPattern node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangSimpleBindingPattern node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangWildCardBindingPattern node, T props) {
        analyzeNode(node, props);
    }

    // Clauses

    public void visit(BLangDoClause node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangFromClause node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangJoinClause node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangLetClause node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangLimitClause node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangMatchClause node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangOnClause node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangOnConflictClause node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangOnFailClause node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangOrderByClause node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangOrderKey node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangSelectClause node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangWhereClause node, T props) {
        analyzeNode(node, props);
    }

    // Types

    public void visit(BLangArrayType node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangBuiltInRefTypeNode node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangConstrainedType node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangErrorType node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangFiniteTypeNode node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangFunctionTypeNode node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangIntersectionTypeNode node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangObjectTypeNode node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangRecordTypeNode node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangStreamType node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangTableTypeNode node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangTupleTypeNode node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangUnionTypeNode node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangUserDefinedType node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangValueType node, T props) {
        analyzeNode(node, props);
    }
}
