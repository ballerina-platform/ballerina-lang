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

package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.channels.BByteChannel;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.nio.channels.ByteChannel;

/**
 * Native function ballerina.io#ToByteChannel
 *
 * @since 0.90
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "toByteChannel",
        args = {@Argument(name = "file", type = TypeKind.STRUCT)},
        returnType = {@ReturnType(type = TypeKind.STRUCT,
                structType = "ByteChannel",
                structPackage = "ballerina.io")},
        isPublic = true
)
public class ToByteChannel extends AbstractNativeFunction {

    /**
     * represents the information related to the byte channel
     */
    private StructInfo byteChannelStructInfo;

    /**
     * The package path of the byte channel
     */
    private static final String BYTE_CHANNEL_PACKAGE = "ballerina.io";

    /**
     * The type of the byte channel
     */
    private static final String STRUCT_TYPE = "ByteChannel";

    /**
     * Represents the type of the channel
     */
    private static final String CHANNEL_TYPE = "file";

    /**
     * Gets the struct related to BByteChannel
     *
     * @param context invocation context
     * @return the struct related to BByteChannel
     */
    private StructInfo getByteChannelStructInfo(Context context) {
        StructInfo result = byteChannelStructInfo;
        if (result == null) {
            PackageInfo timePackageInfo = context.getProgramFile().getPackageInfo(BYTE_CHANNEL_PACKAGE);
            byteChannelStructInfo = timePackageInfo.getStructInfo(STRUCT_TYPE);
        }
        return byteChannelStructInfo;
    }

    /**
     * Converts File object to a BByteChannel
     * <p>
     * {@inheritDoc}
     */
    @Override
    public BValue[] execute(Context context) {

        BStruct file;
        BStruct channel;
        BValue[] bValues;

        try {
            //File which holds access to the channel information
            file = (BStruct) getRefArgument(context, 0);
            //TODO since we're passing the channel in the constructor we will not need to specify the field
            channel = BLangVMStructs.createBStruct(getByteChannelStructInfo(context), CHANNEL_TYPE);
            channel.setStringField(0, CHANNEL_TYPE);
            ByteChannel fileChannel = (ByteChannel) file.getNativeData("filechannel");
            //Will create a BBytesChannel out of the given ByteChannel
            BByteChannel bByteChannel = new BByteChannel(fileChannel);
            channel.addNativeData(IOConstants.BYTE_CHANNEL_NAME, bByteChannel);
            bValues = getBValues(channel);
        } catch (Throwable e) {
            String message = "Error occurred while converting file to byte channel. ";
            throw new BallerinaException(message + e.getMessage(), context);
        }


        return bValues;
    }
}
