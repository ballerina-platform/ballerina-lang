/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerina.compiler.api.model;

import org.ballerina.compiler.api.types.TypeDescriptor;
import org.ballerinalang.model.elements.PackageID;

/**
 * Represents a ballerina worker.
 * 
 * @since 1.3.0
 */
public class BallerinaWorkerSymbol extends BallerinaSymbol {
    // TODO: represent the meta information 
    private BallerinaWorkerSymbol(String name, PackageID moduleID, BallerinaSymbolKind ballerinaSymbolKind) {
        super(name, moduleID, ballerinaSymbolKind);
    }

    /**
     * Represents Ballerina Worker Symbol Builder.
     */
    public static class WorkerSymbolBuilder extends SymbolBuilder<WorkerSymbolBuilder> {
        protected TypeDescriptor returnType;
        
        /**
         * Symbol Builder's Constructor.
         *
         * @param name Symbol Name
         * @param moduleID module ID of the symbol
         */
        public WorkerSymbolBuilder(String name, PackageID moduleID) {
            super(name, moduleID, BallerinaSymbolKind.WORKER);
        }

        public BallerinaWorkerSymbol build() {
            return new BallerinaWorkerSymbol(this.name, this.moduleID, this.ballerinaSymbolKind);
        }

        public WorkerSymbolBuilder withReturnType(TypeDescriptor typeDescriptor) {
            this.returnType = typeDescriptor;
            return this;
        }
    }
}
