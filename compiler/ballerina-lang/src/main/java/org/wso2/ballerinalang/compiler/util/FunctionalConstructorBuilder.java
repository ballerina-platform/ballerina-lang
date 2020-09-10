/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.util;


import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstructorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.model.symbols.SymbolOrigin.BUILTIN;

/**
 * Build functional constructor symbol.
 *
 * @since 1.3.0
 */
public class FunctionalConstructorBuilder {
    private ArrayList<BVarSymbol> params;
    private String name;
    private BPackageSymbol langlibPkg;
    private BType constructedType;
    private DiagnosticPos pos;

    private FunctionalConstructorBuilder(String name, BPackageSymbol langlibPkg, BType constructedType,
                                         DiagnosticPos pos) {
        this.name = name;
        this.langlibPkg = langlibPkg;
        this.constructedType = constructedType;
        this.params = new ArrayList<>();
        this.pos = pos;
    }

    public static FunctionalConstructorBuilder newConstructor(String name,
                                                              BPackageSymbol langlibModule,
                                                              BType constructedType,
                                                              DiagnosticPos pos) {
        return new FunctionalConstructorBuilder(name, langlibModule, constructedType, pos);
    }

    private FunctionalConstructorBuilder addParam(String name, BType type, boolean isDefaultable) {
        BVarSymbol paramSymbol = new BVarSymbol(0, new Name(name), langlibPkg.pkgID, type, null, pos, BUILTIN);
        paramSymbol.defaultableParam = isDefaultable;
        params.add(paramSymbol);
        return this;
    }

    public FunctionalConstructorBuilder addParam(String name, BType type) {
        return addParam(name, type, false);
    }

    public FunctionalConstructorBuilder addDefaultableParam(String name, BType type) {
        return addParam(name, type, true);
    }

    public BConstructorSymbol build() {
        List<BType> paramTypes = new ArrayList<>(params.size());
        for (BVarSymbol param : params) {
            paramTypes.add(param.type);
        }

        BInvokableTypeSymbol invokableTSymbol = new BInvokableTypeSymbol(SymTag.CONSTRUCTOR, Flags.PUBLIC,
                                                                         langlibPkg.pkgID, constructedType,
                                                                         langlibPkg, pos, BUILTIN);
        invokableTSymbol.params = params;
        invokableTSymbol.returnType = constructedType;
        BInvokableType invokableType = new BInvokableType(paramTypes, constructedType, invokableTSymbol);
        BConstructorSymbol symbol = new BConstructorSymbol(Flags.PUBLIC, new Name(name), langlibPkg.pkgID,
                                                           invokableType, langlibPkg, pos, BUILTIN);
        symbol.params = params;
        symbol.kind = SymbolKind.FUNCTIONAL_CONSTRUCTOR;
        symbol.scope = new Scope(symbol);
        symbol.retType = constructedType;
        return symbol;
    }
}
