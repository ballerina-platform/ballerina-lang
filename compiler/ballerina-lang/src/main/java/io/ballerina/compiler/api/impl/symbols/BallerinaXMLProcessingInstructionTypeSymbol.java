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
import io.ballerina.compiler.api.symbols.XMLProcessingInstructionTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLSubType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.Optional;

/**
 * Represents the xml:ProcessingInstruction type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaXMLProcessingInstructionTypeSymbol extends AbstractXMLSubTypeSymbol implements
                                                                                    XMLProcessingInstructionTypeSymbol {

    public BallerinaXMLProcessingInstructionTypeSymbol(CompilerContext context, BXMLSubType piType) {
        super(context, TypeDescKind.XML_PROCESSING_INSTRUCTION, piType);
    }

    @Override
    public Optional<TypeSymbol> typeParameter() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getName() {
        return Optional.of(Names.STRING_XML_PI);
    }

    @Override
    public String signature() {
        return "xml:" + Names.STRING_XML_PI;
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
