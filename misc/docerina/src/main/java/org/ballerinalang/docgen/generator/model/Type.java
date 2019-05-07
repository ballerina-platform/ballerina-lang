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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;

import java.util.ArrayList;
import java.util.List;

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
    public List<Type> memberTypes = new ArrayList<>();

    public Type(BLangType type) {
        if (type == null) {
            return;
        }
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
                        this.memberTypes.add(Type.fromTypeNode(memberType));
                    });
                    if (typeSymbol != null) {
                        this.orgName = typeSymbol.pkgID.orgName.value;
                        this.moduleName = typeSymbol.pkgID.name.value;
                    }
                } else if (type instanceof BLangErrorType) {
                    this.name = type.type.toString();
                } else {
                    this.name = type.type.toString();
                }
            }
        }
        if (type.type instanceof BUnionType) {
            this.category = "types";
        } else if(type.type instanceof BRecordType) {
            this.category = "records";
        } else if(type.type.getClass().equals(BObjectType.class)) {
            BObjectTypeSymbol objSymbol = (BObjectTypeSymbol) type.type.tsymbol;
            if (objSymbol.getFlags().contains(Flag.CLIENT)) {
                this.category = "clients";
            } else if (objSymbol.getFlags().contains(Flag.LISTENER)) {
                this.category = "listeners";
            } else {
                this.category = "objects";
            }
        }
    }

    public Type(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static Type fromTypeNode(BLangType type) {
        return new Type(type);
    }
}
