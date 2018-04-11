/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.model.symbols;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.programfile.InstructionCodes;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @since 0.94
 */
public class Symbols {

    public static BPackageSymbol createPackageSymbol(PackageID packageID, SymbolTable symTable) {
        BPackageSymbol pkgSymbol = new BPackageSymbol(packageID, symTable.rootPkgSymbol);
        if (pkgSymbol.name.value.startsWith(Names.BUILTIN_PACKAGE.value)) {
            pkgSymbol.scope = symTable.rootScope;
        } else {
            pkgSymbol.scope = new Scope(pkgSymbol);
        }
        return pkgSymbol;
    }

    public static BTypeSymbol createStructSymbol(int flags,
                                                 Name name,
                                                 PackageID pkgID,
                                                 BType type,
                                                 BSymbol owner) {
        BTypeSymbol typeSymbol = new BStructSymbol(SymTag.STRUCT, flags, name, pkgID, type, owner);
        typeSymbol.kind = SymbolKind.STRUCT;
        return typeSymbol;
    }

    public static BTypeSymbol createObjectSymbol(int flags,
                                                 Name name,
                                                 PackageID pkgID,
                                                 BType type,
                                                 BSymbol owner) {
        BStructSymbol typeSymbol = new BStructSymbol(SymTag.OBJECT, flags, name, pkgID, type, owner);
        typeSymbol.kind = SymbolKind.OBJECT;
        return typeSymbol;
    }

    public static BTypeSymbol createRecordSymbol(int flags,
                                                 Name name,
                                                 PackageID pkgID,
                                                 BType type,
                                                 BSymbol owner) {
        BStructSymbol typeSymbol = new BStructSymbol(SymTag.RECORD, flags, name, pkgID, type, owner);
        typeSymbol.kind = SymbolKind.RECORD;
        return typeSymbol;
    }

    public static BTypeSymbol createEnumSymbol(int flags,
                                               Name name,
                                               PackageID pkgID,
                                               BType type,
                                               BSymbol owner) {
        BTypeSymbol typeSymbol = createTypeSymbol(SymTag.ENUM, flags, name, pkgID, type, owner);
        typeSymbol.kind = SymbolKind.ENUM;
        return typeSymbol;
    }

    public static BTypeSymbol createTypeDefinitionSymbol(int flags,
                                                         Name name,
                                                         PackageID pkgID,
                                                         BType type,
                                                         BSymbol owner) {
        BTypeSymbol typeSymbol = createTypeSymbol(SymTag.TYPE_DEF, flags, name, pkgID, type, owner);
        typeSymbol.kind = SymbolKind.TYPE_DEF;
        return typeSymbol;
    }

    @Deprecated
    public static BAnnotationAttributeSymbol createAnnotationAttributeSymbol(Name name,
                                                                             PackageID pkgID,
                                                                             BType type,
                                                                             BSymbol owner) {
        BAnnotationAttributeSymbol annotationAttributeSymbol = new BAnnotationAttributeSymbol(name, pkgID, type, owner);
        annotationAttributeSymbol.kind = SymbolKind.ANNOTATION_ATTRIBUTE;
        return annotationAttributeSymbol;
    }

    public static BAnnotationSymbol createAnnotationSymbol(int flags, Name name,
                                                           PackageID pkgID,
                                                           BType type,
                                                           BSymbol owner) {
        BAnnotationSymbol annotationSymbol = new BAnnotationSymbol(name, flags, pkgID, type, owner);
        annotationSymbol.kind = SymbolKind.ANNOTATION;
        return annotationSymbol;
    }

    public static BInvokableSymbol createWorkerSymbol(int flags,
                                                      Name name,
                                                      PackageID pkgID,
                                                      BType type,
                                                      BSymbol owner) {
        BInvokableSymbol symbol = createInvokableSymbol(SymTag.WORKER, flags, name, pkgID, type, owner);
        symbol.kind = SymbolKind.WORKER;
        return symbol;
    }

    public static BConnectorSymbol createConnectorSymbol(int flags,
                                                         Name name,
                                                         PackageID pkgID,
                                                         BType type,
                                                         BSymbol owner) {
        BConnectorSymbol connectorSymbol = new BConnectorSymbol(flags, name, pkgID, type, owner);
        connectorSymbol.kind = SymbolKind.CONNECTOR;
        return connectorSymbol;
    }

    public static BServiceSymbol createServiceSymbol(int flags,
                                                     Name name,
                                                     PackageID pkgID,
                                                     BType type,
                                                     BSymbol owner) {
        BServiceSymbol serviceSymbol = new BServiceSymbol(flags, name, pkgID, type, owner);
        serviceSymbol.kind = SymbolKind.SERVICE;
        return serviceSymbol;
    }

    public static BInvokableSymbol createFunctionSymbol(int flags,
                                                        Name name,
                                                        PackageID pkgID,
                                                        BType type,
                                                        BSymbol owner,
                                                        boolean bodyExist) {
        BInvokableSymbol symbol = createInvokableSymbol(SymTag.FUNCTION, flags, name, pkgID, type, owner);
        symbol.bodyExist = bodyExist;
        symbol.kind = SymbolKind.FUNCTION;
        return symbol;
    }

