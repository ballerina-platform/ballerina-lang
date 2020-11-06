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
import io.ballerina.compiler.api.symbols.TypeDescKind;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.api.symbols.TypeDescKind.ARRAY;
import static io.ballerina.compiler.api.symbols.TypeDescKind.BOOLEAN;
import static io.ballerina.compiler.api.symbols.TypeDescKind.DECIMAL;
import static io.ballerina.compiler.api.symbols.TypeDescKind.ERROR;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.FUTURE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.MAP;
import static io.ballerina.compiler.api.symbols.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STREAM;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TABLE;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPEDESC;
import static io.ballerina.compiler.api.symbols.TypeDescKind.XML;


/**
 * A class to hold the lang library function info required for types.
 *
 * @since 2.0.0
 */
public class LangLibrary {

    private static final CompilerContext.Key<LangLibrary> LANG_LIB_KEY = new CompilerContext.Key<>();
    private static final String LANG_VALUE = "value";

    private final Map<String, Map<String, BInvokableSymbol>> langLibMethods;
    private final Map<String, List<FunctionSymbol>> wrappedLangLibMethods;
    private final SymbolFactory symbolFactory;

    private LangLibrary(CompilerContext context) {
        context.put(LANG_LIB_KEY, this);

        symbolFactory = SymbolFactory.getInstance(context);
        SymbolTable symbolTable = SymbolTable.getInstance(context);
        Map<String, BPackageSymbol> langLibs = new HashMap<>();
        wrappedLangLibMethods = new HashMap<>();

        for (Map.Entry<BPackageSymbol, SymbolEnv> entry : symbolTable.pkgEnvMap.entrySet()) {
            BPackageSymbol module = entry.getKey();
            PackageID moduleID = module.pkgID;

            if (Names.BALLERINA_ORG.equals(moduleID.orgName) &&
                    (moduleID.nameComps.size() == 2 && Names.LANG.equals(moduleID.nameComps.get(0)))) {
                langLibs.put(moduleID.nameComps.get(1).value, module);
            }
        }

        langLibMethods = getLangLibMethods(langLibs);
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
     * @param typeDescKind A type descriptor kind
     * @return The associated list of lang library functions
     */
    public List<FunctionSymbol> getMethods(TypeDescKind typeDescKind) {
        String langLibName = getAssociatedLangLibName(typeDescKind);

        if (wrappedLangLibMethods.containsKey(langLibName)) {
            return wrappedLangLibMethods.get(langLibName);
        }

        Map<String, BInvokableSymbol> methods = langLibMethods.get(langLibName);

        List<FunctionSymbol> wrappedMethods = new ArrayList<>();
        wrappedLangLibMethods.put(langLibName, wrappedMethods);
        populateMethodList(wrappedMethods, methods);

        // Add the common functions in lang.value to types which have an associated lang library.
        if (!LANG_VALUE.equals(langLibName)) {
            populateMethodList(wrappedMethods, langLibMethods.get(LANG_VALUE));
        }

        return wrappedMethods;
    }

    // Private Methods

    private void populateMethodList(List<FunctionSymbol> list, Map<String, BInvokableSymbol> langLib) {
        for (Map.Entry<String, BInvokableSymbol> entry : langLib.entrySet()) {
            FunctionSymbol method = symbolFactory.createFunctionSymbol(entry.getValue(), entry.getKey());
            list.add(method);
        }
    }

    private String getAssociatedLangLibName(TypeDescKind typeDescKind) {
        switch (typeDescKind) {
            case INT:
            case BYTE:
                return INT.getName();
            case FLOAT:
                return FLOAT.getName();
            case DECIMAL:
                return DECIMAL.getName();
            case STRING:
                return STRING.getName();
            case BOOLEAN:
                return BOOLEAN.getName();
            case ARRAY:
            case TUPLE:
                return ARRAY.getName();
            case STREAM:
                return STREAM.getName();
            case OBJECT:
                return OBJECT.getName();
            case RECORD:
            case MAP:
                return MAP.getName();
            case ERROR:
                return ERROR.getName();
            case FUTURE:
                return FUTURE.getName();
            case TYPEDESC:
                return TYPEDESC.getName();
            case XML:
                return XML.getName();
            case TABLE:
                return TABLE.getName();
            default:
                return "value";
        }
    }

    private static Map<String, Map<String, BInvokableSymbol>> getLangLibMethods(Map<String, BPackageSymbol> langLibs) {
        Map<String, Map<String, BInvokableSymbol>> langLibMethods = new HashMap<>();

        for (Map.Entry<String, BPackageSymbol> entry : langLibs.entrySet()) {
            String key = entry.getKey();
            BPackageSymbol value = entry.getValue();

            Map<String, BInvokableSymbol> methods = new HashMap<>();

            for (Map.Entry<Name, Scope.ScopeEntry> nameScopeEntry : value.scope.entries.entrySet()) {
                BSymbol symbol = nameScopeEntry.getValue().symbol;

                if (symbol.kind != SymbolKind.FUNCTION) {
                    continue;
                }

                BInvokableSymbol invSymbol = (BInvokableSymbol) symbol;

                if (Symbols.isFlagOn(invSymbol.flags, Flags.PUBLIC) && !invSymbol.params.isEmpty() &&
                        key.compareToIgnoreCase(invSymbol.params.get(0).type.getKind().name()) == 0) {
                    methods.put(invSymbol.name.value, invSymbol);
                }
            }

            langLibMethods.put(key, methods);
        }

        populateLangValueLibrary(langLibs, langLibMethods);
        return langLibMethods;
    }

    private static void populateLangValueLibrary(Map<String, BPackageSymbol> langLibs,
                                                 Map<String, Map<String, BInvokableSymbol>> langLibMethods) {
        BPackageSymbol langValue = langLibs.get(LANG_VALUE);
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
}
