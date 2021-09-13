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
public abstract class BLangNodeTransformer<T, R> {

    public abstract R transformNode(BLangNode node, T props);

    // Base Nodes

    public R transform(BLangAnnotation node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangAnnotationAttachment node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangBlockFunctionBody node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangClassDefinition node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangCompilationUnit node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangErrorVariable node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangExprFunctionBody node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangExternalFunctionBody node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangFunction node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangIdentifier node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangImportPackage node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangMarkdownDocumentation node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangMarkdownReferenceDocumentation node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangPackage node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangRecordVariable node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangResourceFunction node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangRetrySpec node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangService node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangSimpleVariable node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTableKeySpecifier node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTableKeyTypeConstraint node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTestablePackage node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTupleVariable node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTypeDefinition node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLNS node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLNS.BLangLocalXMLNS node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLNS.BLangPackageXMLNS node, T props) {
        return transformNode(node, props);
    }

    // Types

    public R transform(BLangArrayType node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangBuiltInRefTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangConstrainedType node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangErrorType node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangFiniteTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangFunctionTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangIntersectionTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangObjectTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangRecordTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangStreamType node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTableTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTupleTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangUnionTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangUserDefinedType node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangValueType node, T props) {
        return transformNode(node, props);
    }
}
