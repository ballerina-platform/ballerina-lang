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

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.Set;

/**
 * @since 0.94
 */
public class Symbols {

    public static BPackageSymbol createPackageSymbol(PackageID packageID,
                                                     SymbolTable symTable,
                                                     SymbolOrigin origin) {
        BPackageSymbol pkgSymbol = new BPackageSymbol(packageID, symTable.rootPkgSymbol, symTable.builtinPos, origin);
        return createPackageSymbolScope(symTable, pkgSymbol);
    }

    public static BPackageSymbol createPackageSymbol(PackageID packageID,
                                                     SymbolTable symTable,
                                                     long flags,
                                                     SymbolOrigin origin) {
        BPackageSymbol pkgSymbol = new BPackageSymbol(packageID, symTable.rootPkgSymbol, flags, symTable.builtinPos,
                                                      origin);
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

    public static BObjectTypeSymbol createObjectSymbol(long flags,
                                                       Name name,
                                                       PackageID pkgID,
                                                       BType type,
                                                       BSymbol owner,
                                                       Location pos,
                                                       SymbolOrigin origin) {
        BObjectTypeSymbol typeSymbol = new BObjectTypeSymbol(SymTag.OBJECT, flags, name, pkgID, type, owner, pos,
                                                             origin);
        typeSymbol.kind = SymbolKind.OBJECT;
        return typeSymbol;
    }

    public static BClassSymbol createClassSymbol(long flags,
                                                       Name name,
                                                       PackageID pkgID,
                                                       BType type,
                                                       BSymbol owner,
                                                       Location pos,
                                                       SymbolOrigin origin,
                                                       boolean isServiceDecl) {
        BClassSymbol typeSymbol = new BClassSymbol(SymTag.OBJECT, flags, name, pkgID, type, owner, pos, origin);
        typeSymbol.kind = SymbolKind.OBJECT;
        // This class represent the service declared via service declaration.
        typeSymbol.isServiceDecl = isServiceDecl;
        return typeSymbol;
    }

    public static BRecordTypeSymbol createRecordSymbol(long flags,
                                                       Name name,
                                                       PackageID pkgID,
                                                       BType type,
                                                       BSymbol owner,
                                                       Location pos,
                                                       SymbolOrigin origin) {
        BRecordTypeSymbol typeSymbol = new BRecordTypeSymbol(SymTag.RECORD, flags, name, pkgID, type, owner, pos,
                                                             origin);
        typeSymbol.kind = SymbolKind.RECORD;
        return typeSymbol;
    }

    public static BErrorTypeSymbol createErrorSymbol(long flags, Name name, PackageID pkgID, BType type, BSymbol owner,
                                                     Location pos, SymbolOrigin origin) {
        return new BErrorTypeSymbol(SymTag.ERROR, flags, name, pkgID, type, owner, pos, origin);
    }

    public static BAnnotationSymbol createAnnotationSymbol(long flags, Set<AttachPoint> points, Name name,
                                                           Name origName, PackageID pkgID, BType type, BSymbol owner,
                                                           Location pos, SymbolOrigin origin) {
        BAnnotationSymbol annotationSymbol = new BAnnotationSymbol(name, origName, flags, points, pkgID, type, owner,
                                                                   pos, origin);
        annotationSymbol.kind = SymbolKind.ANNOTATION;
        return annotationSymbol;
    }

    public static BInvokableSymbol createWorkerSymbol(long flags,
                                                      Name name,
                                                      Name originalName,
                                                      PackageID pkgID,
                                                      BType type,
                                                      BSymbol owner,
                                                      Location pos,
                                                      SymbolOrigin origin) {
        BInvokableSymbol symbol = createInvokableSymbol(
                SymTag.WORKER, flags, name, originalName, pkgID, type, owner, pos, origin);
        symbol.kind = SymbolKind.WORKER;
        return symbol;
    }

    public static BInvokableSymbol createFunctionSymbol(long flags,
                                                        Name name,
                                                        Name originalName,
                                                        PackageID pkgID,
                                                        BType type,
                                                        BSymbol owner,
                                                        boolean bodyExist,
                                                        Location pos,
                                                        SymbolOrigin origin) {
        BInvokableSymbol symbol = createInvokableSymbol(
                SymTag.FUNCTION, flags, name, originalName, pkgID, type, owner, pos, origin);
        symbol.bodyExist = bodyExist;
        symbol.kind = SymbolKind.FUNCTION;
        return symbol;
    }

    public static BTypeSymbol createTypeSymbol(long symTag,
                                               long flags,
                                               Name name,
                                               PackageID pkgID,
                                               BType type,
                                               BSymbol owner,
                                               Location pos,
                                               SymbolOrigin origin) {
        return createTypeSymbol(symTag, flags, name, name, pkgID, type, owner, pos, origin);
    }

    public static BTypeSymbol createTypeSymbol(long symTag,
                                               long flags,
                                               Name name,
                                               Name originalName,
                                               PackageID pkgID,
                                               BType type,
                                               BSymbol owner,
                                               Location pos,
                                               SymbolOrigin origin) {
        if (type != null && Types.getReferredType(type).tag == TypeTags.INVOKABLE) {
            BInvokableTypeSymbol invokableTypeSymbol =
                    createInvokableTypeSymbol(symTag, flags, pkgID, type, owner, pos, origin);
            invokableTypeSymbol.returnType = ((BInvokableType) type).retType;
            return invokableTypeSymbol;
        }
        return new BTypeSymbol(symTag, flags, name, originalName, pkgID, type, owner, pos, origin);
    }

    public static BTypeDefinitionSymbol createTypeDefinitionSymbol(long flags,
                                                                   Name name,
                                                                   PackageID pkgID,
                                                                   BType type,
                                                                   BSymbol owner,
                                                                   Location pos,
                                                                   SymbolOrigin origin) {
        return new BTypeDefinitionSymbol(flags, name, pkgID, type, owner, pos, origin);
    }


    public static BInvokableTypeSymbol createInvokableTypeSymbol(long symTag,
                                                                 long flags,
                                                                 PackageID pkgID,
                                                                 BType type,
                                                                 BSymbol owner,
                                                                 Location pos,
                                                                 SymbolOrigin origin) {
        return new BInvokableTypeSymbol(symTag, flags, pkgID, type, owner, pos, origin);
    }

    public static BInvokableSymbol createInvokableSymbol(long kind,
                                                         long flags,
                                                         Name name,
                                                         Name originalName,
                                                         PackageID pkgID,
                                                         BType type,
                                                         BSymbol owner,
                                                         Location pos,
                                                         SymbolOrigin origin) {
        return new BInvokableSymbol(kind, flags, name, originalName, pkgID, type, owner, pos, origin);
    }

    public static BXMLNSSymbol createXMLNSSymbol(Name name,
                                                 String nsURI,
                                                 PackageID pkgID,
                                                 BSymbol owner,
                                                 Location pos,
                                                 SymbolOrigin origin) {
        return new BXMLNSSymbol(name, nsURI, pkgID, owner, pos, origin);
    }
    
    public static BResourcePathSegmentSymbol createResourcePathSegmentSymbol(Name name,
                                                                             PackageID pkgID,
                                                                             BType type,
                                                                             BSymbol owner,
                                                                             Location location,
                                                                             BResourcePathSegmentSymbol parentResource,
                                                                             BResourceFunction resourceMethod,
                                                                             SymbolOrigin origin) {
        return new BResourcePathSegmentSymbol(name, pkgID, type, owner, location, parentResource, resourceMethod,
                origin);
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

    public static boolean isResource(BSymbol sym) {
        return (sym.flags & Flags.RESOURCE) == Flags.RESOURCE;
    }

    public static boolean isRemote(BSymbol sym) {
        return (sym.flags & Flags.REMOTE) == Flags.REMOTE;
    }

    public static boolean isFlagOn(long mask, long flag) {
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

    public static boolean isTagOn(BSymbol symbol, long symTag) {
        return (symbol.tag & symTag) == symTag;
    }

    public static boolean isService(BSymbol sym) {
        return (sym.flags & Flags.SERVICE) == Flags.SERVICE;
    }

    public static boolean isClient(BSymbol sym) {
        return (sym.flags & Flags.CLIENT) == Flags.CLIENT;
    }
}
