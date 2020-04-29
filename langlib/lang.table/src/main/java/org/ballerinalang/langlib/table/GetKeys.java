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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTableType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.TableValueImpl;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Extern function to get key arrays from the table.
 * ballerina.model.table:keys()
 *
 * @since 1.3.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.table",
        functionName = "keys",
        args = {@Argument(name = "tbl", type = TypeKind.TABLE)},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.ANYDATA)},
        isPublic = true
)
public class GetKeys {

    public static ArrayValue keys(Strand strand, TableValueImpl tbl) {
        BType tableKeyType = ((BTableType) tbl.getType()).getKeyType();
        tableKeyType = tableKeyType != null ? tableKeyType : BTypes.typeAnydata;
        return new ArrayValueImpl((Object[]) tbl.getKeys(), new BArrayType(tableKeyType));
    }
}
