/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.time;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.ballerinalang.nativeimpl.Utils.STRUCT_TYPE_TIME;

/**
 * Convert a Time to string in the given format.
 *
 * @since 0.89
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "time",
        functionName = "format",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = STRUCT_TYPE_TIME, structPackage = "ballerina.time"),
        args = {@Argument(name = "pattern", type = TypeKind.UNION)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class Format extends AbstractTimeFunction {

    @Override
    public void execute(Context context) {
        BStruct timeStruct = ((BStruct) context.getRefArgument(0));
        BString pattern = (BString) context.getNullableRefArgument(1);

        switch (pattern.stringValue()) {
            case "RFC_1123":
                ZonedDateTime zonedDateTime = getZonedDateTime(timeStruct);
                String formattedDateTime = zonedDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
                context.setReturnValues(new BString(formattedDateTime));
                break;
            default:
                context.setReturnValues(new BString(getFormattedtString(timeStruct, pattern.stringValue())));
        }
    }
}
