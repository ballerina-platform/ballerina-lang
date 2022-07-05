/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.codegen.internal;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeHashVisitor;

import java.util.Comparator;

/**
 * BType hash comparator to sort BIRTypeDefinitions according its type hash code.
 *
 * @since 2.0.0
 */
public class TypeDefHashComparator implements Comparator<BIRTypeDefinition> {

    private final TypeHashVisitor typeHashVisitor;

    public TypeDefHashComparator(TypeHashVisitor typeHashVisitor) {
        this.typeHashVisitor = typeHashVisitor;
    }

    @Override
    public int compare(BIRTypeDefinition o1, BIRTypeDefinition o2) {
        Integer o1TypeHash = typeHashVisitor.getHash(o1.type);
        typeHashVisitor.reset();
        Integer o2TypeHash = typeHashVisitor.getHash(o2.type);
        typeHashVisitor.reset();
        return Integer.compare(o1TypeHash, o2TypeHash);
    }
}
