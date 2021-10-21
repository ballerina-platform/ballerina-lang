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

import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeHashVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.Comparator;

import static java.util.Objects.hash;

/**
 * BType hash comparator to sort BTypes according its type hash code.
 *
 * @since 2.0.0
 */
public class BTypeHashComparator implements Comparator<BType> {

    private final TypeHashVisitor typeHashVisitor = new TypeHashVisitor();

    @Override
    public int compare(BType o1, BType o2) {
        Integer o1TypeHash = typeHashVisitor.visit(o1);
        typeHashVisitor.reset();
        Integer o2TypeHash = typeHashVisitor.visit(o2);
        typeHashVisitor.reset();
        if (o1TypeHash.equals(o2TypeHash)) {
            o1TypeHash = hash(o1TypeHash, o1.toString());
            o2TypeHash = hash(o2TypeHash, o2.toString());
        }
        return Integer.compare(o1TypeHash, o2TypeHash);
    }
}
