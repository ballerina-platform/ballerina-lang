/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerina.core.nativeimpl.lang.datatable;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BDataTable;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

/**
 * Native function to get boolean value of a given column index.
 * ballerina.lang.datatable:getBoolean(datatable, int)
 */
@BallerinaFunction(
        packageName = "ballerina.lang.datatable",
        functionName = "getBoolean",
        args = {@Argument(name = "datatable", type = TypeEnum.DATATABLE),
                @Argument(name = "index", type = TypeEnum.INT)},
        returnType = {@ReturnType(type = TypeEnum.BOOLEAN)},
        isPublic = true
)
public class GetBooleanByIndex extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
        BDataTable dataframe = (BDataTable) getArgument(ctx, 0);
        int index = ((BInteger) getArgument(ctx, 1)).intValue();
        return getBValues(new BBoolean(dataframe.getBoolean(index)));
    }
}
