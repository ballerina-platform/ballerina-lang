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
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a table type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaTableTypeSymbol extends AbstractTypeSymbol implements TableTypeSymbol {

    private TypeSymbol rowTypeParameter;
    private TypeSymbol keyConstraintTypeParameter;

    public BallerinaTableTypeSymbol(CompilerContext context, ModuleID moduleID, BTableType tableType) {
        super(context, TypeDescKind.TABLE, moduleID, tableType);
    }

    @Override
    public TypeSymbol rowTypeParameter() {
        if (this.rowTypeParameter == null) {
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            this.rowTypeParameter = typesFactory.getTypeDescriptor(((BTableType) this.getBType()).constraint);
        }

        return this.rowTypeParameter;
    }

    @Override
    public Optional<TypeSymbol> keyConstraintTypeParameter() {
        if (this.rowTypeParameter == null) {
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            this.keyConstraintTypeParameter = typesFactory.getTypeDescriptor(
                    ((BTableType) this.getBType()).keyTypeConstraint);
        }

        return Optional.ofNullable(this.keyConstraintTypeParameter);
    }

    @Override
    public List<String> keySpecifiers() {
        return Collections.emptyList();
    }

    @Override
    public String signature() {
        // TODO: Implement the correct signature
        return "table";
    }
}
