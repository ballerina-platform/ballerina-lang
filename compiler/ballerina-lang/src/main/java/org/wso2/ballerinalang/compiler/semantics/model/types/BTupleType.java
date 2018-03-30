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

import org.ballerinalang.model.types.TupleType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;
import java.util.stream.Collectors;

/**
 * {@code {@link BTupleType }} represents the tuple type.
 *
 * @since 0.966.0
 */
public class BTupleType extends BType implements TupleType {

    public List<BType> tupleTypes;

    public BTupleType(List<BType> tupleTypes) {
        super(TypeTags.TUPLE, null);
        this.tupleTypes = tupleTypes;
    }

    @Override
    public List<BType> getTupleTypes() {
        return tupleTypes;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.TUPLE;
    }

    @Override
    public String toString() {
        return "(" + tupleTypes.stream().map(BType::toString).collect(Collectors.joining(",")) + ")";
    }

    @Override
    public String getDesc() {
        if (tupleTypes.size() > 1) {
            StringBuilder sig = new StringBuilder(TypeDescriptor.SIG_TUPLE + tupleTypes.size() + ";");
            tupleTypes.forEach(memberType -> sig.append(memberType.getDesc()));
            return sig.toString();
        }
        return tupleTypes.get(0).getDesc();
    }
}
