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
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;

/**
 * Represents an error type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaErrorTypeSymbol extends AbstractTypeSymbol implements ErrorTypeSymbol {

    private TypeSymbol detail;

    public BallerinaErrorTypeSymbol(CompilerContext context, ModuleID moduleID, BErrorType errorType) {
        super(context, TypeDescKind.ERROR, moduleID, errorType);
    }

    /**
     * Get the detail type descriptor.
     *
     * @return {@link TypeSymbol} detail
     */
    @Override
    public TypeSymbol detailTypeDescriptor() {
        if (this.detail == null) {
            this.detail = TypesFactory.getTypeDescriptor(((BErrorType) this.getBType()).getDetailType());
        }
        return this.detail;
    }

    @Override
    public String signature() {
        String definitionName = getBType().tsymbol.name.value;
        if (this.moduleID().moduleName().equals("lang.annotations") && this.moduleID().orgName().equals("ballerina")) {
            return definitionName;
        }
        return this.moduleID().orgName() + Names.ORG_NAME_SEPARATOR +
                this.moduleID().moduleName() + Names.VERSION_SEPARATOR + this.moduleID().version() + ":" +
                definitionName;
    }
}
