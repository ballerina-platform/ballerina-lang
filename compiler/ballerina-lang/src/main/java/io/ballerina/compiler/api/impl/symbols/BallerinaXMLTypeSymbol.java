/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Optional;

/**
 * Represents an xml type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaXMLTypeSymbol extends AbstractTypeSymbol implements XMLTypeSymbol {

    private TypeSymbol typeParameter;
    private String typeName;

    public BallerinaXMLTypeSymbol(CompilerContext context, ModuleID moduleID, BXMLType xmlType) {
        super(context, TypeDescKind.XML, moduleID, xmlType);
    }

    @Override
    public Optional<TypeSymbol> typeParameter() {
        if (this.typeParameter == null) {
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            this.typeParameter = typesFactory.getTypeDescriptor(((BXMLType) this.getBType()).constraint);
        }

        return Optional.ofNullable(this.typeParameter);
    }

    @Override
    public String name() {
        if (this.typeName == null) {
            BXMLType xmlType = (BXMLType) this.getBType();
            SymbolTable symbolTable = SymbolTable.getInstance(this.context);

            if (xmlType == symbolTable.xmlType || this.typeParameter().isEmpty()) {
                this.typeName = "xml";
            } else {
                this.typeName = "xml<" + this.typeParameter().get().name() + ">";
            }
        }

        return this.typeName;
    }

    @Override
    public String signature() {
        return name();
    }
}
