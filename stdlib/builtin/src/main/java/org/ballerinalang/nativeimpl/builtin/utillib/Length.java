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

package org.ballerinalang.nativeimpl.builtin.utillib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Extern function to get length of any type.
 *
 */
@BallerinaFunction(orgName = "ballerina", packageName = "builtin",
        functionName = "length",
        args = { @Argument(name = "value", type = TypeKind.ANY)},
        returnType = {@ReturnType(type = TypeKind.INT)})
public class Length extends BlockingNativeCallableUnit {

    public void execute(Context ctx) {
        Object obj =  ctx.getRefArgument(0);
        long length = 0;
        if (obj instanceof BNewArray) { // Arrays, Tuples and Json
            length = ((BNewArray) obj).size();
        } else if (obj instanceof BMap) { // Map and Records
            length = ((BMap<String, BValue>) obj).size();
        } else if (obj instanceof BTable) { // Table
            length = ((BTable) obj).length();
        } else if (obj instanceof BXML) { // XML
            length = ((BXML) obj).length();
        }
        ctx.setReturnValues(new BInteger(length));
    }
}
