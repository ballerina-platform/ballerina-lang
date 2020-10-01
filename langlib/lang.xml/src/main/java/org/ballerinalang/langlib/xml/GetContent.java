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

import org.ballerinalang.jvm.XMLValueUtil;
import org.ballerinalang.jvm.api.BStringUtils;
import org.ballerinalang.jvm.api.values.BString;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.XMLValue;

/**
 * Returns the content of a text or processing instruction or comment item.
 *
 * @since 0.90
 */
public class GetContent {

    public static BString getContent(Object xmlVal) {
        XMLValue value = (XMLValue) xmlVal;
        if (IsText.isText(value)) {
            return BStringUtils.fromString(value.getTextValue());
        } else if (IsProcessingInstruction.isProcessingInstruction(value)) {
            return BStringUtils.fromString(XMLValueUtil.getPIContent(value));
        } else if (IsComment.isComment(value)) {
            return BStringUtils.fromString(XMLValueUtil.getCommentContent(value));
        }
        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.XML_FUNC_TYPE_ERROR, "getContent",
                                                       "text|processing instruction|comment");
    }
}
