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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;

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

    public BallerinaUnionTypeSymbol(CompilerContext context, ModuleID moduleID, BUnionType unionType) {
        super(context, TypeDescKind.UNION, unionType);
    }

    public BallerinaUnionTypeSymbol(CompilerContext context, ModuleID moduleID, BFiniteType finiteType) {
        super(context, TypeDescKind.UNION, finiteType);
    }

    @Override
    public List<TypeSymbol> memberTypeDescriptors() {
        if (this.memberTypes == null) {
            List<TypeSymbol> members = new ArrayList<>();

            if (this.getBType().tag == TypeTags.UNION) {
                TypesFactory typesFactory = TypesFactory.getInstance(this.context);

                for (BType memberType : ((BUnionType) this.getBType()).getMemberTypes()) {
                    if (typesFactory.isTypeReference(memberType, memberType.tsymbol, false)
                            || memberType.getKind() != TypeKind.FINITE) {
                        members.add(typesFactory.getTypeDescriptor(memberType));
                        continue;
                    }

                    BFiniteType finiteType = (BFiniteType) memberType;
                    for (BLangExpression value : finiteType.getValueSpace()) {
                        ModuleID moduleID = getModule().isPresent() ? getModule().get().id() : null;
                        BFiniteType bFiniteType = new BFiniteType(value.getBType().tsymbol, Set.of(value));
                        members.add(new BallerinaSingletonTypeSymbol(this.context, moduleID, (BLangLiteral) value,
                                                                     bFiniteType));
                    }
                }
            } else {
                for (BLangExpression value : ((BFiniteType) this.getBType()).getValueSpace()) {
                    ModuleID moduleID = getModule().isPresent() ? getModule().get().id() : null;
                    BFiniteType bFiniteType = new BFiniteType(value.getBType().tsymbol, Set.of(value));
                    members.add(new BallerinaSingletonTypeSymbol(this.context, moduleID, (BLangLiteral) value,
                                                                 bFiniteType));
                }
            }

            this.memberTypes = Collections.unmodifiableList(members);
        }

        return this.memberTypes;
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
                for (BLangExpression value : ((BFiniteType) this.getBType()).getValueSpace()) {
                    ModuleID moduleID = getModule().isPresent() ? getModule().get().id() : null;
                    members.add(new BallerinaSingletonTypeSymbol(this.context, moduleID, (BLangLiteral) value,
                                                                 value.getBType()));
                }
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
                if (internalType.tag == TypeTags.FINITE && ((BFiniteType) internalType).getValueSpace().size() > 1) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
