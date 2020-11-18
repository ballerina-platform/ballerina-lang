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

import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.WorkerSymbol;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

/**
 * Represents a ballerina worker.
 *
 * @since 2.0.0
 */
public class BallerinaWorkerSymbol extends BallerinaSymbol implements WorkerSymbol {

    private BallerinaTypeDescriptor returnType;

    private BallerinaWorkerSymbol(String name,
                                      PackageID moduleID,
                                      SymbolKind ballerinaSymbolKind,
                                      BallerinaTypeDescriptor returnType,
                                      BSymbol symbol) {
        super(name, moduleID, ballerinaSymbolKind, symbol);
        this.returnType = returnType;
    }

    /**
     * Get the return type.
     *
     * @return {@link BallerinaTypeDescriptor} return type of the worker.
     */
    @Override
    public BallerinaTypeDescriptor returnType() {
        return returnType;
    }

    /**
     * Represents Ballerina Worker Symbol Builder.
     */
    public static class WorkerSymbolBuilder extends SymbolBuilder<WorkerSymbolBuilder> {

        protected BallerinaTypeDescriptor returnType;

        public WorkerSymbolBuilder(String name, PackageID moduleID, BSymbol symbol) {
            super(name, moduleID, SymbolKind.WORKER, symbol);
        }

        @Override
        public BallerinaWorkerSymbol build() {
            return new BallerinaWorkerSymbol(this.name,
                    this.moduleID,
                    this.ballerinaSymbolKind,
                    this.returnType,
                    this.bSymbol);
        }

        public WorkerSymbolBuilder withReturnType(BallerinaTypeDescriptor typeDescriptor) {
            this.returnType = typeDescriptor;
            return this;
        }
    }
}
