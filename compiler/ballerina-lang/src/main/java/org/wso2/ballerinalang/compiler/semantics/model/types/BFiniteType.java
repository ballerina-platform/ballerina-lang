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

import io.ballerina.types.SemType;
import org.ballerinalang.model.types.FiniteType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemTypeResolver;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringJoiner;

/**
 * {@code BFiniteType} represents the finite type in Ballerina.
 *
 */
public class BFiniteType extends BType implements FiniteType {

    private Set<BLangExpression> valueSpace;
    private boolean nullable = false;
    public Boolean isAnyData = null;
    private HybridType hybridType;

    public BFiniteType(BTypeSymbol tsymbol) {
        this(tsymbol, new LinkedHashSet<>(), null);
    }

    public BFiniteType(BTypeSymbol tsymbol, Set<BLangExpression> valueSpace) {
        this(tsymbol, valueSpace, null);
    }

    public BFiniteType(BTypeSymbol tsymbol, Set<BLangExpression> valueSpace, SemType semType) {
        super(TypeTags.FINITE, tsymbol, semType);
        this.valueSpace = valueSpace;
        this.flags |= Flags.READONLY;
        this.hybridType = SemTypeResolver.resolveBFiniteTypeHybridType(new ArrayList<>(valueSpace));
    }

    private BFiniteType(Set<BLangExpression> valueSpace) {
        super(TypeTags.FINITE, null, null);
        this.valueSpace = valueSpace;
        this.flags |= Flags.READONLY;
    }

    /**
     * Creates finite type for {@link HybridType#getBTypeComponent}.
     *
     * @param types Constituent types of the intersection
     * @return The created intersection type
     */
    public static BFiniteType createBTypeComponent(Set<BLangExpression> types) {
        return new BFiniteType(types);
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
            switch (value.getBType().tag) {
                case TypeTags.FLOAT:
                    joiner.add(value + "f");
                    break;
                case TypeTags.DECIMAL:
                    joiner.add(value + "d");
                    break;
                case TypeTags.STRING:
                case TypeTags.CHAR_STRING:
                    joiner.add("\"" + value + "\"");
                    break;
                default:
                    joiner.add(value.toString());
            }
        }
        return joiner.toString();
    }

    public void addValue(BLangExpression value) {
        addValue(value, false);
    }

    public void addValue(BLangExpression value, boolean hybridTypeComponent) {
        this.valueSpace.add(value);
        if (!this.nullable && value.getBType() != null &&  value.getBType().isNullable()) {
            this.nullable = true;
        }

        if (!hybridTypeComponent) {
            SemTypeResolver.addBFiniteValue(this, value);
        }
    }

    public HybridType getHybridType() {
        return hybridType;
    }

    public void setHybridType(HybridType hybridType) {
        this.hybridType = hybridType;
    }
}
