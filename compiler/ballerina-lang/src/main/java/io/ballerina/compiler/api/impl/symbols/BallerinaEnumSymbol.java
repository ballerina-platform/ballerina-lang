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

import io.ballerina.compiler.api.SymbolTransformer;
import io.ballerina.compiler.api.SymbolVisitor;
import io.ballerina.compiler.api.impl.LangLibrary;
import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.impl.util.SymbolUtils;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.EnumSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEnumSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Represents an enum.
 *
 * @since 2.0.0
 */
public class BallerinaEnumSymbol extends BallerinaTypeDefinitionSymbol implements EnumSymbol {

    private List<ConstantSymbol> members;
    private List<AnnotationSymbol> annots;
    private final List<AnnotationAttachmentSymbol> annotAttachments;
    private List<FunctionSymbol> langLibFunctions;
    private final BType bType;
    private String signature;

    protected BallerinaEnumSymbol(String name, List<ConstantSymbol> members, List<Qualifier> qualifiers,
                                  List<AnnotationSymbol> annots, List<AnnotationAttachmentSymbol> annotAttachments,
                                  TypeSymbol typeDescriptor, BSymbol bSymbol, CompilerContext context) {
        super(name, qualifiers, annots, annotAttachments, typeDescriptor, bSymbol, context);
        this.members = Collections.unmodifiableList(members);
        this.annots = annots;
        this.annotAttachments = annotAttachments;
        this.bType = bSymbol.getType();
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
    public List<AnnotationAttachmentSymbol> annotAttachments() {
        return this.annotAttachments;
    }

    /**
     * Get the Type Kind.
     *
     * @return {@link TypeDescKind} represented by the model
     */
    @Override
    public TypeDescKind typeKind() {
        return TypeDescKind.ENUM;
    }

    @Override
    public String signature() {
        if (this.signature != null) {
            return this.signature;
        }

        StringJoiner joiner = new StringJoiner("|");
        for (ConstantSymbol member : members) {
            joiner.add(member.signature());
        }

        this.signature = joiner.toString();

        return this.signature;
    }

    /**
     * List of lang library functions that can be called using a method call expression.
     *
     * @return {@link List} of lang library functions of the type
     */
    @Override
    public List<FunctionSymbol> langLibMethods() {
        if (this.langLibFunctions != null) {
            return this.langLibFunctions;
        }

        LangLibrary langLibrary = LangLibrary.getInstance(this.context);
        List<FunctionSymbol> functions = langLibrary.getMethods(this.getBType());
        this.langLibFunctions = SymbolUtils.filterLangLibMethods(this.context, functions, this.getBType());

        return this.langLibFunctions;
    }

    /**
     * Checks whether a value of this type can be assigned to a variable of the specified type.
     *
     * @param targetType The type with which compatibility is checked
     * @return Returns true if this type is assignable to the specified type
     * @deprecated This method will be replaced by subtypeOf() method in a later version. This is just a rename in the
     * method for aligning the method names with the language semantics. Switching to subtypeOf() will not break
     * anything.
     */
    @Override
    public boolean assignableTo(TypeSymbol targetType) {
        Types types = Types.getInstance(this.context);
        return types.isAssignable(this.bType, getTargetBType(targetType));
    }

    /**
     * Checks whether this type is a subtype of the specified type. This is evaluated as per the language semantics
     * defined in the spec.
     *
     * @param targetType The type with which compatibility is checked
     * @return Returns true if this type is a subtype of the specified type
     */
    @Override
    public boolean subtypeOf(TypeSymbol targetType) {
        Types types = Types.getInstance(this.context);
        return types.isAssignable(this.bType, getTargetBType(targetType));
    }

    private BType getBType() {
        return bType;
    }

    private BType getTargetBType(TypeSymbol typeSymbol) {
        if (typeSymbol.kind() == SymbolKind.TYPE) {
            return ((AbstractTypeSymbol) typeSymbol).getBType();
        }

        return ((BallerinaClassSymbol) typeSymbol).getBType();
    }

    @Override
    public SymbolKind kind() {
        return SymbolKind.ENUM;
    }

    @Override
    public void accept(SymbolVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(SymbolTransformer<T> transformer) {
        return transformer.transform(this);
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
        protected List<AnnotationAttachmentSymbol> annotAttachments = new ArrayList<>();
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

        public EnumSymbolBuilder withAnnotationAttachment(AnnotationAttachmentSymbol annotAttachment) {
            this.annotAttachments.add(annotAttachment);
            return this;
        }

        @Override
        public BallerinaEnumSymbol build() {
            return new BallerinaEnumSymbol(this.name, this.members, this.qualifiers, this.annots, this.annotAttachments,
                                           this.typeDescriptor, this.bSymbol, this.context);
        }
    }
}
