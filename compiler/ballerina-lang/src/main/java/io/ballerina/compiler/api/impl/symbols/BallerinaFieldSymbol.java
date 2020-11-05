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

import io.ballerina.compiler.api.impl.TypesFactory;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FieldSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.util.Optional;

/**
 * Represents a field with a name and type.
 *
 * @since 2.0.0
 */
public class BallerinaFieldSymbol implements FieldSymbol {

    private final Documentation docAttachment;
    private final BField bField;
    private final CompilerContext context;
    private TypeSymbol typeDescriptor;

    public BallerinaFieldSymbol(CompilerContext context, BField bField) {
        this.context = context;
        this.bField = bField;
        this.docAttachment = new BallerinaDocumentation(bField.symbol.markdownDocumentation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.bField.getName().getValue();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOptional() {
        return (this.bField.type.flags & Flags.OPTIONAL) == Flags.OPTIONAL;
    }

    @Override
    public boolean hasDefaultValue() {
        return !isOptional() && (this.bField.symbol.flags & Flags.REQUIRED) != Flags.REQUIRED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TypeSymbol typeDescriptor() {
        if (this.typeDescriptor == null) {
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            this.typeDescriptor = typesFactory.getTypeDescriptor(this.bField.type);
        }

        return this.typeDescriptor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Documentation> documentation() {
        return Optional.ofNullable(docAttachment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Qualifier> qualifier() {
        if ((this.bField.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
            return Optional.of(Qualifier.PUBLIC);
        } else if ((this.bField.symbol.flags & Flags.PRIVATE) == Flags.PRIVATE) {
            return Optional.of(Qualifier.PRIVATE);
        }

        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String signature() {
        StringBuilder signature = new StringBuilder(this.typeDescriptor().signature() + " " + this.name());
        if (this.isOptional()) {
            signature.append("?");
        }

        return signature.toString();
    }
}
