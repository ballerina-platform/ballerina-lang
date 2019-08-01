/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.system.nativeimpl;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.AbstractNativeChannel;
import org.ballerinalang.stdlib.io.channels.BlobChannel;
import org.ballerinalang.stdlib.io.channels.BlobIOChannel;
import org.ballerinalang.stdlib.system.utils.SystemConstants;
import org.ballerinalang.stdlib.system.utils.SystemUtils;

/**
 * External function for ballerina.system:Process.stderr.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = SystemConstants.ORG_NAME,
        packageName = SystemConstants.PACKAGE_NAME,
        functionName = "stderr",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Process",
        structPackage = "ballerina/system"),
        returnType = { @ReturnType(type = TypeKind.OBJECT) }        
)
public class Stderr extends AbstractNativeChannel {

    public static ObjectValue stderr(Strand strand, ObjectValue objVal) {
        Process process = SystemUtils.processFromObject(objVal);
        InputStream in = process.getErrorStream();
        ReadableByteChannel readableByteChannel = Channels.newChannel(in);        
        return createChannel(new BlobIOChannel(new BlobChannel(readableByteChannel)));
    }

}
