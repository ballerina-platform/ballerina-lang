/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.ballerinalang.docgen.generator.model;

import org.ballerinalang.docgen.docs.BallerinaDocDataHolder;
import org.ballerinalang.model.elements.Flag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a Ballerina Type.
 */
public class Type {
    public String orgName;
    public String moduleName;
    public String name;
    public String description;
    public String category;
    public boolean isAnonymousUnionType;
    public boolean isArrayType;
    public boolean isNullable;
    public boolean isTuple;
    public boolean isLambda;
    public boolean generateUserDefinedTypeLink = true;
    public List<Type> memberTypes = new ArrayList<>();
    public List<Type> paramTypes = new ArrayList<>();
    public int arrayDimensions;
    public Type elementType;
    public Type returnType;

    private Type() {
    }

    public Type(BType type) {
        this.name = type.tsymbol.name != null ? type.tsymbol.name.value : null;
        this.orgName = type.tsymbol.pkgID.orgName.value;
        this.moduleName = type.tsymbol.pkgID.name.value;
        setCategory(type);
    }

    public Type(BLangType type, String currentModule) {
        BTypeSymbol typeSymbol = type.type.tsymbol;
        if (typeSymbol != null && !typeSymbol.name.value.equals("")) {
            if (type instanceof BLangUserDefinedType) {
                BLangUserDefinedType userDefinedType = (BLangUserDefinedType) type;
                this.name = userDefinedType.typeName.value;
            } else {
                this.name = typeSymbol.name.value;
            }
            this.orgName = typeSymbol.pkgID.orgName.value;
            this.moduleName = typeSymbol.pkgID.name.value;
        } else {
            if (type.type instanceof BUnionType) {
                if (type instanceof BLangUserDefinedType) {
                    BLangUserDefinedType userDefinedType = (BLangUserDefinedType) type;
                    this.name = userDefinedType.typeName.value;
                    if (userDefinedType.pkgAlias.value.equals("")) {
                        this.orgName = BallerinaDocDataHolder.getInstance().getOrgName();
                        this.moduleName = ".";
                    } else {
                        this.moduleName = userDefinedType.pkgAlias.value;
                    }
                } else if (type instanceof BLangUnionTypeNode) {
                    this.isAnonymousUnionType = true;
                    ((BLangUnionTypeNode) type).memberTypeNodes.forEach(memberType -> {
                        this.memberTypes.add(Type.fromTypeNode(memberType, currentModule));
                    });
                    if (typeSymbol != null) {
                        this.orgName = typeSymbol.pkgID.orgName.value;
                        this.moduleName = typeSymbol.pkgID.name.value;
                    }
                } else {
                    this.name = type.type.toString();
                }
            }
        }
        setCategory(type.type);
    }

