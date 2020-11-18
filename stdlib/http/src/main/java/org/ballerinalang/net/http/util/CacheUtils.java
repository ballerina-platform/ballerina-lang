/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.util;

import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.Types;
import io.ballerina.runtime.api.values.BString;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

/**
 * A utility class for HTTP caching related tasks.
 *
 * @since 0.965.0
 */
public class CacheUtils {

    public static final String WEAK_VALIDATOR_TAG = "W/";
    public static final String ETAG_HEADER = "ETag";
    public static final String IF_NONE_MATCH_HEADER = "If-None-Match";
    public static final String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";
    public static final String LAST_MODIFIED_HEADER = "Last-Modified";

    /**
     * Method for revalidating a cached response. This method follows the RFC7232 and RFC7234 specifications.
     *
     * @param outboundResponse The response to be sent to downstream
     * @param inboundRequest   The request received from downstream
     * @return Returns true if the cached response is still valid
     */
    public static boolean isValidCachedResponse(HttpCarbonMessage outboundResponse, HttpCarbonMessage inboundRequest) {
        String outgoingETag = outboundResponse.getHeader(ETAG_HEADER);
        String incomingETags = inboundRequest.getHeader(IF_NONE_MATCH_HEADER);

        if (incomingETags != null) {
            if (outgoingETag == null) {
                // If inbound request has the If-None-Match header, but the outgoing request does not have an ETag
                // header, it is considered that the cached response is invalid.
                return false;
            }

            return !isNonMatchingETag(incomingETags, outgoingETag);
        }

        // If there isn't an If-None-Match header, then check if there is a If-Modified-Since header.
        String ifModifiedSince = inboundRequest.getHeader(IF_MODIFIED_SINCE_HEADER);
        if (ifModifiedSince == null) {
            // If both If-None-Match and If-Modified-Since headers aren't there, then it is not looking for cache
            // revalidation.
            return false;
        }

        String lastModified = outboundResponse.getHeader(LAST_MODIFIED_HEADER);
        if (lastModified == null) {
            return false;
        }

        try {
            TemporalAccessor ifModifiedSinceTime = ZonedDateTime.parse(ifModifiedSince,
                                                                       DateTimeFormatter.RFC_1123_DATE_TIME);
            TemporalAccessor lastModifiedTime = ZonedDateTime.parse(lastModified, DateTimeFormatter.RFC_1123_DATE_TIME);
            return ifModifiedSinceTime.equals(lastModifiedTime);
        } catch (DateTimeParseException e) {
            // If the Date header cannot be parsed, it is ignored.
            return false;
        }
    }

    private static boolean isNonMatchingETag(String etags, String outgoingETag) {
        String[] etagArray = etags.split(",");

        if (etagArray.length == 1 && "*".equals(etagArray[0])) {
            return false;
        }

        for (String etag : etagArray) {
            if (weakEquals(etag.trim(), outgoingETag)) {
                return false;
            }
        }

        return true;
    }

    private static boolean weakEquals(String requestETag, String responseETag) {
        // Taking the sub string to ignore "W/"
        String requestTagPortion = isWeakEntityTag(requestETag) ?
                requestETag.substring(WEAK_VALIDATOR_TAG.length()) : requestETag;
        String responseTagPortion = isWeakEntityTag(responseETag) ?
                responseETag.substring(WEAK_VALIDATOR_TAG.length()) : responseETag;

        return requestTagPortion.equals(responseTagPortion);
    }

    private static boolean isWeakEntityTag(String etag) {
        return etag.startsWith(WEAK_VALIDATOR_TAG);
    }

    public static BString getProperty(BString name) {
        String value = System.getProperty(name.getValue());
        if (value == null) {
            return Types.TYPE_STRING.getZeroValue();
        }
        return BStringUtils.fromString(value);
    }

    private CacheUtils() {
    }
}
