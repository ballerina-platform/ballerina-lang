/*
 *   Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;

/**
 * Return character data of an xml value as a string.
 *
 * @since 2.0
 */
public final class Data {
    private static final BString empty = StringUtils.fromString("");

    private Data() {
    }

    public static BString data(BXml xmlValue) {
        if (xmlValue.isEmpty()
                || IsComment.isComment(xmlValue)
                || IsProcessingInstruction.isProcessingInstruction(xmlValue)) {
            return empty;
        }

        return StringUtils.fromString(xmlValue.getTextValue());
    }
}