    public Type(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static Type fromTypeNode(BLangType type, String currentModule) {
        Type typeModel = null;
        if (type instanceof BLangFunctionTypeNode) {
            typeModel = new Type();
            typeModel.isLambda = true;
            typeModel.paramTypes = ((BLangFunctionTypeNode) type).params.stream()
                    .map((p) -> Type.fromTypeNode(p.typeNode, currentModule))
                    .collect(Collectors.toList());
            typeModel.returnType = Type.fromTypeNode(((BLangFunctionTypeNode) type).returnTypeNode, currentModule);
        } else if (type instanceof BLangFiniteTypeNode) {
            List<BLangExpression> valueSpace = ((BLangFiniteTypeNode) type).valueSpace;
            if (valueSpace.size() == 1) {
                typeModel = new Type(valueSpace.get(0).type);
            }
        } else if (type instanceof BLangArrayType) {
            BLangType elemtype = ((BLangArrayType) type).elemtype;
            String moduleName = elemtype.type.tsymbol.pkgID.name.toString();
            Type elementType = fromTypeNode(elemtype, moduleName);
            typeModel = new Type(type, currentModule);
            typeModel.elementType = elementType;
            typeModel.arrayDimensions = ((BLangArrayType) type).dimensions;
            typeModel.isArrayType = true;
            if (moduleName.equals(currentModule) && elemtype instanceof BLangUserDefinedType
                    && !((BLangUserDefinedType) elemtype).flagSet.contains(Flag.PUBLIC)) {
                typeModel.generateUserDefinedTypeLink = false;
            }
        } else if (type instanceof BLangTupleTypeNode) {
            List<BLangType> memberTypeNodes = ((BLangTupleTypeNode) type).memberTypeNodes;
            typeModel = new Type(type, currentModule);
            typeModel.isTuple = true;
            typeModel.memberTypes = memberTypeNodes.stream()
                                    .map(typeVal -> Type.fromTypeNode(typeVal, currentModule))
                                    .collect(Collectors.toList());
        } else if (type.nullable && type instanceof BLangUnionTypeNode
                && ((BLangUnionTypeNode) type).getMemberTypeNodes().size() == 2) {
            BLangUnionTypeNode unionType = (BLangUnionTypeNode) type;
            typeModel = fromTypeNode((BLangType) unionType.getMemberTypeNodes().toArray()[0], currentModule);
        }
        if (typeModel == null) {
            typeModel = new Type(type, currentModule);
            if (type.type.tsymbol != null && type.type.tsymbol.pkgID.name.toString().equals(currentModule)
                    && type instanceof BLangUserDefinedType
                    && (type.type.tsymbol.flags & Flags.PUBLIC) != Flags.PUBLIC) {
                typeModel.generateUserDefinedTypeLink = false;
            }
        }
        typeModel.isNullable = type.nullable;
        if (type.type instanceof BNilType) {
            typeModel.name = "()";
        }
        return typeModel;
    }

    private void setCategory(BType type) {
        if (type.getClass().equals(BObjectType.class)) {
            BObjectTypeSymbol objSymbol = (BObjectTypeSymbol) type.tsymbol;
            if (objSymbol.getFlags().contains(Flag.CLIENT)) {
                this.category = "clients";
            } else if (objSymbol.getFlags().contains(Flag.LISTENER) || isListenerObject(objSymbol)) {
                this.category = "listeners";
            } else {
                this.category = "objects";
            }
        } else {
            switch (type.tag) {
                case TypeTags
                        .ANNOTATION: this.category = "annotations"; break;
                case TypeTags
                        .RECORD: this.category = "records"; break;
                case TypeTags
                        .UNION:
                case TypeTags
                        .FINITE:
                        if (type instanceof BFiniteType) {
                            Set<BLangExpression> valueSpace = ((BFiniteType) type).valueSpace;
                            if (valueSpace.size() == 1 && valueSpace.toArray()[0] instanceof BLangLiteral) {
                                BLangLiteral literal = (BLangLiteral) valueSpace.toArray()[0];
                                if (literal.isConstant) {
                                    this.category = "constants";
                                    break;
                                }
                            }
                        }
                        this.category = "types"; break;
                case TypeTags
                        .ERROR: this.category = "errors"; break;
                case TypeTags.INT:
                case TypeTags.BYTE:
                case TypeTags.FLOAT:
                case TypeTags.DECIMAL:
                case TypeTags.STRING:
                case TypeTags.BOOLEAN:
                case TypeTags.JSON:
                case TypeTags.XML:
                case TypeTags.NIL:
                case TypeTags.ANY:
                case TypeTags.ANYDATA:
                case TypeTags.XMLNS:
                case TypeTags.MAP: // TODO generate type for constraint type
                case TypeTags.TABLE:
                case TypeTags.FUTURE:
                case TypeTags.HANDLE:
                    this.category = "builtin"; break;
                default:
                    this.category = "UNKNOWN";
            }

        }

    }

    /**
     * Check whether the symbol is a listener object.
     *
     * @param bSymbol Symbol to evaluate
     * @return {@link Boolean}  whether listener or not
     */
    private static boolean isListenerObject(BSymbol bSymbol) {
        if (!(bSymbol instanceof BObjectTypeSymbol)) {
            return false;
        }
        List<String> attachedFunctions = ((BObjectTypeSymbol) bSymbol).attachedFuncs.stream()
                .map(function -> function.funcName.getValue())
                .collect(Collectors.toList());
        return attachedFunctions.contains("__start") && attachedFunctions.contains("__immediateStop")
                && attachedFunctions.contains("__attach");
    }
}
