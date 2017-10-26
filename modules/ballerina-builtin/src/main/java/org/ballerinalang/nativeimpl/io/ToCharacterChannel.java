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
import org.ballerinalang.nativeimpl.io.channels.base.AbstractChannel;
import org.ballerinalang.nativeimpl.io.channels.base.CharacterChannel;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Native function ballerina.io#ToCharacterChannel.
 *
 * @since 0.94
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "toCharacterChannel",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ByteChannel", structPackage = "ballerina.io"),
        args = {@Argument(name = "encoding", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRUCT,
                structType = "CharacterChannel",
                structPackage = "ballerina.io")},
        isPublic = true
)
public class ToCharacterChannel extends AbstractNativeFunction {
    /**
     * Specifies the index of the character channel  in ballerina.io#toCharacterChannel.
     */
    private static final int CHAR_CHANNEL_INDEX = 0;

    /**
     * Specifies the index of the encoding in ballerina.io#toCharacterChannel.
     */
    private static final int ENCODING_INDEX = 0;
    /**
     * represents the information related to the byte channel.
     */
    private StructInfo characterChannelStructInfo;

    /**
     * The package path of the byte channel.
     */
    private static final String CHAR_CHANNEL_PACKAGE = "ballerina.io";

    /**
     * The type of the byte channel struct.
     */
    private static final String STRUCT_TYPE = "CharacterChannel";

    /**
     * Gets the struct related to AbstractChannel.
     *
     * @param context invocation context.
     * @return the struct related to AbstractChannel.
     */
    private StructInfo getCharacterChannelStructInfo(Context context) {
        StructInfo result = characterChannelStructInfo;
        if (result == null) {
            PackageInfo timePackageInfo = context.getProgramFile().getPackageInfo(CHAR_CHANNEL_PACKAGE);
            characterChannelStructInfo = timePackageInfo.getStructInfo(STRUCT_TYPE);
        }
        return characterChannelStructInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BValue[] execute(Context context) {
        BStruct characterChannelInfo;
        BStruct characterChannel;
        BValue[] bValues;
        String encoding;
        try {
            //File which holds access to the channel information
            characterChannelInfo = (BStruct) getRefArgument(context, CHAR_CHANNEL_INDEX);
            encoding = getStringArgument(context, ENCODING_INDEX);
            characterChannel = BLangVMStructs.createBStruct(getCharacterChannelStructInfo(context));
            //Will get the relevant byte channel and will create a character channel
            AbstractChannel byteChannel = (AbstractChannel) characterChannelInfo.getNativeData(IOConstants
                    .BYTE_CHANNEL_NAME);
            CharacterChannel bCharacterChannel = new CharacterChannel(byteChannel, encoding);
            characterChannel.addNativeData(IOConstants.CHARACTER_CHANNEL_NAME, bCharacterChannel);
            bValues = getBValues(characterChannel);
        } catch (Throwable e) {
            String message = "Error occurred while converting byte channel to character channel. ";
            throw new BallerinaException(message + e.getMessage(), context);
        }
        return bValues;
    }
}
