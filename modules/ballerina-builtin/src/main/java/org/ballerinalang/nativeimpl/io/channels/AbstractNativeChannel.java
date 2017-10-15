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

package org.ballerinalang.nativeimpl.io.channels;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.nativeimpl.io.channels.base.BByteChannel;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * This will required to be implemented for native channels
 * i.e file open, socket open
 */
public abstract class AbstractNativeChannel extends AbstractNativeFunction {

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
     * Will be the set of actions which should be executed prior to identifying the ByteChannel
     *
     * @param context holds the context received from Ballerina
     * @return the channel which holds the reference t
     */
    public abstract BByteChannel flow(Context context) throws BallerinaException;


    /**
     * {@inheritDoc}
     */
    @Override
    public BValue[] execute(Context context) {
        BByteChannel channel = flow(context);
        BStruct channelStruct = BLangVMStructs.createBStruct(getByteChannelStructInfo(context));
        channelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, channel);
        return getBValues(channelStruct);
    }
}
