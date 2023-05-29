/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langlib.xml;

import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.values.XmlComment;
import io.ballerina.runtime.internal.values.XmlPi;
import io.ballerina.runtime.internal.values.XmlSequence;

/**
 * Returns the content of a text or processing instruction or comment item.
 *
 * @since 0.90
 */
public class GetContent {

    public static BString getContent(Object xmlVal) {
        BXml value = (BXml) xmlVal;
        if (IsProcessingInstruction.isProcessingInstruction(value)) {
            return getPIContent(value);
        } else if (IsComment.isComment(value)) {
            return getCommentContent(value);
        }
        throw ErrorHelper.getRuntimeException(ErrorCodes.XML_FUNC_TYPE_ERROR, "getContent",
                                                       "processing instruction|comment");
    }

    private static BString getCommentContent(BXml value) {
        if (value.getNodeType() == XmlNodeType.COMMENT) {
            return StringUtils.fromString(((XmlComment) value).getTextValue());
        }
        return getCommentContent(((XmlSequence) value).getItem(0));
    }

    private static BString getPIContent(BXml value) {
        if (value.getNodeType() == XmlNodeType.PI) {
            return StringUtils.fromString(((XmlPi) value).getData());
        }
        return getPIContent(((XmlSequence) value).getItem(0));
    }
}
