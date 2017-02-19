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
package org.ballerinalang.nativeimpl.lang.datatables;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BDataTable;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Locale;

/**
 * Native function to get some special type to ballerina supported types. Eg:- Blob, Clob, NClob, Date, Timestamp
 * ballerina.model.datatables:getLong(datatable, int, string)
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.lang.datatables",
        functionName = "getLong",
        args = {@Argument(name = "dt", type = TypeEnum.DATATABLE),
                @Argument(name = "index", type = TypeEnum.INT),
                @Argument(name = "type", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.LONG)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Retrieves the long value of the designated column in the "
                + "current row for the given column type: date, time, or timestamp") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "dt",
        value = "The datatable object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "index",
        value = "The column index position of the result. The first column is 1, the second is 2, etc. ") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "type",
        value = "Database table column type. Supported values are date, time, timestamp") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "long",
        value = "The column value as a long") })
public class GetByIndexLongReturn extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
        BDataTable dataTable = (BDataTable) getArgument(ctx, 0);
        int index = ((BInteger) getArgument(ctx, 1)).intValue();
        String type = (getArgument(ctx, 2)).stringValue();
        return getBValues(dataTable.get(index, type.toLowerCase(Locale.ENGLISH)));
    }
}
