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

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extern function ballerina/io#nextTextRecords.
 *
 * @since 0.94
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "getNext",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = "ReadableTextRecordChannel",
                structPackage = "ballerina/io"),
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.STRING),
                      @ReturnType(type = TypeKind.ERROR)},
        isPublic = true
)
public class NextTextRecord {

    private static final Logger log = LoggerFactory.getLogger(NextTextRecord.class);

    public static Object getNext(Strand strand, ObjectValue channel) {
        DelimitedRecordChannel delimitedRecordChannel = (DelimitedRecordChannel) channel.getNativeData(
                IOConstants.TXT_RECORD_CHANNEL_NAME);
        if (delimitedRecordChannel.hasReachedEnd()) {
            return IOUtils.createEoFError();
        } else {
            try {
                return new ArrayValueImpl(delimitedRecordChannel.read());
            } catch (BallerinaIOException e) {
                log.error("error occurred while reading next text record from ReadableTextRecordChannel", e);
                return IOUtils.createError(e);
            }
        }
    }

}
