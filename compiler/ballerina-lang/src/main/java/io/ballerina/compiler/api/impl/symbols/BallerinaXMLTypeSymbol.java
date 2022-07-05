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

import io.ballerina.compiler.api.SymbolTransformer;
import io.ballerina.compiler.api.SymbolVisitor;
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
    private String signature;

    public BallerinaXMLTypeSymbol(CompilerContext context, BXMLType xmlType) {
        super(context, TypeDescKind.XML, xmlType);
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
    public Optional<String> getName() {
        if (this.typeName == null) {
            BXMLType xmlType = (BXMLType) this.getBType();
            SymbolTable symbolTable = SymbolTable.getInstance(this.context);

            if (xmlType == symbolTable.xmlType || this.typeParameter().isEmpty()) {
                this.typeName = "xml";
            } else {
                this.typeName = "xml<" + this.typeParameter().get().getName().orElse("") + ">";
            }
        }

        return Optional.of(this.typeName);
    }

    @Override
    public String signature() {
        if (this.signature == null) {
            BXMLType xmlType = (BXMLType) this.getBType();
            SymbolTable symbolTable = SymbolTable.getInstance(this.context);

            if (xmlType == symbolTable.xmlType || this.typeParameter().isEmpty()) {
                this.signature = "xml";
            } else {
                this.signature = "xml<" + this.typeParameter().get().signature() + ">";
            }
        }

        return this.signature;
    }

    @Override
    public void accept(SymbolVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(SymbolTransformer<T> transformer) {
        return transformer.transform(this);
    }
}
