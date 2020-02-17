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
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

/**
 * {@code BFiniteType} represents the finite type in Ballerina.
 *
 */
public class BFiniteType extends BType implements FiniteType {

    private Set<BLangExpression> valueSpace;
    private boolean nullable = false;
    private Optional<Boolean> isAnyData = Optional.empty();


    public BFiniteType(BTypeSymbol tsymbol) {
        super(TypeTags.FINITE, tsymbol);
        valueSpace = new LinkedHashSet<>();
    }

    public BFiniteType(BTypeSymbol tsymbol, Set<BLangExpression> valueSpace) {
        super(TypeTags.FINITE, tsymbol);
        this.valueSpace = valueSpace;
    }

    @Override
    public Set<BLangExpression> getValueSpace() {
        return Collections.unmodifiableSet(valueSpace);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.FINITE;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("|");
        for (BLangExpression value : this.valueSpace) {
            if (value.type.tag == TypeTags.FLOAT) {
                joiner.add(value.toString() + "f");
            } else if (value.type.tag == TypeTags.DECIMAL) {
                joiner.add(value.toString() + "d");
            } else {
                joiner.add(value.toString());
            }
        }
        return joiner.toString();
    }

    @Override
    public String getDesc() {
        return TypeDescriptor.SIG_FINITE + getQualifiedTypeName() + ";";
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public boolean isAnydata() {
        if (this.isAnyData.isPresent()) {
            return this.isAnyData.get();
        }

        for (BLangExpression value : this.valueSpace) {
            if (!value.type.isAnydata()) {
                this.isAnyData = Optional.of(false);
                return false;
            }
        }

        this.isAnyData = Optional.of(true);
        return true;
    }

    public void addValue(BLangExpression value) {
        this.valueSpace.add(value);
        if (!nullable && (value.type.tag == TypeTags.NIL)) {
            nullable = true;
        }
    }
}
