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
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.mime.util.MimeConstants.INVALID_CONTENT_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_PACKAGE_MIME;

/**
 * Construct MediaType struct from Content-Type string.
 *
 * @since 0.96
 */
@BallerinaFunction(orgName = "ballerina", packageName = "mime",
        functionName = "getMediaType",
        args = {@Argument(name = "contentType",
                type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.RECORD)},
        isPublic = true)
public class GetMediaType {

    public static Object getMediaType(Strand strand, String contentType) {
        try {
            ObjectValue mediaType = BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_MIME, MEDIA_TYPE);
            mediaType = MimeUtil.parseMediaType(mediaType, contentType);
            return mediaType;
        } catch (Throwable e) {
            return MimeUtil.createError(INVALID_CONTENT_TYPE, e.getMessage());
        }
    }
}
