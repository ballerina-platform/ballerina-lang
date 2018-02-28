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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * {@code Add} is the function to add data to a table.
 *
 * @since 0.963.0
 */
@BallerinaFunction(packageName = "ballerina.builtin",
                   functionName = "table.add",
                   args = {
                           @Argument(name = "dt",
                                     type = TypeKind.TABLE),
                           @Argument(name = "data",
                                     type = TypeKind.ANY)
                   },
                   isPublic = true)
public class Add extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BTable table = (BTable) getRefArgument(context, 0);
        BStruct data = (BStruct) getRefArgument(context, 1);
        table.addData(data);
        return VOID_RETURN;
    }
}
