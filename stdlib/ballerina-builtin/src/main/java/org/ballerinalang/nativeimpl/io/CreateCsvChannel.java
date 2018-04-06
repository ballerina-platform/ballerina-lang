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

package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.nativeimpl.io.csv.Format;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native function ballerina.io#createCsvChannel.
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
                @ReturnType(type = TypeKind.STRUCT, structType = "DelimitedRecordChannel",
                        structPackage = "ballerina.io"),
                @ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.io")},
        isPublic = true
)
public class CreateCsvChannel extends BlockingNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(CreateDelimitedRecordChannel.class);

    /**
     * The index od the text record channel in ballerina.io#createDelimitedRecordChannel().
     */
    private static final int PATH_INDEX = 0;
    /**
     * The index of the access mode.
     */
    private static final int MODE_INDEX = 1;
    /**
     * Specifies the index of the format enum.
     */
    private static final int FORMAT_INDEX = 2;
    /**
     * Specifies the index in which the charset is defined.
     */
    private static final int CHARSET_INDEX = 3;
    /**
     * The package path of the delimited record channel.
     */
    private static final String RECORD_CHANNEL_PACKAGE = "ballerina.io";
    /**
     * The type of the delimited record channel.
     */
    private static final String STRUCT_TYPE = "DelimitedRecordChannel";

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context) {
        try {
            //File which holds access to the channel information
            String filePath = context.getStringArgument(PATH_INDEX);
            String format = context.getStringArgument(FORMAT_INDEX);
            String charset = context.getStringArgument(CHARSET_INDEX);
            String accessMode = context.getStringArgument(MODE_INDEX);
            BStruct textRecordChannel = BLangConnectorSPIUtil.createBStruct(context, RECORD_CHANNEL_PACKAGE,
                    STRUCT_TYPE);
            DelimitedRecordChannel delimitedRecordChannel = IOUtils.createDelimitedRecordChannel(filePath, charset,
                    accessMode, Format.valueOf(format));
            textRecordChannel.addNativeData(IOConstants.TXT_RECORD_CHANNEL_NAME, delimitedRecordChannel);
            context.setReturnValues(textRecordChannel);
        } catch (Throwable e) {
            String message = "Error occurred while converting character channel to textRecord channel:" + e
                    .getMessage();
            log.error(message, e);
            context.setReturnValues(IOUtils.createError(context, message));
        }
    }

}
