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
import io.ballerina.types.Core;
import io.ballerina.types.Env;
import io.ballerina.types.FixedLengthArray;
import io.ballerina.types.ListAtomicType;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import io.ballerina.types.definition.ListDefinition;
import io.ballerina.types.subtypedata.TableSubtype;
import org.ballerinalang.model.types.TableType;
import org.ballerinalang.model.types.TypeKind;
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
    boolean resolving;

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
                if (!keyStringBuilder.toString().equals("")) {
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
        if (resolving || constraint.tag == TypeTags.SEMANTIC_ERROR) {
            // this is to handle negative table recursions. e.g.  type T table<T>
            return PredefinedType.TABLE;
        }
        resolving = true;

        SemType constraintTy = constraint instanceof BParameterizedType p ? p.paramValueType.semType() :
                constraint.semType();
        constraintTy = SemTypes.intersect(constraintTy, PredefinedType.MAPPING);

        Context cx = Context.from(env); // apis calling with 'cx' here are only accessing the env field internally
        if (!fieldNameList.isEmpty()) {
            SemType[] fieldTypes = new SemType[fieldNameList.size()]; // Need to preserve the original order
            for (int i = 0; i < fieldNameList.size(); i++) {
                SemType key = SemTypes.stringConst(fieldNameList.get(i));
                fieldTypes[i] = Core.mappingMemberTypeInnerVal(cx, constraintTy, key);
            }

            SemType normalizedKc;
            if (fieldTypes.length > 1) {
                ListDefinition ld = new ListDefinition();
                normalizedKc = ld.tupleTypeWrapped(env, fieldTypes);
            } else {
                normalizedKc = fieldTypes[0];
            }

            List<String> sortedFieldNames = new ArrayList<>(fieldNameList);
            sortedFieldNames.sort(String::compareTo);
            SemType[] stringConstants = new SemType[sortedFieldNames.size()]; // Need to normalize the order
            for (int i = 0; i < sortedFieldNames.size(); i++) {
                stringConstants[i] = SemTypes.stringConst(sortedFieldNames.get(i));
            }

            SemType normalizedKs = new ListDefinition().tupleTypeWrapped(env, stringConstants);
            resolving = false;
            return TableSubtype.tableContaining(env, constraintTy, normalizedKc, normalizedKs);
        }

        if (keyTypeConstraint != null && keyTypeConstraint.tag != TypeTags.NEVER &&
                keyTypeConstraint.tag != TypeTags.SEMANTIC_ERROR) {
            SemType keyConstraint = keyTypeConstraint.semType();
            SemType normalizedKc;
            ListAtomicType lat = Core.listAtomicType(cx, keyConstraint);
            if (lat != null && PredefinedType.CELL_ATOMIC_UNDEF.equals(Core.cellAtomicType(lat.rest()))) {
                FixedLengthArray members = lat.members();
                normalizedKc = switch (members.fixedLength()) {
                    case 0 -> PredefinedType.VAL;
                    case 1 -> Core.cellAtomicType(members.initial().get(0)).ty();
                    default -> keyConstraint;
                };
            } else {
                normalizedKc = keyConstraint;
            }

            resolving = false;
            return TableSubtype.tableContaining(env, constraintTy, normalizedKc, PredefinedType.VAL);
        }

        resolving = false;
        return TableSubtype.tableContaining(env, constraintTy);
    }
}
