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

import io.ballerina.compiler.api.impl.types.TypeBuilder;
import io.ballerina.compiler.api.impl.types.util.BallerinaLangLibMethod;
import io.ballerina.compiler.api.types.FunctionTypeDescriptor;
import io.ballerina.compiler.api.types.TypeDescKind;
import io.ballerina.compiler.api.types.util.LangLibMethod;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.api.types.TypeDescKind.ARRAY;
import static io.ballerina.compiler.api.types.TypeDescKind.BOOLEAN;
import static io.ballerina.compiler.api.types.TypeDescKind.DECIMAL;
import static io.ballerina.compiler.api.types.TypeDescKind.ERROR;
import static io.ballerina.compiler.api.types.TypeDescKind.FLOAT;
import static io.ballerina.compiler.api.types.TypeDescKind.FUTURE;
import static io.ballerina.compiler.api.types.TypeDescKind.INT;
import static io.ballerina.compiler.api.types.TypeDescKind.MAP;
import static io.ballerina.compiler.api.types.TypeDescKind.OBJECT;
import static io.ballerina.compiler.api.types.TypeDescKind.STREAM;
import static io.ballerina.compiler.api.types.TypeDescKind.STRING;
import static io.ballerina.compiler.api.types.TypeDescKind.TYPEDESC;
import static io.ballerina.compiler.api.types.TypeDescKind.XML;

/**
 * A class to hold the lang library function info required for types.
 *
 * @since 2.0.0
 */
public class LangLibrary {

    private static final LangLibrary langLibrary = new LangLibrary();
    private static Map<String, BPackageSymbol> langLibs;
    private static Map<String, Map<String, BInvokableSymbol>> langLibMethods;
    private static Map<String, List<LangLibMethod>> wrappedLangLibMethods;
    private static TypeBuilder typeBuilder;

    private LangLibrary() {
    }

    public static LangLibrary getInstance(CompilerContext context) {
        if (langLibs != null) {
            return langLibrary;
        }

        // Only need to do the following once.

        SymbolTable symbolTable = SymbolTable.getInstance(context);
        typeBuilder = new TypeBuilder(Types.getInstance(context), langLibrary);
        langLibs = new HashMap<>();
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
        return langLibrary;
    }

    public List<LangLibMethod> getMethods(TypeDescKind typeDescKind) {
        String langLibName = getAssociatedLangLibName(typeDescKind);

        if (wrappedLangLibMethods.containsKey(langLibName)) {
            return wrappedLangLibMethods.get(langLibName);
        }

        List<LangLibMethod> wrappedMethods = new ArrayList<>();
        Map<String, BInvokableSymbol> methods = langLibMethods.get(langLibName);

        if (methods == null) {
            // TODO: handle types which dont have a corresponding lang lib
            return wrappedMethods;
        }

        wrappedLangLibMethods.put(langLibName, wrappedMethods);

        for (Map.Entry<String, BInvokableSymbol> entry : methods.entrySet()) {
            String name = entry.getKey();
            BInvokableSymbol methodSymbol = entry.getValue();
            BallerinaLangLibMethod method =
                    new BallerinaLangLibMethod(name, new HashSet<>(),
                                               (FunctionTypeDescriptor) typeBuilder.build(methodSymbol.type));
            wrappedMethods.add(method);
        }

        return wrappedMethods;
    }

    // Private Methods

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

        return langLibMethods;
    }
}
