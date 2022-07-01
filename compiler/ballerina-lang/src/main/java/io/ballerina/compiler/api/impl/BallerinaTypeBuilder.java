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

package io.ballerina.compiler.api.impl;

import io.ballerina.compiler.api.TypeBuilder;
import io.ballerina.compiler.api.impl.type.builders.BallerinaArrayTypeBuilder;
import io.ballerina.compiler.api.impl.type.builders.BallerinaErrorTypeBuilder;
import io.ballerina.compiler.api.impl.type.builders.BallerinaFunctionTypeBuilder;
import io.ballerina.compiler.api.impl.type.builders.BallerinaFutureTypeBuilder;
import io.ballerina.compiler.api.impl.type.builders.BallerinaMapTypeBuilder;
import io.ballerina.compiler.api.impl.type.builders.BallerinaObjectTypeBuilder;
import io.ballerina.compiler.api.impl.type.builders.BallerinaRecordTypeBuilder;
import io.ballerina.compiler.api.impl.type.builders.BallerinaSingletonTypeBuilder;
import io.ballerina.compiler.api.impl.type.builders.BallerinaStreamTypeBuilder;
import io.ballerina.compiler.api.impl.type.builders.BallerinaTableTypeBuilder;
import io.ballerina.compiler.api.impl.type.builders.BallerinaTupleTypeBuilder;
import io.ballerina.compiler.api.impl.type.builders.BallerinaTypeDescTypeBuilder;
import io.ballerina.compiler.api.impl.type.builders.BallerinaXMLTypeBuilder;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * Initialize and provide the set of builders required for certain complex types within a single semantic context.
 *
 * @since 2201.2.0
 */
public class BallerinaTypeBuilder extends TypeBuilder {

    public BallerinaTypeBuilder(CompilerContext context) {
        XML_TYPE = new BallerinaXMLTypeBuilder(context);
        MAP_TYPE = new BallerinaMapTypeBuilder(context);
        FUTURE_TYPE = new BallerinaFutureTypeBuilder(context);
        TYPEDESC_TYPE = new BallerinaTypeDescTypeBuilder(context);
        STREAM_TYPE = new BallerinaStreamTypeBuilder(context);
        TUPLE_TYPE = new BallerinaTupleTypeBuilder(context);
        ARRAY_TYPE = new BallerinaArrayTypeBuilder(context);
        ERROR_TYPE = new BallerinaErrorTypeBuilder(context);
        SINGLETON_TYPE = new BallerinaSingletonTypeBuilder(context);
        TABLE_TYPE = new BallerinaTableTypeBuilder(context);
        FUNCTION_TYPE = new BallerinaFunctionTypeBuilder(context);
        OBJECT_TYPE = new BallerinaObjectTypeBuilder(context);
        RECORD_TYPE = new BallerinaRecordTypeBuilder(context);
    }
}
