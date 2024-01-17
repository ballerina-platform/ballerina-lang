/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.compiler.api.impl.symbols.resourcepath.util;

import io.ballerina.compiler.api.impl.symbols.BallerinaSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.resourcepath.util.NamedPathSegment;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourcePathSegmentSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * Represents an implementation of a named path segment.
 *
 * @since 2.0.0
 */
public class BallerinaNamedPathSegment extends BallerinaSymbol implements NamedPathSegment {

    public BallerinaNamedPathSegment(BResourcePathSegmentSymbol pathSegmentSymbol, CompilerContext context) {
        super(pathSegmentSymbol.getName().getValue(), SymbolKind.PATH_NAME_SEGMENT, pathSegmentSymbol, context);
    }

    @Override
    public Kind pathSegmentKind() {
        return Kind.NAMED_SEGMENT;
    }

    @Override
    public String signature() {
        return this.name();
    }

    @Override
    @Deprecated
    public String name() {
        return this.getName().orElse("");
    }
}
