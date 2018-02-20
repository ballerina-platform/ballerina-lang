/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.builtin.tablelib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.WorkerContext;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.program.BLangFunctions;

/**
 * {@code Remove} is the function to remove data from a table.
 *
 * @since 0.963.0
 */
@BallerinaFunction(packageName = "ballerina.builtin",
                   functionName = "table.remove",
                   args = {
                           @Argument(name = "dt", type = TypeKind.TABLE),
                           @Argument(name = "func", type = TypeKind.ANY)
                   },
                   returnType = {@ReturnType(type = TypeKind.INT)},
                   isPublic = true)
public class Remove extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BTable table = (BTable) getRefArgument(context, 0);
        BFunctionPointer lambDaFunction = (BFunctionPointer) getRefArgument(context, 1);
        int deletedCount = 0;
        while (table.hasNext(false)) {
            BStruct data = table.getNext();
            BValue[] args = { data };
            Context newContext = new WorkerContext(context.getProgramFile(), context);
            BValue[] returns = BLangFunctions
                    .invokeFunction(context.getProgramFile(), lambDaFunction.value().getFunctionInfo(), args,
                            newContext);
            if (((BBoolean) returns[0]).booleanValue()) {
                ++deletedCount;
                table.removeData(data);
            }
        }
        BInteger intValue = new BInteger(deletedCount);
        return getBValues(intValue);
    }
}
