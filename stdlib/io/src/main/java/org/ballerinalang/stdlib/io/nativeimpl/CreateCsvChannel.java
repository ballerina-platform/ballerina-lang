/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.stdlib.io.csv.Format;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extern function ballerina/io#createCsvChannel.
 *
 * @since 0.970.0-alpha1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "createCsvChannel",
        args = {
                @Argument(name = "path", type = TypeKind.STRING),
                @Argument(name = "mode", type = TypeKind.STRING),
                @Argument(name = "rf", type = TypeKind.STRING),
                @Argument(name = "charset", type = TypeKind.STRING)
        },
        returnType = {
                @ReturnType(type = TypeKind.OBJECT, structType = "DelimitedRecordChannel",
                        structPackage = "ballerina/io"),
                @ReturnType(type = TypeKind.ERROR)
        },
        isPublic = true
)
public class CreateCsvChannel {
    private static final Logger log = LoggerFactory.getLogger(CreateCsvChannel.class);

    /**
     * The package path of the delimited record channel.
     */
    private static final String RECORD_CHANNEL_PACKAGE = "ballerina/io";
    /**
     * The type of the delimited record channel.
     */
    private static final String STRUCT_TYPE = "DelimitedRecordChannel";

    public static Object createCsvChannel(Strand strand, String filePath, String accessMode, String format,
                                          String charset) {
        try {
            ObjectValue textRecordChannel = BallerinaValues.createObjectValue(RECORD_CHANNEL_PACKAGE, STRUCT_TYPE);
            DelimitedRecordChannel delimitedRecordChannel =
                    IOUtils.createDelimitedRecordChannelExtended(filePath, charset, accessMode, Format.valueOf(format));
            textRecordChannel.addNativeData(IOConstants.TXT_RECORD_CHANNEL_NAME, delimitedRecordChannel);
            return textRecordChannel;
        } catch (Throwable e) {
            String message = "Error occurred while converting character channel to textRecord channel:" + e
                    .getMessage();
            log.error(message, e);
            return IOUtils.createError(message);
        }
    }
}
