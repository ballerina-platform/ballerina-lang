/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api.impl;

import io.ballerina.compiler.api.symbols.FunctionSymbol;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * A class to hold the lang library function info required for types.
 *
 * @since 2.0.0
 */
public class LangLibrary {

    private static final CompilerContext.Key<LangLibrary> LANG_LIB_KEY = new CompilerContext.Key<>();
    private static final String LANG_VALUE = "value";

    private final Map<String, Map<String, BInvokableSymbol>> langLibMethods;
    private final SymbolFactory symbolFactory;
    private final LangLibFunctionBinder methodBinder;
    private final Types types;

    private LangLibrary(CompilerContext context) {
        context.put(LANG_LIB_KEY, this);

        this.symbolFactory = SymbolFactory.getInstance(context);
        this.langLibMethods = new HashMap<>();
        this.types = Types.getInstance(context);
        this.methodBinder = new LangLibFunctionBinder(this.types);

        SymbolTable symbolTable = SymbolTable.getInstance(context);
        for (Map.Entry<BPackageSymbol, SymbolEnv> entry : symbolTable.pkgEnvMap.entrySet()) {
            BPackageSymbol module = entry.getKey();
            PackageID moduleID = module.pkgID;

            if (isLangLibModule(moduleID)) {
                if (!LANG_VALUE.equals(moduleID.nameComps.get(1).value)) {
                    addLangLibMethods(moduleID.nameComps.get(1).value, module, this.langLibMethods, types);
                } else {
                    populateLangValueLibrary(module, this.langLibMethods);
                }
            }
        }
    }

    public static LangLibrary getInstance(CompilerContext context) {
        LangLibrary langLib = context.get(LANG_LIB_KEY);
        if (langLib == null) {
            langLib = new LangLibrary(context);
        }

        return langLib;
    }

    /**
     * Given a type descriptor kind, return the list of lang library functions that can be called using a method call
     * expr, on an expression of that type.
     *
     * @return The associated list of lang library functions
     */
    public List<FunctionSymbol> getMethods(BType type) {
        String langLibName;
        if (type.getKind() == TypeKind.UNION && types.isAllErrorMembers((BUnionType) type)) {
            langLibName = TypeKind.ERROR.typeName();
        } else {
            langLibName = getAssociatedLangLibName(type.getKind());
        }
        return getMethods(langLibName, type);
    }

    // Private Methods

    private List<FunctionSymbol> getMethods(String langLibName, BType type) {
        Map<String, BInvokableSymbol> methods = langLibMethods.get(langLibName);
        BType boundType = getTypeParamBoundType(type);
        List<FunctionSymbol> wrappedMethods = new ArrayList<>();

        populateMethodList(wrappedMethods, methods, type, boundType);

        // Add the common functions in lang.value to types which have an associated lang library.
        if (!LANG_VALUE.equals(langLibName)) {
            populateMethodList(wrappedMethods, langLibMethods.get(LANG_VALUE), type, null);
        }

        return wrappedMethods;
    }

    private void populateMethodList(List<FunctionSymbol> list, Map<String, BInvokableSymbol> langLib, BType type,
                                    BType boundTypeParam) {
        for (BInvokableSymbol symbol : langLib.values()) {
            String name = symbol.getOriginalName().getValue();
            BInvokableSymbol duplicate = methodBinder.cloneAndBind(symbol, type, boundTypeParam);
            FunctionSymbol method = symbolFactory.createFunctionSymbol(duplicate, name);
            list.add(method);
        }
    }

    private String getAssociatedLangLibName(TypeKind typeKind) {
        switch (typeKind) {
            case INT:
            case BYTE:
                return TypeKind.INT.typeName();
            case ARRAY:
            case TUPLE:
                return "array";
            case RECORD:
            case MAP:
                return TypeKind.MAP.typeName();
            case FLOAT:
            case DECIMAL:
            case STRING:
            case BOOLEAN:
            case STREAM:
            case OBJECT:
            case ERROR:
            case FUTURE:
            case TYPEDESC:
            case XML:
            case TABLE:
            case REGEXP:
                return typeKind.typeName();
            default:
                return LANG_VALUE;
        }
    }

    private static void addLangLibMethods(String basicType, BPackageSymbol langLibModule,
                                          Map<String, Map<String, BInvokableSymbol>> langLibMethods, Types types) {
        Map<String, BInvokableSymbol> methods = new HashMap<>();

        for (Map.Entry<Name, Scope.ScopeEntry> nameScopeEntry : langLibModule.scope.entries.entrySet()) {
            BSymbol symbol = nameScopeEntry.getValue().symbol;

            if (symbol.kind != SymbolKind.FUNCTION) {
                continue;
            }

            BInvokableSymbol invSymbol = (BInvokableSymbol) symbol;

            if (Symbols.isFlagOn(invSymbol.flags, Flags.PUBLIC) &&
                    (!invSymbol.params.isEmpty() && equalsToBasicType(invSymbol.params.get(0).type, basicType) ||
                            invSymbol.restParam != null &&
                                    equalsToBasicType(((BArrayType) invSymbol.restParam.type).eType, basicType))) {
                methods.put(invSymbol.name.value, invSymbol);
            }
        }

        langLibMethods.put(basicType, methods);
    }

    private static void populateLangValueLibrary(BPackageSymbol langValue,
                                                 Map<String, Map<String, BInvokableSymbol>> langLibMethods) {
        Map<String, BInvokableSymbol> methods = new HashMap<>();

        for (Map.Entry<Name, Scope.ScopeEntry> nameScopeEntry : langValue.scope.entries.entrySet()) {
            BSymbol symbol = nameScopeEntry.getValue().symbol;

            if (symbol.kind != SymbolKind.FUNCTION || !Symbols.isFlagOn(symbol.flags, Flags.LANG_LIB)) {
                continue;
            }

            methods.put(symbol.name.value, (BInvokableSymbol) symbol);
        }

        langLibMethods.put(LANG_VALUE, methods);
    }

    private BType getTypeParamBoundType(BType type) {
        switch (type.getKind()) {
            case MAP:
                return ((BMapType) type).constraint;
            case ARRAY:
                return ((BArrayType) type).eType;
            case TABLE:
                return ((BTableType) type).constraint;
            case STREAM:
                return ((BStreamType) type).constraint;
            case XML:
                if (type.tag == TypeTags.XML) {
                    return ((BXMLType) type).constraint;
                }
                return type;
            // The following explicitly mentioned type kinds should be supported, but they are not for the moment.
            case ERROR:
            default:
                return null;
        }
    }

    private static boolean isLangLibModule(PackageID moduleID) {
        return Names.BALLERINA_ORG.equals(moduleID.orgName)
                && (moduleID.nameComps.size() == 2 && Names.LANG.equals(moduleID.nameComps.get(0)));
    }

    private static boolean equalsToBasicType(BType type, String basicType) {
        if (type.getKind() == TypeKind.UNION) {
            LinkedHashSet<BType> memberTypes = ((BUnionType) type).getMemberTypes();
            for (BType memberType : memberTypes) {
                if (basicType.compareToIgnoreCase(Types.getReferredType(memberType).getKind().name()) != 0) {
                    return false;
                }
            }
            return true;
        } else {
            return basicType.compareToIgnoreCase(Types.getReferredType(type).getKind().name()) == 0;
        }
    }
}
