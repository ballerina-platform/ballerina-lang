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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.symbols.ClassFieldSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.util.Collections;
import java.util.List;

import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS_FIELD;

/**
 * Represents a field in a class.
 *
 * @since 2.0.0
 */
public class BallerinaClassFieldSymbol extends BallerinaObjectFieldSymbol implements ClassFieldSymbol {

    public BallerinaClassFieldSymbol(CompilerContext context, BField bField) {
        super(context, bField, CLASS_FIELD);
    }

    @Override
    public boolean hasDefaultValue() {
        return false;
    }

    @Override
    public List<Qualifier> qualifiers() {
        if ((this.bField.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
            return List.of(Qualifier.PUBLIC);
        } else if ((this.bField.symbol.flags & Flags.PRIVATE) == Flags.PRIVATE) {
            return List.of(Qualifier.PRIVATE);
        }

        return Collections.emptyList();
    }
}
