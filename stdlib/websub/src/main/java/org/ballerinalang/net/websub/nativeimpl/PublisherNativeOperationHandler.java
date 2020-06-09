/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.websub.nativeimpl;

import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.net.websub.BallerinaWebSubException;
import org.ballerinalang.net.websub.WebSubUtils;
import org.ballerinalang.net.websub.hub.Hub;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOConstants;

import java.io.IOException;

/**
 * This class contains interop external functions related to Publisher.
 *
 * @since 1.1.0
 */
public class PublisherNativeOperationHandler {

    /**
     * Validates that the hub URL passed indicates the underlying Ballerina Hub (if started) and publish against a topic
     * in the default Ballerina Hub's underlying broker.
     *
     * @param hubUrl  the publisher URL of the Ballerina WebSub Hub as included in the `websub:Hub` object
     * @param topic   the topic for which the update should happen
     * @param content the content to send to subscribers, with the payload and content-type specified
     * @return `error` if an error occurred during publishing
     */
    public static Object validateAndPublishToInternalHub(BString hubUrl, BString topic,
                                                         MapValue<BString, Object> content) {
        Hub hubInstance = Hub.getInstance();
        if (hubInstance.isStarted() && hubInstance.getPublishUrl().equals(hubUrl.getValue())) {
            try {
                Hub.getInstance().publish(topic.getValue(), content);
            } catch (BallerinaWebSubException e) {
                return WebSubUtils.createError(e.getMessage());
            }
            return null;
        }
        return WebSubUtils.createError("Internal Ballerina Hub not initialized or incorrectly referenced");
    }

    /**
     * Creates a byte array to use as the content when a ByteChannel is specified to indicate the content to be sent as
     * the WebSub notification.
     *
     * @param byteChannel the specified byte channel
     * @return the constructed byte array
     */
    public static ArrayValue constructByteArray(ObjectValue byteChannel) {
        Channel channel = (Channel) byteChannel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
        if (channel == null) {
            return new ArrayValueImpl(new byte[0]);
        }
        try {
            byte[] byteData = MimeUtil.getByteArray(channel.getInputStream());
            channel.close();
            return new ArrayValueImpl(byteData);
        } catch (IOException e) {
            return new ArrayValueImpl(new byte[0]);
        }
    }
}