    public static BInvokableSymbol createActionSymbol(int flags,
                                                      Name name,
                                                      PackageID pkgID,
                                                      BType type,
                                                      BSymbol owner) {
        BInvokableSymbol symbol = createInvokableSymbol(SymTag.ACTION, flags, name, pkgID, type, owner);
        symbol.kind = SymbolKind.ACTION;
        return symbol;
    }

    public static BInvokableSymbol createResourceSymbol(int flags,
                                                        Name name,
                                                        PackageID pkgID,
                                                        BType type,
                                                        BSymbol owner) {
        BInvokableSymbol symbol = createInvokableSymbol(SymTag.RESOURCE, flags, name, pkgID, type, owner);
        symbol.kind = SymbolKind.RESOURCE;
        return symbol;
    }

    public static BTypeSymbol createTypeSymbol(int symTag,
                                               int flags,
                                               Name name,
                                               PackageID pkgID,
                                               BType type,
                                               BSymbol owner) {
        return new BTypeSymbol(symTag, flags, name, pkgID, type, owner);
    }

    public static BInvokableSymbol createInvokableSymbol(int kind,
                                                         int flags,
                                                         Name name,
                                                         PackageID pkgID,
                                                         BType type,
                                                         BSymbol owner) {
        return new BInvokableSymbol(kind, flags, name, pkgID, type, owner);
    }

    public static BXMLNSSymbol createXMLNSSymbol(Name name,
                                                 String nsURI,
                                                 PackageID pkgID,
                                                 BSymbol owner) {
        return new BXMLNSSymbol(name, nsURI, pkgID, owner);
    }

    public static BConversionOperatorSymbol createConversionOperatorSymbol(final BType sourceType,
                                                                           final BType targetType,
                                                                           final BType errorType,
                                                                           boolean implicit,
                                                                           boolean safe,
                                                                           int opcode,
                                                                           PackageID pkgID,
                                                                           BSymbol owner) {
        List<BType> paramTypes = Lists.of(sourceType, targetType);
        BType retType;
        if (safe) {
            retType = targetType;
        } else if (targetType.tag == TypeTags.UNION && targetType instanceof BUnionType) {
            BUnionType unionType = (BUnionType) targetType;
            unionType.memberTypes.add(errorType);
            retType = unionType;
        } else {
            Set<BType> memberTypes = new LinkedHashSet<>(2);
            memberTypes.add(targetType);
            memberTypes.add(errorType);
            retType = new BUnionType(null, memberTypes, false);
        }

        BInvokableType opType = new BInvokableType(paramTypes, retType, null);
        BConversionOperatorSymbol symbol = new BConversionOperatorSymbol(pkgID, opType, owner, implicit, safe, opcode);
        symbol.kind = SymbolKind.CONVERSION_OPERATOR;
        return symbol;
    }

    public static BConversionOperatorSymbol createUnboxValueTypeOpSymbol(BType sourceType, BType targetType) {
        int opcode;
        switch (targetType.tag) {
            case TypeTags.INT:
                opcode = InstructionCodes.ANY2I;
                break;
            case TypeTags.FLOAT:
                opcode = InstructionCodes.ANY2F;
                break;
            case TypeTags.STRING:
                opcode = InstructionCodes.ANY2S;
                break;
            case TypeTags.BOOLEAN:
                opcode = InstructionCodes.ANY2B;
                break;
            default:
                opcode = InstructionCodes.ANY2L;
                break;
        }

        List<BType> paramTypes = Lists.of(sourceType, targetType);
        BInvokableType opType = new BInvokableType(paramTypes, targetType, null);
        BConversionOperatorSymbol symbol = new BConversionOperatorSymbol(null, opType,
                null, false, true, opcode);
        symbol.kind = SymbolKind.CONVERSION_OPERATOR;
        return symbol;
    }

    public static BTransformerSymbol createTransformerSymbol(int flags,
                                                             Name name,
                                                             PackageID pkgID,
                                                             BType type,
                                                             boolean safe,
                                                             BSymbol owner) {
        BTransformerSymbol symbol = new BTransformerSymbol(name, pkgID, type, owner, safe);
        symbol.kind = SymbolKind.TRANSFORMER;
        symbol.scope = new Scope(symbol);
        return symbol;
    }

    public static String getAttachedFuncSymbolName(String typeName, String funcName) {
        return typeName + Names.DOT.value + funcName;
    }

    public static boolean isNative(BSymbol sym) {
        return (sym.flags & Flags.NATIVE) == Flags.NATIVE;
    }

    public static boolean isPublic(BSymbol sym) {
        return (sym.flags & Flags.PUBLIC) == Flags.PUBLIC;
    }

    public static boolean isPrivate(BSymbol sym) {
        return (sym.flags & Flags.PUBLIC) != Flags.PUBLIC;
    }

    public static boolean isFlagOn(int mask, int flag) {
        return (mask & flag) == flag;
    }
}
