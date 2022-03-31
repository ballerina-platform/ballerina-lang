/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.compiler.api.types;

import org.wso2.ballerinalang.compiler.util.CompilerContext;

public class BallerinaTypeBuilder extends TypeBuilder {

    private static final CompilerContext.Key<BallerinaTypeBuilder> TYPE_BUILDER_KEY = new CompilerContext.Key<>();
    public final CompilerContext context;

    public BallerinaTypeBuilder(CompilerContext context) {
        context.put(TYPE_BUILDER_KEY, this);
        this.context = context;
        this.XML = new BallerinaXMLBuilder(context);
        this.MAP = new BallerinaMapBuilder(context);
        this.FUTURE = new BallerinaFutureTypeBuilder(context);
        this.TYPEDESC = new BallerinaTypeDescTypeBuilder(context);
        this.STREAM = new BallerinaStreamTypeBuilder(context);
    }

    public static TypeBuilder getInstance(CompilerContext context) {
        TypeBuilder typeBuilder = context.get(TYPE_BUILDER_KEY);
        if (typeBuilder == null) {
            typeBuilder = new BallerinaTypeBuilder(context);
        }

        return typeBuilder;
    }
}
