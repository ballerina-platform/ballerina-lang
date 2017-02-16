/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.nativeimpl.lang.typemappers;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeTypeMapper;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaTypeMapper;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

/**
 * Convert JSON to String
 */
@BallerinaTypeMapper(
        packageName = "ballerina.lang.typemappers",
        typeMapperName = "jsonToString",
        args = {@Argument(name = "j", type = TypeEnum.JSON)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)

public class JSONToString extends AbstractNativeTypeMapper {

    public BValue convert(Context ctx) {
        BJSON msg = (BJSON) getArgument(ctx, 0);
        BString result = new BString(msg.stringValue());
        return result;
    }
}
