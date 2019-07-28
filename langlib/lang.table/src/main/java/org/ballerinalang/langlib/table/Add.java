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
package org.ballerinalang.langlib.table;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BLangFreezeException;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * {@code Add} is the function to add data to a table.
 *
 * @since 0.963.0
 */
@BallerinaFunction(orgName = "ballerina", packageName = "lang.table",
                   functionName = "add",
                   args = {
                           @Argument(name = "dt",
                                     type = TypeKind.TABLE),
                           @Argument(name = "data",
                                     type = TypeKind.ANY)
                   },
                   isPublic = true)
public class Add extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        BTable table = (BTable) context.getRefArgument(0);
        BMap<String, BValue> data = (BMap) context.getRefArgument(1);
        try {
            table.performAddOperation(data, context);
        } catch (BLangFreezeException e) {
            throw new BallerinaException(e.getMessage(), "Failed to add data to the table: " + e.getDetail());
        }
    }

    public static Object add(Strand strand, TableValue table, Object data) {
        try {
            return table.performAddOperation((MapValueImpl<String, Object>) data);
        } catch (org.ballerinalang.jvm.util.exceptions.BLangFreezeException e) {
            return BallerinaErrors.createError(e.getMessage(), "Failed to add data to the table: " + e.getDetail());
        }
    }
}
