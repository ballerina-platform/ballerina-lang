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
import org.ballerinalang.model.values.BString;
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
 * ballerina.model.datatables:getStringWithType(datatable, any, string)
 *
 * @since 0.88
 */
@BallerinaFunction(
        packageName = "ballerina.lang.datatables",
        functionName = "getStringWithType",
        args = {@Argument(name = "dt", type = TypeEnum.DATATABLE),
                @Argument(name = "column", type = TypeEnum.ANY),
                @Argument(name = "type", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Retrieves the base64encoded string value of the designated column in "
                + "the current row for the given column type: blob, clob, nclob, or binary") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "dt",
        value = "The datatable object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "column",
        value = "The column position of the result as index or name") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "type",
        value = "Database table column type. Supported values are blob, clob, nclob, binary.") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "The column value as a string") })
public class GetStringWithType extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
        BDataTable dataTable = (BDataTable) getRefArgument(ctx, 0);
        BValue index = getRefArgument(ctx, 1);
        String type = getStringArgument(ctx, 0);

        BValue[] result = null;
        if (index instanceof BInteger) {
            result = getBValues(dataTable.get(((BInteger) index).intValue(), type.toLowerCase(Locale.ENGLISH)));
        } else if (index instanceof BString) {
            result = getBValues(dataTable.get(((BString) index).stringValue(), type.toLowerCase(Locale.ENGLISH)));
        }
        return result;
    }
}
