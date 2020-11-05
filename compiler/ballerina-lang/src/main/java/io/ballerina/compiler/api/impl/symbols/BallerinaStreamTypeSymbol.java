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
import io.ballerina.compiler.api.impl.TypesFactory;
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Represents an stream type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaStreamTypeSymbol extends AbstractTypeSymbol implements StreamTypeSymbol {

    private List<TypeSymbol> typeParameters;

    public BallerinaStreamTypeSymbol(CompilerContext context, ModuleID moduleID, BStreamType streamType) {
        super(context, TypeDescKind.STREAM, moduleID, streamType);
    }

    @Override
    public List<TypeSymbol> typeParameters() {
        if (this.typeParameters == null) {
            List<TypeSymbol> typeParams = new ArrayList<>();
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            typeParams.add(typesFactory.getTypeDescriptor(((BStreamType) this.getBType()).constraint));
            this.typeParameters = Collections.unmodifiableList(typeParams);
        }

        return this.typeParameters;
    }

    @Override
    public String signature() {
        String memberSignature;
        if (this.typeParameters().isEmpty()) {
            memberSignature = "()";
        } else {
            StringJoiner joiner = new StringJoiner(", ");
            this.typeParameters().forEach(typeDescriptor -> joiner.add(typeDescriptor.signature()));
            memberSignature = joiner.toString();
        }
        return "stream<" + memberSignature + ">";
    }
}
