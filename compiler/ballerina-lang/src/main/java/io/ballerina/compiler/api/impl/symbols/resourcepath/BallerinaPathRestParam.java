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

package io.ballerina.compiler.api.impl.symbols.resourcepath;

import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.PathRestParam;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * Represents an implementation of the path rest param resource path.
 *
 * @since 2.0.0
 */
public class BallerinaPathRestParam implements PathRestParam {

    private final CompilerContext context;
    private final BSymbol internalSymbol;
    private PathParameterSymbol pathParam;
    private final String name;

    public BallerinaPathRestParam(String name, BSymbol internalSymbol, CompilerContext context) {
        this.name = name;
        this.internalSymbol = internalSymbol;
        this.context = context;
    }

    @Override
    public PathParameterSymbol parameter() {
        if (this.pathParam != null) {
            return this.pathParam;
        }

        SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
        this.pathParam = symbolFactory.createPathParamSymbol(this.name, this.internalSymbol,
                PathSegment.Kind.PATH_REST_PARAMETER);
        return this.pathParam;
    }

    @Override
    public Kind kind() {
        return Kind.PATH_REST_PARAM;
    }

    @Override
    public String signature() {
        return this.parameter().signature();
    }
}
