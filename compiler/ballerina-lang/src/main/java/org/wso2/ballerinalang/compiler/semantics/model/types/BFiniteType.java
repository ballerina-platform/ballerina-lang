/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.types.FiniteType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringJoiner;

/**
 * {@code BFiniteType} represents the finite type in Ballerina.
 *
 */
public class BFiniteType extends BType implements FiniteType {

    public Set<BLangExpression> valueSpace;

    public BFiniteType(BTypeSymbol tsymbol) {
        super(TypeTags.FINITE, tsymbol);
        valueSpace = new LinkedHashSet<>();
    }

    @Override
    public Set<BLangExpression> getValueSpace() {
        return valueSpace;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.FINITE;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("|");
        this.valueSpace.forEach(value -> joiner.add(value.toString()));
        return joiner.toString();
    }

    @Override
    public boolean hasImplicitInitialValue() {
        // Has NIL element as a member.
        if (valueSpace.stream().anyMatch(value -> value.type.tag == TypeTags.NIL)) {
            return true;
        }
        // All of the element are from same type. And elements contains implicit initial value.
        if (!valueSpace.isEmpty()) {
            BLangExpression firstElement = valueSpace.iterator().next();
            boolean sameType = valueSpace.stream().allMatch(value -> value.type.tag == firstElement.type.tag);
            if (!sameType) {
                return false;
            }

            switch (firstElement.type.tag) {
                case TypeTags.STRING:
                    return containsElement(valueSpace, "\"\"");
                case TypeTags.INT:
                    return containsElement(valueSpace, "0");
                case TypeTags.FLOAT:
                    return containsElement(valueSpace, "0.0");
                case TypeTags.BOOLEAN:
                    return containsElement(valueSpace, "false");
                default:
                    return false;
            }

        }
        return false;
    }

    private boolean containsElement(Set<BLangExpression> valueSpace, String element) {
        return valueSpace.stream().map(v -> (BLangLiteral) v).anyMatch(lit -> lit.originalValue.equals(element));
    }

    @Override
    public String getDesc() {
        return TypeDescriptor.SIG_FINITE + getQualifiedTypeName() + ";";
    }
}
