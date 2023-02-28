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
import io.ballerina.compiler.api.impl.LangLibrary;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Represents an intersection type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaIntersectionTypeSymbol extends AbstractTypeSymbol implements IntersectionTypeSymbol {

    private List<TypeSymbol> memberTypes;
    private TypeSymbol effectiveType;
    private String signature;

    public BallerinaIntersectionTypeSymbol(CompilerContext context, BIntersectionType intersectionType) {
        super(context, TypeDescKind.INTERSECTION, intersectionType);
    }

    @Override
    public List<TypeSymbol> memberTypeDescriptors() {
        if (this.memberTypes != null) {
            return this.memberTypes;
        }

        List<TypeSymbol> members = new ArrayList<>();
        TypesFactory typesFactory = TypesFactory.getInstance(this.context);

        for (BType memberType : ((BIntersectionType) this.getBType()).getConstituentTypes()) {
            members.add(typesFactory.getTypeDescriptor(memberType));
        }

        this.memberTypes = Collections.unmodifiableList(members);
        return this.memberTypes;
    }

    @Override
    public TypeSymbol effectiveTypeDescriptor() {
        if (this.effectiveType != null) {
            return this.effectiveType;
        }

        TypesFactory typesFactory = TypesFactory.getInstance(this.context);
        BType effectiveType = ((BIntersectionType) this.getBType()).effectiveType;
        this.effectiveType = typesFactory.getTypeDescriptor(effectiveType,
                effectiveType != null ? effectiveType.tsymbol : null,
                false, false, true);
        return this.effectiveType;
    }

    @Override
    public List<FunctionSymbol> langLibMethods() {
        if (this.langLibFunctions == null) {
            if (this.effectiveTypeDescriptor().typeKind() == TypeDescKind.OBJECT) {
                this.langLibFunctions = this.effectiveTypeDescriptor().langLibMethods();
            } else if (this.effectiveTypeDescriptor().typeKind() == TypeDescKind.TYPE_REFERENCE) {
                TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) this.effectiveTypeDescriptor();
                this.langLibFunctions = typeRef.typeDescriptor().langLibMethods();
            } else {
                LangLibrary langLibrary = LangLibrary.getInstance(this.context);
                List<FunctionSymbol> functions = langLibrary.getMethods(
                        ((AbstractTypeSymbol) this.effectiveTypeDescriptor()).getBType());
                this.langLibFunctions = filterLangLibMethods(functions, this.getBType());
            }
        }

        return this.langLibFunctions;
    }

    @Override
    public String signature() {
        if (this.signature != null) {
            return this.signature;
        }

        List<TypeSymbol> memberTypes = this.memberTypeDescriptors();
        StringJoiner joiner = new StringJoiner(" & ");
        memberTypes.forEach(typeDescriptor -> joiner.add(typeDescriptor.signature()));
        this.signature = joiner.toString();
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
