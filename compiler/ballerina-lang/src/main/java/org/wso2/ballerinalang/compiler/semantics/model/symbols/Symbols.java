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

import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.List;
import java.util.Set;

/**
 * @since 0.94
 */
public class Symbols {

    public static BPackageSymbol createPackageSymbol(PackageID packageID,
                                                     SymbolTable symTable) {
        BPackageSymbol pkgSymbol = new BPackageSymbol(packageID, symTable.rootPkgSymbol);
        return createPackageSymbolScope(symTable, pkgSymbol);
    }

    public static BPackageSymbol createPackageSymbol(PackageID packageID,
                                                     SymbolTable symTable,
                                                     int flags) {
        BPackageSymbol pkgSymbol = new BPackageSymbol(packageID, symTable.rootPkgSymbol, flags);
        return createPackageSymbolScope(symTable, pkgSymbol);
    }

    private static BPackageSymbol createPackageSymbolScope(SymbolTable symTable,
                                                           BPackageSymbol pkgSymbol) {

        if (pkgSymbol.pkgID.equals(PackageID.ANNOTATIONS)) {
            pkgSymbol.scope = symTable.rootScope;
        } else {
            pkgSymbol.scope = new Scope(pkgSymbol);
        }
        return pkgSymbol;
    }

    public static BTypeSymbol createObjectSymbol(int flags,
                                                 Name name,
                                                 PackageID pkgID,
                                                 BType type,
                                                 BSymbol owner) {
        BObjectTypeSymbol typeSymbol = new BObjectTypeSymbol(SymTag.OBJECT, flags, name, pkgID, type, owner);
        typeSymbol.kind = SymbolKind.OBJECT;
        return typeSymbol;
    }

    public static BRecordTypeSymbol createRecordSymbol(int flags,
                                                       Name name,
                                                       PackageID pkgID,
                                                       BType type,
                                                       BSymbol owner) {
        BRecordTypeSymbol typeSymbol = new BRecordTypeSymbol(SymTag.RECORD, flags, name, pkgID, type, owner);
        typeSymbol.kind = SymbolKind.RECORD;
        return typeSymbol;
    }

    public static BErrorTypeSymbol createErrorSymbol(int flags, Name name, PackageID pkgID, BType type, BSymbol owner) {
        BErrorTypeSymbol typeSymbol = new BErrorTypeSymbol(SymTag.ERROR, flags, name, pkgID, type, owner);
        typeSymbol.kind = SymbolKind.ERROR;
        return typeSymbol;
    }

    @Deprecated
    public static BAnnotationSymbol createAnnotationSymbol(int flags, int maskedPoints, Name name,
                                                           PackageID pkgID, BType type, BSymbol owner) {
        BAnnotationSymbol annotationSymbol = new BAnnotationSymbol(name, flags, maskedPoints, pkgID, type, owner);
        annotationSymbol.kind = SymbolKind.ANNOTATION;
        return annotationSymbol;
    }

    public static BAnnotationSymbol createAnnotationSymbol(int flags, Set<AttachPoint> points, Name name,
                                                           PackageID pkgID, BType type, BSymbol owner) {
        BAnnotationSymbol annotationSymbol = new BAnnotationSymbol(name, flags, points, pkgID, type, owner);
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

    public static BTypeSymbol createTypeSymbol(int symTag,
                                               int flags,
                                               Name name,
                                               PackageID pkgID,
                                               BType type,
                                               BSymbol owner) {
        if (type != null && type.tag == TypeTags.INVOKABLE) {
            return createInvokableTypeSymbol(symTag, flags, pkgID, type, owner);
        }
        return new BTypeSymbol(symTag, flags, name, pkgID, type, owner);
    }

    public static BInvokableTypeSymbol createInvokableTypeSymbol(int symTag,
                                                                 int flags,
                                                                 PackageID pkgID,
                                                                 BType type,
                                                                 BSymbol owner) {
        return new BInvokableTypeSymbol(symTag, flags, pkgID, type, owner);
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

    public static BCastOperatorSymbol createCastOperatorSymbol(final BType sourceType,
                                                               final BType targetType,
                                                               final BType errorType,
                                                               boolean implicit,
                                                               boolean safe,
                                                               PackageID pkgID,
                                                               BSymbol owner) {
        List<BType> paramTypes = Lists.of(sourceType, targetType);
        BType retType;
        if (safe) {
            retType = targetType;
        } else if (targetType.tag == TypeTags.UNION && targetType instanceof BUnionType) {
            BUnionType unionType = (BUnionType) targetType;
            unionType.add(errorType);
            retType = unionType;
        } else {
            retType = BUnionType.create(null, targetType, errorType);
        }

        BInvokableType opType = new BInvokableType(paramTypes, retType, null);
        return new BCastOperatorSymbol(pkgID, opType, sourceType, owner, implicit, safe);
    }

    public static BCastOperatorSymbol createUnboxValueTypeOpSymbol(BType sourceType, BType targetType) {

        List<BType> paramTypes = Lists.of(sourceType, targetType);
        BInvokableType opType = new BInvokableType(paramTypes, targetType, null);
        return new BCastOperatorSymbol(null, opType, sourceType, null, false, true);
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
        return (sym.flags & Flags.PRIVATE) == Flags.PRIVATE;
    }

    public static boolean isFlagOn(int mask, int flag) {
        return (mask & flag) == flag;
    }

    public static boolean isAttachPointPresent(int mask, int attachPoint) {
        return (mask & attachPoint) != 0;
    }

    public static boolean isOptional(BSymbol sym) {
        return (sym.flags & Flags.OPTIONAL) == Flags.OPTIONAL;
    }

    public static boolean isFunctionDeclaration(BSymbol sym) {
        return (sym.flags & Flags.INTERFACE) == Flags.INTERFACE;
    }
}
