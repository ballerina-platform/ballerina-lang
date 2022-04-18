/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
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

package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLSubType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Optional;

/**
 * A base class for the xml subtypes.
 *
 * @since 2201.1.0
 */
public abstract class AbstractXMLSubTypeSymbol extends AbstractTypeSymbol {

    private ModuleSymbol module;

    public AbstractXMLSubTypeSymbol(CompilerContext context, TypeDescKind typeKind, BXMLSubType type) {
        super(context, typeKind, type);
    }

    @Override
    public Optional<ModuleSymbol> getModule() {
        if (this.module != null) {
            return Optional.of(this.module);
        }

        SymbolTable symTable = SymbolTable.getInstance(this.context);
        SymbolFactory symFactory = SymbolFactory.getInstance(this.context);
        this.module = (ModuleSymbol) symFactory.getBCompiledSymbol(symTable.langXmlModuleSymbol,
                                                                   symTable.langXmlModuleSymbol
                                                                           .getOriginalName().value);
        return Optional.of(this.module);
    }
}
