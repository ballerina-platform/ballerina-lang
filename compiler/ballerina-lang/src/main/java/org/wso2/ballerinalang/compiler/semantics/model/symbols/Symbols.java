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
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.Set;

/**
 * @since 0.94
 */
public class Symbols {

    public static BPackageSymbol createPackageSymbol(PackageID packageID,
                                                     SymbolTable symTable) {
        BPackageSymbol pkgSymbol = new BPackageSymbol(packageID, symTable.rootPkgSymbol, symTable.builtinPos);
        return createPackageSymbolScope(symTable, pkgSymbol);
    }

    public static BPackageSymbol createPackageSymbol(PackageID packageID,
                                                     SymbolTable symTable,
                                                     int flags) {
        BPackageSymbol pkgSymbol = new BPackageSymbol(packageID, symTable.rootPkgSymbol, flags, symTable.builtinPos);
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

    public static BObjectTypeSymbol createObjectSymbol(int flags,
                                                       Name name,
                                                       PackageID pkgID,
                                                       BType type,
                                                       BSymbol owner,
                                                       DiagnosticPos pos,
                                                       SymbolOrigin origin) {
        BObjectTypeSymbol typeSymbol = new BObjectTypeSymbol(SymTag.OBJECT, flags, name, pkgID, type, owner, pos,
                                                             origin);
        typeSymbol.kind = SymbolKind.OBJECT;
        return typeSymbol;
    }

    public static BRecordTypeSymbol createRecordSymbol(int flags,
                                                       Name name,
                                                       PackageID pkgID,
                                                       BType type,
                                                       BSymbol owner,
                                                       DiagnosticPos pos,
                                                       SymbolOrigin origin) {
        BRecordTypeSymbol typeSymbol = new BRecordTypeSymbol(SymTag.RECORD, flags, name, pkgID, type, owner, pos,
                                                             origin);
        typeSymbol.kind = SymbolKind.RECORD;
        return typeSymbol;
    }

    public static BErrorTypeSymbol createErrorSymbol(int flags, Name name, PackageID pkgID, BType type, BSymbol owner,
                                                     DiagnosticPos pos, SymbolOrigin origin) {
        BErrorTypeSymbol typeSymbol = new BErrorTypeSymbol(SymTag.ERROR, flags, name, pkgID, type, owner, pos, origin);
        typeSymbol.kind = SymbolKind.ERROR;
        return typeSymbol;
    }

    public static BAnnotationSymbol createAnnotationSymbol(int flags, Set<AttachPoint> points, Name name,
                                                           PackageID pkgID, BType type, BSymbol owner,
                                                           DiagnosticPos pos, SymbolOrigin origin) {
        BAnnotationSymbol annotationSymbol = new BAnnotationSymbol(name, flags, points, pkgID, type, owner, pos,
                                                                   origin);
        annotationSymbol.kind = SymbolKind.ANNOTATION;
        return annotationSymbol;
    }

    public static BInvokableSymbol createWorkerSymbol(int flags,
                                                      Name name,
                                                      PackageID pkgID,
                                                      BType type,
                                                      BSymbol owner,
                                                      DiagnosticPos pos,
                                                      SymbolOrigin origin) {
        BInvokableSymbol symbol = createInvokableSymbol(SymTag.WORKER, flags, name, pkgID, type, owner, pos, origin);
        symbol.kind = SymbolKind.WORKER;
        return symbol;
    }

    public static BServiceSymbol createServiceSymbol(int flags,
                                                     Name name,
                                                     PackageID pkgID,
                                                     BType type,
                                                     BSymbol owner,
                                                     DiagnosticPos pos,
                                                     SymbolOrigin origin) {
        BServiceSymbol serviceSymbol = new BServiceSymbol(flags, name, pkgID, type, owner, pos, origin);
        serviceSymbol.kind = SymbolKind.SERVICE;
        return serviceSymbol;
    }

    public static BInvokableSymbol createFunctionSymbol(int flags,
                                                        Name name,
                                                        PackageID pkgID,
                                                        BType type,
                                                        BSymbol owner,
                                                        boolean bodyExist,
                                                        DiagnosticPos pos,
                                                        SymbolOrigin origin) {
        BInvokableSymbol symbol = createInvokableSymbol(SymTag.FUNCTION, flags, name, pkgID, type, owner, pos, origin);
        symbol.bodyExist = bodyExist;
        symbol.kind = SymbolKind.FUNCTION;
        return symbol;
    }

    public static BTypeSymbol createTypeSymbol(int symTag,
                                               int flags,
                                               Name name,
                                               PackageID pkgID,
                                               BType type,
                                               BSymbol owner,
                                               DiagnosticPos pos,
                                               SymbolOrigin origin) {
        if (type != null && type.tag == TypeTags.INVOKABLE) {
            return createInvokableTypeSymbol(symTag, flags, pkgID, type, owner, pos, origin);
        }
        return new BTypeSymbol(symTag, flags, name, pkgID, type, owner, pos, origin);
    }

    public static BInvokableTypeSymbol createInvokableTypeSymbol(int symTag,
                                                                 int flags,
                                                                 PackageID pkgID,
                                                                 BType type,
                                                                 BSymbol owner,
                                                                 DiagnosticPos pos,
                                                                 SymbolOrigin origin) {
        return new BInvokableTypeSymbol(symTag, flags, pkgID, type, owner, pos, origin);
    }

    public static BInvokableSymbol createInvokableSymbol(int kind,
                                                         int flags,
                                                         Name name,
                                                         PackageID pkgID,
                                                         BType type,
                                                         BSymbol owner,
                                                         DiagnosticPos pos,
                                                         SymbolOrigin origin) {
        return new BInvokableSymbol(kind, flags, name, pkgID, type, owner, pos, origin);
    }

    public static BXMLNSSymbol createXMLNSSymbol(Name name,
                                                 String nsURI,
                                                 PackageID pkgID,
                                                 BSymbol owner,
                                                 DiagnosticPos pos,
                                                 SymbolOrigin origin) {
        return new BXMLNSSymbol(name, nsURI, pkgID, owner, pos, origin);
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

    public static boolean isRemote(BSymbol sym) {
        return (sym.flags & Flags.REMOTE) == Flags.REMOTE;
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

    public static boolean isTagOn(BSymbol symbol, int symTag) {
        return (symbol.tag & symTag) == symTag;
    }
}
