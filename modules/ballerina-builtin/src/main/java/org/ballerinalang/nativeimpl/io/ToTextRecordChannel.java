/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.channels.base.CharacterChannel;
import org.ballerinalang.nativeimpl.io.channels.base.TextRecordChannel;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Native function ballerina.io#toTextRecordChannel.
 *
 * @since 0.94
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "toTextRecordChannel",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "CharacterChannel", structPackage = "ballerina.io"),
        args = {@Argument(name = "recordSeparator", type = TypeKind.STRING),
                @Argument(name = "fieldSeparator", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRUCT,
                structType = "TextRecordChannel",
                structPackage = "ballerina.io")},
        isPublic = true
)
public class ToTextRecordChannel extends AbstractNativeFunction {

    /**
     * The index od the text record channel in ballerina.io#toTextRecordChannel().
     */
    private static final int TXT_RECORD_CHANNEL_INDEX = 0;
    /**
     * The index of the record channel separator in ballerina.io#toTextRecordChannel().
     */
    private static final int RECORD_SEPARATOR_INDEX = 0;
    /**
     * The index of the field separator in ballerina.io#toTextRecordChannel().
     */
    private static final int FIELD_SEPARATOR_INDEX = 1;
    /**
     * represents the information related to the byte channel.
     */
    private StructInfo textRecordChannelStructInfo;
    /**
     * The package path of the byte channel.
     */
    private static final String TXT_RECORD_CHANNEL_PACKAGE = "ballerina.io";
    /**
     * The type of the byte channel.
     */
    private static final String STRUCT_TYPE = "TextRecordChannel";

    /**
     * Gets the struct related to AbstractChannel.
     *
     * @param context invocation context.
     * @return the struct related to AbstractChannel.
     */
    private StructInfo getCharacterChannelStructInfo(Context context) {
        StructInfo result = textRecordChannelStructInfo;
        if (result == null) {
            PackageInfo timePackageInfo = context.getProgramFile().getPackageInfo(TXT_RECORD_CHANNEL_PACKAGE);
            textRecordChannelStructInfo = timePackageInfo.getStructInfo(STRUCT_TYPE);
        }
        return textRecordChannelStructInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BValue[] execute(Context context) {
        BStruct textRecordChannelInfo;
        BStruct textRecordChannel;
        BValue[] bValues;
        String recordSeparator;
        String fieldSeparator;
        try {
            //File which holds access to the channel information
            textRecordChannelInfo = (BStruct) getRefArgument(context, TXT_RECORD_CHANNEL_INDEX);
            recordSeparator = getStringArgument(context, RECORD_SEPARATOR_INDEX);
            fieldSeparator = getStringArgument(context, FIELD_SEPARATOR_INDEX);

            textRecordChannel = BLangVMStructs.createBStruct(getCharacterChannelStructInfo(context));

            //Will get the relevant byte channel and will create a character channel
            CharacterChannel characterChannel = (CharacterChannel) textRecordChannelInfo.getNativeData(IOConstants
                    .CHARACTER_CHANNEL_NAME);
            TextRecordChannel bCharacterChannel = new TextRecordChannel(characterChannel, recordSeparator,
                    fieldSeparator);
            textRecordChannel.addNativeData(IOConstants.TXT_RECORD_CHANNEL_NAME, bCharacterChannel);
            bValues = getBValues(textRecordChannel);
        } catch (Throwable e) {
            String message = "Error occurred while converting character channel to textRecord channel. ";
            throw new BallerinaException(message + e.getMessage(), context);
        }
        return bValues;
    }
}
