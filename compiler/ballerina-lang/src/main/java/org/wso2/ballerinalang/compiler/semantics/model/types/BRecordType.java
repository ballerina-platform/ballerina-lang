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

import io.ballerina.types.CellAtomicType;
import io.ballerina.types.Core;
import io.ballerina.types.Env;
import io.ballerina.types.SemType;
import io.ballerina.types.definition.Field;
import io.ballerina.types.definition.MappingDefinition;
import org.ballerinalang.model.types.RecordType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_LIMITED;
import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_NONE;
import static io.ballerina.types.PredefinedType.NEVER;
import static io.ballerina.types.PredefinedType.VAL;

/**
 * {@code BRecordType} represents record type in Ballerina.
 *
 * @since 0.971.0
 */
public class BRecordType extends BStructureType implements RecordType {
    private static final String SPACE = " ";
    private static final String RECORD = "record";
    private static final String CLOSE_LEFT = "{|";
    private static final String SEMI = ";";
    private static final String CLOSE_RIGHT = "|}";
    private static final String REST = "...";
    public static final String OPTIONAL = "?";
    public static final String EMPTY = "";
    public static final String READONLY = "readonly";
    public boolean sealed;
    public BType restFieldType;

    public BRecordType mutableType;

    public final Env env;
    private MappingDefinition md = null;

    public BRecordType(Env env, BTypeSymbol tSymbol) {
        super(TypeTags.RECORD, tSymbol);
        this.env = env;
    }

    public BRecordType(Env env, BTypeSymbol tSymbol, long flags) {
        super(TypeTags.RECORD, tSymbol, flags);
        this.env = env;
    }

    /**
     * It is required to reset {@link #md} when the type gets mutated.
     * This method is used for that. e.g. When changing Flags.READONLY
     */
    protected void restMd() {
        md = null;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.RECORD;
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

        if (shouldPrintShape()) {
            // Try to print possible shape. But this may fail with self reference hence avoid .
            StringBuilder sb = new StringBuilder();
            sb.append(RECORD).append(SPACE).append(CLOSE_LEFT);
            for (BField field : fields.values()) {
                sb.append(SPACE);

                if (Symbols.isFlagOn(field.symbol.flags, Flags.READONLY)) {
                    sb.append(READONLY).append(SPACE);
                }

                BType type = field.type;
                if (type == this) {
                    sb.append("...");
                } else {
                    sb.append(type);
                }

                sb.append(SPACE).append(field.name)
                        .append(Symbols.isOptional(field.symbol) ? OPTIONAL : EMPTY).append(SEMI);
            }
            if (sealed) {
                sb.append(SPACE).append(CLOSE_RIGHT);
                return !Symbols.isFlagOn(this.getFlags(), Flags.READONLY) ? sb.toString() :
                        sb.toString().concat(" & readonly");
            }
            sb.append(SPACE).append(restFieldType).append(REST).append(SEMI).append(SPACE).append(CLOSE_RIGHT);
            return !Symbols.isFlagOn(this.getFlags(), Flags.READONLY) ? sb.toString() :
                    sb.toString().concat(" & readonly");
        }
        return this.tsymbol.toString();
    }

    private boolean hasTypeHoles() {
        if (this.fields != null) {
            for (BField member : this.fields.values()) {
                if (member.type instanceof BNoType) {
                    return true;
                }
            }
        }

        // Note: restFieldType will be null/BNoType for closed records
        return false;
    }

    /**
     * When the type is mutated we need to reset the definition used for the semType.
     */
    @Override
    public void resetSemType() {
        md = null;
    }

    // If the member has a semtype component then it will be represented by that component otherwise with never. This
    // means we depend on properly partitioning types to semtype components. Also, we need to ensure member types are
    // "ready" when we call this
    @Override
    public SemType semType() {
        if (md != null) {
            return md.getSemType(env);
        }
        md = new MappingDefinition();
        if (hasTypeHoles()) {
            return md.defineMappingTypeWrapped(env, List.of(), VAL);
        }

        SemType restFieldSemType;
        if (restFieldType == null || restFieldType instanceof BNoType || restFieldType.semType() == null) {
            restFieldSemType = NEVER;
        } else {
            restFieldSemType = restFieldType.semType();
        }

        List<Field> semFields = new ArrayList<>(this.fields.size());
        for (BField field : this.fields.values()) {
            boolean optional = Symbols.isOptional(field.symbol);
            BType bType = field.type;
            SemType ty = bType.semType();
            if (ty == null || NEVER.equals(ty)) {
                if (!optional) {
                    // if there is a non-optional field with `never` type(BType Component + SemType Component),
                    // it is not possible to create a value. Hence, the whole record type is considered as `never`.
                    md.setSemTypeToNever();
                    return NEVER;
                }

                if (Core.isNever(restFieldSemType)) {
                    // record { never x?; never...;} is equivalent to record { never... }
                    // ignore the field
                    continue;
                }
            }
            Field semField = Field.from(field.name.value, ty, Symbols.isFlagOn(field.symbol.flags, Flags.READONLY),
                    optional);
            semFields.add(semField);
        }

        boolean isReadonly = Symbols.isFlagOn(getFlags(), Flags.READONLY);
        CellAtomicType.CellMutability mut = isReadonly ? CELL_MUT_NONE : CELL_MUT_LIMITED;
        return md.defineMappingTypeWrapped(env, semFields, restFieldSemType, mut);
    }

    // This is to ensure call to isNullable won't call semType. In case this is a member of a recursive union otherwise
    // this will have an invalid record type since parent union type call this while it is filling its members
    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public void setFlags(long flags) {
        super.setFlags(flags);
        restMd();
    }

    @Override
    public void addFlags(long flags) {
        super.addFlags(flags);
        restMd();
    }
}
