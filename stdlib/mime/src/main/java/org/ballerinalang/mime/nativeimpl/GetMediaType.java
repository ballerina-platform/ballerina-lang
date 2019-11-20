/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.mime.util.MimeUtil;

import static org.ballerinalang.mime.nativeimpl.AbstractGetPayloadHandler.getErrorMsg;
import static org.ballerinalang.mime.util.MimeConstants.INVALID_CONTENT_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_MIME_PKG_ID;

/**
 * Construct MediaType struct from Content-Type string.
 *
 * @since 0.96
 */
public class GetMediaType {

    public static Object getMediaType(String contentType) {
        try {
            ObjectValue mediaType = BallerinaValues.createObjectValue(PROTOCOL_MIME_PKG_ID, MEDIA_TYPE);
            mediaType = MimeUtil.parseMediaType(mediaType, contentType);
            return mediaType;
        } catch (Throwable err) {
            return MimeUtil.createError(INVALID_CONTENT_TYPE, getErrorMsg(err));
        }
    }
}
