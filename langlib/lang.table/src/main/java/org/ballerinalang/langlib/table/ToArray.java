/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.table;

import io.ballerina.runtime.api.BValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.types.BTableType;

import java.util.Collection;

/**
 * Function for returning the values of the table as an array. T[] vals = tbl.toArray();
 *
 * @since 1.3.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.table",
//        functionName = "toArray",
//        args = {@Argument(name = "tbl", type = TypeKind.TABLE)},
//        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.ANYDATA)},
//        isPublic = true
//)
public class ToArray {

    public static BArray toArray(BTable tbl) {
        Type constrainedType = ((BTableType) tbl.getType()).getConstrainedType();

        Collection values = tbl.values();
        //Basic constrain types not applicable for table type
        return BValueCreator.createArrayValue(values.toArray(), new BArrayType(constrainedType));
    }
}
