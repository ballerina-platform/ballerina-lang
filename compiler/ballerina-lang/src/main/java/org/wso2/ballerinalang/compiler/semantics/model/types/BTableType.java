/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.types.Context;
import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import org.ballerinalang.model.types.TableType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code BTableType} represents a table in Ballerina.
 *
 * @since 1.3.0
 */
public class BTableType extends BType implements TableType {
    public BType constraint;
    public BType keyTypeConstraint;
    public List<String> fieldNameList = new ArrayList<>();
    public Location keyPos;
    public boolean isTypeInlineDefined;
    public Location constraintPos;

    public BTableType mutableType;
    public final Env env;

    public BTableType(Env env, int tag, BType constraint, BTypeSymbol tSymbol) {
        super(tag, tSymbol);
        this.constraint = constraint;
        this.env = env;
    }

    public BTableType(Env env, int tag, BType constraint, BTypeSymbol tSymbol, long flags) {
        super(tag, tSymbol, flags);
        this.constraint = constraint;
        this.env = env;
    }

    public BType getConstraint() {
        return this.constraint;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        boolean readonly = Symbols.isFlagOn(getFlags(), Flags.READONLY);
        if (constraint == null) {
            return readonly ? super.toString().concat(" & readonly") : super.toString();
        }

        StringBuilder keyStringBuilder = new StringBuilder();
        String stringRep;
        if (!fieldNameList.isEmpty()) {
            for (String fieldName : fieldNameList) {
                if (!keyStringBuilder.toString().isEmpty()) {
                    keyStringBuilder.append(", ");
                }
                keyStringBuilder.append(fieldName);
            }
            stringRep = super.toString() + "<" + constraint + "> key(" + keyStringBuilder.toString() + ")";
        } else {
            stringRep = (super.toString() + "<" + constraint + "> " +
                                 ((keyTypeConstraint != null) ? ("key<" + keyTypeConstraint + ">") : "")).trim();
        }

        return readonly ? stringRep.concat(" & readonly") : stringRep;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.TABLE;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public SemType semType() {
        boolean readonly = Symbols.isFlagOn(this.getFlags(), Flags.READONLY);
        SemType s = semTypeInner();
        return readonly ? SemTypes.intersect(PredefinedType.VAL_READONLY, s) : s;
    }

    private SemType semTypeInner() {
        BType constraintType = Types.getReferredType(constraint);
        if (constraintType.tag == TypeTags.TABLE || constraintType.tag == TypeTags.SEMANTIC_ERROR) {
            // this is to handle negative table recursions. e.g.  type T table<T>
            return PredefinedType.TABLE;
        } else if (constraintType instanceof BIntersectionType intersectionType) {
            for (BType memberType : intersectionType.getConstituentTypes()) {
                if (Types.getReferredType(memberType).tag == TypeTags.TABLE) {
                    // Negative scenario
                    return PredefinedType.TABLE;
                }
            }
        }

        SemType tableConstraint = constraintType instanceof BParameterizedType p ? p.paramValueType.semType() :
                constraintType.semType();
        tableConstraint = SemTypes.intersect(tableConstraint, PredefinedType.MAPPING);

        Context cx = Context.from(env); // apis calling with 'cx' here are only accessing the env field internally
        String[] fieldNames = fieldNameList.toArray(String[]::new);
        if (!fieldNameList.isEmpty()) {
            return SemTypes.tableContainingKeySpecifier(cx, tableConstraint, fieldNames);
        }

        if (keyTypeConstraint != null && keyTypeConstraint.tag != TypeTags.NEVER &&
                keyTypeConstraint.tag != TypeTags.SEMANTIC_ERROR) {
            SemType keyConstraint = keyTypeConstraint.semType();
            return SemTypes.tableContainingKeyConstraint(cx, tableConstraint, keyConstraint);
        }

        return SemTypes.tableContaining(env, tableConstraint);
    }
}
