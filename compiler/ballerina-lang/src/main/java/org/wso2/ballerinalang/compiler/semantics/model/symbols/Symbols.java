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
import org.ballerinalang.model.tree.OperatorKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.programfile.InstructionCodes;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.List;

/**
 * @since 0.94
 */
public class Symbols {

    public static BTypeSymbol createStructSymbol(int flags,
                                                 Name name,
                                                 PackageID pkgID,
                                                 BType type,
                                                 BSymbol owner) {
        BTypeSymbol typeSymbol = new BStructSymbol(SymTag.STRUCT, flags, name, pkgID, type, owner);
        typeSymbol.kind = SymbolKind.STRUCT;
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

    public static BStreamletSymbol createStreamletSymbol(int flags,
                                                    Name name,
                                                    PackageID pkgID,
                                                    BType type,
                                                    BSymbol owner) {
        BStreamletSymbol typeSymbol = createStreamletSymbol(SymTag.STREAMLET, flags, name, pkgID, type, owner);
        typeSymbol.kind = SymbolKind.STREAMLET;
        return typeSymbol;
    }

    public static BInvokableSymbol createFunctionSymbol(int flags,
                                                        Name name,
                                                        PackageID pkgID,
                                                        BType type,
                                                        BSymbol owner) {
        BInvokableSymbol symbol = createInvokableSymbol(SymTag.FUNCTION, flags, name, pkgID, type, owner);
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

    public static BStreamletSymbol createStreamletSymbol(int symTag,
                                                         int flags,
                                                         Name name,
                                                         PackageID pkgID,
                                                         BType type,
                                                         BSymbol owner) {
        return new BStreamletSymbol(symTag, flags, name, pkgID, type, owner);
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

    public static BCastOperatorSymbol createCastOperatorSymbol(BType sourceType,
                                                               BType targetType,
                                                               BType errorType,
                                                               boolean implicit,
                                                               boolean safe,
                                                               int opcode,
                                                               PackageID pkgID,
                                                               BSymbol owner) {
        List<BType> paramTypes = Lists.of(sourceType, targetType);
        List<BType> retTypes = Lists.of(targetType, errorType);
        BInvokableType opType = new BInvokableType(paramTypes, retTypes, null);
        BCastOperatorSymbol symbol = new BCastOperatorSymbol(pkgID, opType,
                owner, implicit, safe, opcode);
        symbol.kind = SymbolKind.CAST_OPERATOR;
        return symbol;
    }

    public static BConversionOperatorSymbol createConversionOperatorSymbol(BType sourceType,
                                                                           BType targetType,
                                                                           BType errorType,
                                                                           boolean safe,
                                                                           int opcode,
                                                                           PackageID pkgID,
                                                                           BSymbol owner) {
        List<BType> paramTypes = Lists.of(sourceType, targetType);
        List<BType> retTypes = Lists.of(targetType, errorType);
        BInvokableType opType = new BInvokableType(paramTypes, retTypes, null);
        BConversionOperatorSymbol symbol = new BConversionOperatorSymbol(pkgID, opType, owner, safe, opcode);
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

    public static BOperatorSymbol createTypeofOperatorSymbol(BType exprType, Types types,
                                                             SymbolTable symTable, Names names) {
        List<BType> paramTypes = Lists.of(exprType);
        List<BType> retTypes = Lists.of(symTable.typeType);
        BInvokableType opType = new BInvokableType(paramTypes, retTypes, null);
        if (types.isValueType(exprType)) {
            return new BOperatorSymbol(names.fromString(OperatorKind.TYPEOF.value()),
                    symTable.rootPkgSymbol.pkgID, opType, symTable.rootPkgSymbol, InstructionCodes.TYPELOAD);
        } else {
            return new BOperatorSymbol(names.fromString(OperatorKind.TYPEOF.value()),
                    symTable.rootPkgSymbol.pkgID, opType, symTable.rootPkgSymbol, InstructionCodes.TYPEOF);
        }
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
