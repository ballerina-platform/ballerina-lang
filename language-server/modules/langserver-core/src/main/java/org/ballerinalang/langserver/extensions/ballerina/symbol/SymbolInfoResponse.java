/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.ballerinalang.langserver.extensions.ballerina.symbol;

import io.ballerina.compiler.api.symbols.SymbolKind;
/**
 * Represents the response for getSymbol endpoint of BallerinaSymbolService.
 *
 * @since 2201.1.0
 */
public class SymbolInfoResponse {
    private SymbolKind symbolKind;
    private SymbolDocumentation documentation;


    public void setSymbolDocumentation(SymbolDocumentation documentation) {
        this.documentation = documentation;
    }

    public void setSymbolKind(SymbolKind kind) {
        this.symbolKind = kind;
    }

    public SymbolKind getSymbolKind() {
        return symbolKind;
    }

    public SymbolDocumentation getDocumentation() {
        return documentation;
    }
}
