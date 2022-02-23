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

package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.EnumSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an enum.
 *
 * @since 2.0.0
 */
public class BallerinaEnumSymbol extends BallerinaTypeDefinitionSymbol implements EnumSymbol {

    private List<ConstantSymbol> members;
    private List<AnnotationSymbol> annots;

    protected BallerinaEnumSymbol(String name, List<ConstantSymbol> members, List<Qualifier> qualifiers,
                                  List<AnnotationSymbol> annots, TypeSymbol typeDescriptor, BSymbol bSymbol,
                                  CompilerContext context) {
        super(name, qualifiers, annots, typeDescriptor, bSymbol, context);
        this.members = Collections.unmodifiableList(members);
        this.annots = annots;
    }

    @Override
    public List<ConstantSymbol> members() {
        return this.members;
    }

    @Override
    public List<AnnotationSymbol> annotations() {
        return this.annots;
    }

    @Override
    public SymbolKind kind() {
        return SymbolKind.ENUM;
    }

    /**
     * An enum symbol builder.
     *
     * @since 2.0.0
     */
    public static class EnumSymbolBuilder extends SymbolBuilder<BallerinaEnumSymbol.EnumSymbolBuilder> {

        protected List<ConstantSymbol> members;
        protected List<Qualifier> qualifiers = new ArrayList<>();
        protected List<AnnotationSymbol> annots = new ArrayList<>();
        protected TypeSymbol typeDescriptor;

        public EnumSymbolBuilder(String name, BSymbol symbol, CompilerContext context) {
            super(name, SymbolKind.TYPE_DEFINITION, symbol, context);
        }

        public EnumSymbolBuilder withMembers(List<ConstantSymbol> members) {
            this.members = members;
            return this;
        }

        public EnumSymbolBuilder withTypeDescriptor(TypeSymbol typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public EnumSymbolBuilder withQualifier(Qualifier qualifier) {
            this.qualifiers.add(qualifier);
            return this;
        }

        public EnumSymbolBuilder withAnnotation(AnnotationSymbol annot) {
            this.annots.add(annot);
            return this;
        }

        @Override
        public BallerinaEnumSymbol build() {
            return new BallerinaEnumSymbol(this.name, this.members, this.qualifiers, this.annots,
                                           this.typeDescriptor, this.bSymbol, this.context);
        }
    }
}
