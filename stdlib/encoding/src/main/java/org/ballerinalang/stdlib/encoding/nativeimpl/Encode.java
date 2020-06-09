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
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.stdlib.encoding.EncodingUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.ballerinalang.stdlib.encoding.Constants.ENCODING_ERROR;

/**
 * Extern functions of ballerina encoding.
 *of
 * @since 0.991.0
 */
public class Encode {

    public static BString encodeBase64Url(ArrayValue input) {
        byte[] encodedValue = Base64.getUrlEncoder().withoutPadding().encode(input.getBytes());
        return StringUtils.fromString(new String(encodedValue, StandardCharsets.ISO_8859_1));
    }

    public static Object encodeUriComponent(BString url, BString charset) {
        try {
            String encoded = URLEncoder.encode(url.getValue(), charset.getValue());
            StringBuilder buf = new StringBuilder(encoded.length());
            char focus;
            for (int i = 0; i < encoded.length(); i++) {
                focus = encoded.charAt(i);
                if (focus == '*') {
                    buf.append("%2A");
                } else if (focus == '+') {
                    buf.append("%20");
                } else if (focus == '%' && (i + 1) < encoded.length() && encoded.charAt(i + 1) == '7'
                        && encoded.charAt(i + 2) == 'E') {
                    buf.append('~');
                    i += 2;
                } else {
                    buf.append(focus);
                }

            }
            return StringUtils.fromString(buf.toString());
        } catch (Throwable e) {
            return EncodingUtil
                    .createError("Error occurred while encoding the URI component. " + e.getMessage(), ENCODING_ERROR);
        }
    }
}
