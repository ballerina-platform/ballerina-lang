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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.lang.system;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Native function ballerina.lang.system:getDateFormat.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.lang.system",
        functionName = "getDateFormat",
        args = { @Argument(name = "format", type = TypeEnum.STRING) },
        returnType = { @ReturnType(type = TypeEnum.STRING) },
        isPublic = true)

public class DateFormat extends AbstractNativeFunction {

    @Override public BValue[] execute(Context context) {

        String format = getArgument(context, 0).stringValue();
        final Date date = new Date();
        final TimeZone timeZone = TimeZone.getTimeZone("GMT");
        final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(timeZone);
        final String amzDate = dateFormat.format(date);

        return getBValues(new BString(amzDate));
    }
}
