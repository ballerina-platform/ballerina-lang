/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.diagramutil.connector.models.connector.Type;

/**
 * Represents a type info with the position identifier.
 */
public class ResolvedTypeForSymbol {
    private Type type;
    private final LinePosition requestedPosition;

    public ResolvedTypeForSymbol(LinePosition requestedPosition) {
        this.requestedPosition = requestedPosition;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public LinePosition getRequestedPosition() {
        return requestedPosition;
    }

}
