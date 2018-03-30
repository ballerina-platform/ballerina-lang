/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.types.IntermediateCollectionType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.stream.Collectors;

/**
 * {@code {@link BIntermediateCollectionType }} represents the type of an intermediate tuple Collection returned by an
 * iterable operation.
 *
 * TODO : Fix this with Tuple Type.
 *
 * @since 0.961.0
 */
public class BIntermediateCollectionType extends BType implements IntermediateCollectionType {

    public BTupleType tupleType;

    public BIntermediateCollectionType(BTupleType tupleTypes) {
        super(TypeTags.INTERMEDIATE_COLLECTION, null);
        this.tupleType = tupleTypes;
    }

    @Override
    public BTupleType getTupleType() {
        return tupleType;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.INTERMEDIATE_COLLECTION;
    }

    @Override
    public String toString() {
        return "(" + tupleType.getTupleTypes().stream().map(BType::toString).collect(Collectors.joining(","))
                + ") collection";
    }

}
