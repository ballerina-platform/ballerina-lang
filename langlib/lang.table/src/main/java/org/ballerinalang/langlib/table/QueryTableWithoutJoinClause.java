/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.table;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * This class represents the implementation of creating a new table from an existing in memory table. The records
 * which are fetched according to the given sqlQuery is inserted to the new in memory table.
 */
@BallerinaFunction(orgName = "ballerina", packageName = "lang.table",
        functionName = "queryTableWithoutJoinClause",
        args = {
                @Argument(name = "sqlQuery",
                        type = TypeKind.STRING),
                @Argument(name = "fromTable",
                        type = TypeKind.TABLE),
                @Argument(name = "parameters",
                        type = TypeKind.ARRAY),
                @Argument(name = "retType",
                        type = TypeKind.TYPEDESC)
        },
        returnType = {@ReturnType(type = TypeKind.TABLE)})
public class QueryTableWithoutJoinClause extends BlockingNativeCallableUnit {
    /**
     * Where extern function logic is implemented.
     *
     * @param context Current Context instance
     */
    @Override
    public void execute(Context context) {
        String query = context.getStringArgument(0);
        BTable fromTable = (BTable) context.getRefArgument(0);
        BValueArray array = (BValueArray) context.getRefArgument(1);
        BMap<String, BValue> tableTypeStruct = (BMap<String, BValue>) context.getRefArgument(2);
        context.setReturnValues(new BTable(query, fromTable, null, (BStructureType) tableTypeStruct.getType(), array));
    }

    public static TableValue queryTableWithoutJoinClause(Strand strand, String query, TableValue fromTable,
                                                         Object array, Object tableLiteral) {
        return new TableValue(query, fromTable, null,
                              (org.ballerinalang.jvm.types.BStructureType) TypeChecker.getType(tableLiteral),
                              (ArrayValue) array);
    }
}
