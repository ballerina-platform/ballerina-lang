/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.mime.util.MimeUtil;

import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_STRUCT;
import static org.ballerinalang.mime.util.MimeConstants.DISPOSITION_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_MIME_PKG_ID;

/**
 * Functionality related to content disposition.
 *
 * @since 1.1.0
 */
public class ContentDisposition {

    public static BObject getContentDispositionObject(BString contentDisposition) {
        BObject contentDispositionObj = ValueCreator.createObjectValue(PROTOCOL_MIME_PKG_ID,
                                                                       CONTENT_DISPOSITION_STRUCT);
        MimeUtil.populateContentDispositionObject(contentDispositionObj, contentDisposition.getValue());
        return contentDispositionObj;
    }

    public static BString convertContentDispositionToString(BObject contentDispositionObj) {
        StringBuilder dispositionBuilder = new StringBuilder();
        if (contentDispositionObj != null) {
            String disposition = String.valueOf(contentDispositionObj.get(DISPOSITION_FIELD));
            if (!disposition.isEmpty()) {
                dispositionBuilder.append(disposition);
                MimeUtil.convertDispositionObjectToString(dispositionBuilder, contentDispositionObj);
            }
        }
        return StringUtils.fromString(dispositionBuilder.toString());
    }
}
