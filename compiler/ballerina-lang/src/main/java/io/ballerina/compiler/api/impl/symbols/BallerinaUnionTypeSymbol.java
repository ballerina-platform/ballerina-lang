/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.SymbolTransformer;
import io.ballerina.compiler.api.SymbolVisitor;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.types.ComplexSemType;
import io.ballerina.types.Core;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.subtypedata.BooleanSubtype;
import io.ballerina.types.subtypedata.DecimalSubtype;
import io.ballerina.types.subtypedata.FloatSubtype;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.StringSubtype;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.SemNamedType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import static io.ballerina.compiler.api.symbols.TypeDescKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INTERSECTION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NIL;
import static io.ballerina.types.BasicTypeCode.BT_BOOLEAN;
import static io.ballerina.types.BasicTypeCode.BT_DECIMAL;
import static io.ballerina.types.BasicTypeCode.BT_FLOAT;
import static io.ballerina.types.BasicTypeCode.BT_INT;
import static io.ballerina.types.BasicTypeCode.BT_STRING;
import static io.ballerina.types.Core.getComplexSubtypeData;
import static io.ballerina.types.SemTypes.isSubtypeSimple;

/**
 * Represents an union type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaUnionTypeSymbol extends AbstractTypeSymbol implements UnionTypeSymbol {

    private static final String CLONEABLE = "Cloneable";
    private static final String CLONEABLE_TYPE = "CloneableType";
    private static final Pattern pCloneableType = Pattern.compile(CLONEABLE_TYPE);

    private List<TypeSymbol> memberTypes;
    private List<TypeSymbol> originalMemberTypes;
    private String signature;
    private SymbolTable symTable;

    public BallerinaUnionTypeSymbol(CompilerContext context, BUnionType unionType) {
        super(context, TypeDescKind.UNION, unionType);
        this.symTable = SymbolTable.getInstance(context);
    }

    public BallerinaUnionTypeSymbol(CompilerContext context, BFiniteType finiteType) {
        super(context, TypeDescKind.UNION, finiteType);
        this.symTable = SymbolTable.getInstance(context);
    }

    @Override
    public List<TypeSymbol> memberTypeDescriptors() {
        if (this.memberTypes == null) {
            List<TypeSymbol> members = new ArrayList<>();

            if (this.getBType().tag == TypeTags.UNION) {
                TypesFactory typesFactory = TypesFactory.getInstance(this.context);

                for (BType memberType : ((BUnionType) this.getBType()).getMemberTypes()) {
                    if (memberType.tag == TypeTags.TYPEREFDESC || memberType.getKind() != TypeKind.FINITE) {
                        members.add(typesFactory.getTypeDescriptor(memberType));
                        continue;
                    }
                    updateMembersForBFiniteType(members, (BFiniteType) memberType);
                }
            } else {
                updateMembersForBFiniteType(members, (BFiniteType) this.getBType());
            }

            this.memberTypes = Collections.unmodifiableList(members);
        }

        return this.memberTypes;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent") // xxxSubtypeSingleValue() are guaranteed to have a value
    private void updateMembersForBFiniteType(List<TypeSymbol> members, BFiniteType bFiniteType) {
        for (SemNamedType semNamedType : bFiniteType.valueSpace) {
            SemType s = semNamedType.semType();
            BFiniteType ft = BFiniteType.newSingletonBFiniteType(null, s);
            if (PredefinedType.NIL.equals(s)) {
                members.add(new BallerinaSingletonTypeSymbol(context, symTable.nilType, Names.NIL_VALUE.value, ft));
                continue;
            }

            ComplexSemType cs = (ComplexSemType) s;
            BType broadType;
            String value;
            if (isSubtypeSimple(s, PredefinedType.BOOLEAN)) {
                broadType = symTable.booleanType;
                boolean boolVal = BooleanSubtype.booleanSubtypeSingleValue(getComplexSubtypeData(cs, BT_BOOLEAN)).get();
                value = boolVal ? Names.TRUE.value : Names.FALSE.value;
            } else if (isSubtypeSimple(s, PredefinedType.INT)) {
                broadType = symTable.intType;
                long longVal = IntSubtype.intSubtypeSingleValue(getComplexSubtypeData(cs, BT_INT)).get();
                value = Long.toString(longVal);
            } else if (isSubtypeSimple(s, PredefinedType.FLOAT)) {
                broadType = symTable.floatType;
                double doubleVal = FloatSubtype.floatSubtypeSingleValue(getComplexSubtypeData(cs, BT_FLOAT)).get();
                value = Double.toString(doubleVal);
            } else if (isSubtypeSimple(s, PredefinedType.DECIMAL)) {
                broadType = symTable.decimalType;
                BigDecimal bVal = DecimalSubtype.decimalSubtypeSingleValue(getComplexSubtypeData(cs, BT_DECIMAL)).get();
                value = bVal.toPlainString();
            } else if (isSubtypeSimple(s, PredefinedType.STRING)) {
                broadType = symTable.stringType;
                value = StringSubtype.stringSubtypeSingleValue(getComplexSubtypeData(cs, BT_STRING)).get();
            } else {
                throw new IllegalStateException("Unexpected value space type: " + s);
            }

            members.add(new BallerinaSingletonTypeSymbol(context, broadType, value, ft));
        }
    }

    @Override
    public List<TypeSymbol> userSpecifiedMemberTypes() {
        if (this.originalMemberTypes == null) {
            List<TypeSymbol> members = new ArrayList<>();

            if (this.getBType().tag == TypeTags.UNION) {
                TypesFactory typesFactory = TypesFactory.getInstance(this.context);

                for (BType memberType : ((BUnionType) this.getBType()).getOriginalMemberTypes()) {
                    members.add(typesFactory.getTypeDescriptor(memberType));
                }
            } else {
                updateMembersForBFiniteType(members, (BFiniteType) this.getBType());
            }

            this.originalMemberTypes = Collections.unmodifiableList(members);
        }

        return this.originalMemberTypes;
    }

    @Override
    public String signature() {
        if (this.signature != null) {
            return this.signature;
        }

        BType type = this.getBType();
        if (type.tag == TypeTags.UNION) {
            this.signature = getSignatureForUnion(type);
        } else if (type.tag == TypeTags.FINITE) {
            this.signature = getSignatureForFiniteType();
        } else {
            throw new IllegalStateException("Invalid type kind: " + type.getClass().getName());
        }

        return this.signature;
    }

    @Override
    public void accept(SymbolVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(SymbolTransformer<T> transformer) {
        return transformer.transform(this);
    }

    private String getSignatureForUnion(BType type) {
        BUnionType unionType = (BUnionType) type;
        if (unionType.isCyclic && (unionType.tsymbol != null) && !unionType.tsymbol.getName().getValue().isEmpty()) {
            String typeStr;
            typeStr = unionType.tsymbol.getName().getValue();
            if (Symbols.isFlagOn(unionType.getFlags(), Flags.TYPE_PARAM) && pCloneableType.matcher(typeStr).matches()) {
                typeStr = CLONEABLE;
            }
            return typeStr;
        }

        if (unionType.resolvingToString) {
            return "...";
        }

        List<TypeSymbol> memberTypes = this.userSpecifiedMemberTypes();
        if (containsTwoElements(memberTypes) && containsNil(memberTypes)) {
            TypeSymbol member1 = memberTypes.get(0);
            return member1.typeKind() == NIL ? getSignatureForIntersectionType(memberTypes.get(1)) + "?" :
                    getSignatureForIntersectionType(member1) + "?";
        } else {
            StringJoiner joiner = new StringJoiner("|");
            unionType.resolvingToString = true;
            for (TypeSymbol typeDescriptor : memberTypes) {
                // If the member is a function and not the last element, add surrounding parenthesis
                if (typeDescriptor.typeKind() == FUNCTION &&
                        !memberTypes.get(memberTypes.size() - 1).equals(typeDescriptor)) {
                    joiner.add("(" + typeDescriptor.signature() + ")");
                    continue;
                }
                joiner.add(getSignatureForIntersectionType(typeDescriptor));
            }
            unionType.resolvingToString = false;
            return joiner.toString();
        }
    }

    private String getSignatureForFiniteType() {
        List<TypeSymbol> memberTypes = this.userSpecifiedMemberTypes();
        StringJoiner joiner = new StringJoiner("|");
        for (TypeSymbol typeDescriptor : memberTypes) {
            joiner.add(typeDescriptor.signature());
        }
        return joiner.toString();
    }

    private String getSignatureForIntersectionType(TypeSymbol typeSymbol) {
        if (typeSymbol.typeKind() != INTERSECTION) {
            return typeSymbol.signature();
        }
        return "(" + typeSymbol.signature() + ")";
    }

    private boolean containsNil(List<TypeSymbol> types) {
        for (TypeSymbol type : types) {
            if (type.typeKind() == NIL) {
                return true;
            }
        }
        return false;
    }

    private boolean containsTwoElements(List<TypeSymbol> types) {
        if (types.size() == 2) {
            for (TypeSymbol type : types) {
                BType internalType = ((AbstractTypeSymbol) type).getBType();
                if (internalType.tag == TypeTags.FINITE && Core.singleShape(internalType.semType()).isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Optional<Location> getLocation() {
        return Optional.of(this.getBType().tsymbol.pos);
    }
}
