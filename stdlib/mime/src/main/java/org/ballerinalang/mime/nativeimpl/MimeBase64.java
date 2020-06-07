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

import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.stdlib.io.utils.Utils;

import static org.ballerinalang.mime.util.MimeConstants.UTF_8;

/**
 * Utilities related to MIME base64.
 *
 * @since 1.1.0
 */
public class MimeBase64 {

    public static Object base64Decode(Object contentToBeDecoded, BString charset) {
        return Utils.decode(contentToBeDecoded, charset != null ? charset.getValue() : UTF_8, true);
    }

    public static Object base64Encode(Object contentToBeDecoded, BString charset) {
        return Utils.encode(contentToBeDecoded, charset != null ? charset.getValue() : UTF_8, true);
    }
}
