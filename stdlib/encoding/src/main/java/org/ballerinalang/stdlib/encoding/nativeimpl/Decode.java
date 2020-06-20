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

package org.ballerinalang.stdlib.encoding.nativeimpl;

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.stdlib.encoding.EncodingUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Base64;

import static org.ballerinalang.stdlib.encoding.Constants.DECODING_ERROR;

/**
 * Extern functions of ballerina decoding.
 *of
 * @since 0.991.0
 */
public class Decode {

    public static Object decodeBase64Url(BString input) {
        try {
            byte[] output = Base64.getUrlDecoder().decode(input.getValue());
            return BValueCreator.createArrayValue(output);
        } catch (IllegalArgumentException e) {
            return EncodingUtil.createError("Input is not a valid Base64 URL encoded value", DECODING_ERROR);
        }
    }

    public static Object decodeUriComponent(BString url, BString charset) {
        try {
            return StringUtils.fromString(URLDecoder.decode(url.getValue(), charset.getValue()));
        } catch (UnsupportedEncodingException e) {
            return EncodingUtil
                    .createError("Error occurred while decoding the URI component. " + e.getMessage(), DECODING_ERROR);
        }
    }
}
