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
import io.ballerina.types.Core;
import io.ballerina.types.EnumerableCharString;
import io.ballerina.types.EnumerableDecimal;
import io.ballerina.types.EnumerableFloat;
import io.ballerina.types.EnumerableString;
import io.ballerina.types.EnumerableType;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.subtypedata.AllOrNothingSubtype;
import io.ballerina.types.subtypedata.BooleanSubtype;
import io.ballerina.types.subtypedata.CharStringSubtype;
import io.ballerina.types.subtypedata.DecimalSubtype;
import io.ballerina.types.subtypedata.FloatSubtype;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.NonCharStringSubtype;
import io.ballerina.types.subtypedata.Range;
import io.ballerina.types.subtypedata.StringSubtype;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import static io.ballerina.compiler.api.symbols.TypeDescKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INTERSECTION;
import static io.ballerina.compiler.api.symbols.TypeDescKind.NIL;

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
                    updateMembersForBFiniteType(members, memberType);
                }
            } else {
                updateMembersForBFiniteType(members, this.getBType());
            }

            this.memberTypes = Collections.unmodifiableList(members);
        }

        return this.memberTypes;
    }

    private void updateMembersForBFiniteType(List<TypeSymbol> members, BType bFiniteType) {
        assert bFiniteType.tag == TypeTags.FINITE;
        SemType semType = bFiniteType.getSemType();
        if (Core.containsNil(semType)) {
            BFiniteType ft = new BFiniteType(null, PredefinedType.NIL);
            members.add(new BallerinaSingletonTypeSymbol(this.context, symTable.nilType, "()", ft));
        }

        SubtypeData booleanSubTypeData = Core.booleanSubtype(semType);
        if (booleanSubTypeData instanceof AllOrNothingSubtype allOrNothingSubtype) {
            if (allOrNothingSubtype.isAllSubtype()) {
                members.add(new BallerinaSingletonTypeSymbol(this.context, symTable.booleanType, "true",
                        symTable.trueType));
                members.add(new BallerinaSingletonTypeSymbol(this.context, symTable.booleanType, "false",
                        symTable.falseType));
            }
        } else {
            BooleanSubtype booleanSubtype = (BooleanSubtype) booleanSubTypeData;
            if (booleanSubtype.value) {
                members.add(new BallerinaSingletonTypeSymbol(this.context, symTable.booleanType, "true",
                        symTable.trueType));
            } else {
                members.add(new BallerinaSingletonTypeSymbol(this.context, symTable.booleanType, "false",
                        symTable.falseType));
            }
        }

        SubtypeData intSubTypeData = Core.intSubtype(semType);
        if (intSubTypeData instanceof IntSubtype intSubtype) {
            for (Range range : intSubtype.ranges) {
                for (long i = range.min; i <= range.max; i++) {
                    BFiniteType ft = new BFiniteType(null, IntSubtype.intConst(i));
                    members.add(new BallerinaSingletonTypeSymbol(this.context, symTable.intType, Long.toString(i), ft));
                    if (i == Long.MAX_VALUE) {
                        // To avoid overflow
                        break;
                    }
                }
            }
        }

        SubtypeData decimalSubTypeData = Core.decimalSubtype(semType);
        if (decimalSubTypeData instanceof DecimalSubtype decimalSubtype) {
            for (EnumerableType enumerableDecimal : decimalSubtype.values()) {
                BigDecimal i = ((EnumerableDecimal) enumerableDecimal).value;
                BFiniteType ft = new BFiniteType(null, DecimalSubtype.decimalConst(i));
                members.add(new BallerinaSingletonTypeSymbol(this.context, symTable.decimalType, i.toString(), ft));
            }
        }

        SubtypeData floatSubTypeData = Core.floatSubtype(semType);
        if (floatSubTypeData instanceof FloatSubtype floatSubtype) {
            for (EnumerableType enumerableFloat : floatSubtype.values()) {
                double i = ((EnumerableFloat) enumerableFloat).value;
                BFiniteType ft = new BFiniteType(null, FloatSubtype.floatConst(i));
                members.add(new BallerinaSingletonTypeSymbol(this.context, symTable.floatType, Double.toString(i), ft));
            }
        }

        SubtypeData stringSubTypeData = Core.stringSubtype(semType);
        if (stringSubTypeData instanceof StringSubtype stringSubtype) {
            CharStringSubtype charStringSubtype = stringSubtype.getChar();
            for (EnumerableType enumerableType : charStringSubtype.values()) {
                String i = ((EnumerableCharString) enumerableType).value;
                BFiniteType ft = new BFiniteType(null, StringSubtype.stringConst(i));
                members.add(new BallerinaSingletonTypeSymbol(this.context, symTable.stringType, i, ft));
            }

            NonCharStringSubtype nonCharStringSubtype = stringSubtype.getNonChar();
            for (EnumerableType enumerableType : nonCharStringSubtype.values()) {
                String i = ((EnumerableString) enumerableType).value;
                BFiniteType ft = new BFiniteType(null, StringSubtype.stringConst(i));
                members.add(new BallerinaSingletonTypeSymbol(this.context, symTable.stringType, i, ft));
            }
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
                updateMembersForBFiniteType(members, this.getBType());
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
            if (Symbols.isFlagOn(unionType.flags, Flags.TYPE_PARAM) && pCloneableType.matcher(typeStr).matches()) {
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
                if (internalType.tag == TypeTags.FINITE && Core.singleShape(internalType.getSemType()).isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
