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

package org.ballerinalang.net.uri.parser;

import org.ballerinalang.net.uri.URITemplateException;
import org.ballerinalang.net.uri.URIUtil;

import java.util.Map;

/**
 * DotSuffixExpression represents path segments that have string suffix concatenated by dot.
 * ex - /{foo}.bar/
 *
 * @param <DataType> Type of data which should be stored in the node.
 * @param <InboundMsgType> Inbound message type for additional checks.
 */
public class DotSuffixExpression<DataType, InboundMsgType> extends SimpleStringExpression<DataType, InboundMsgType> {

    public DotSuffixExpression(DataElement<DataType, InboundMsgType> dataElement, String token)
            throws URITemplateException {
        super(dataElement, token);
    }

    @Override
    int match(String uriFragment, Map<String, String> variables) {
        String pathSegment = uriFragment;
        if (uriFragment.contains(URIUtil.URI_PATH_DELIMITER)) {
            pathSegment = uriFragment.substring(0, uriFragment.indexOf(URIUtil.URI_PATH_DELIMITER));
        }
        String[] subSegments = pathSegment.split("\\.");

        int endCharacterCount = subSegments.length - 1;
        int dotSegmentCounter = 0;
        int length = uriFragment.length();

        for (int i = 0; i < length; i++) {
            char ch = uriFragment.charAt(i);
            if (isEndCharacter(ch)) {
                dotSegmentCounter++;
                if (dotSegmentCounter != endCharacterCount) {
                    continue;
                }

                if (!setVariables(uriFragment.substring(0, i), variables)) {
                    return -1;
                }
                return i;
            } else if (i == length - 1) {
                if (!setVariables(uriFragment, variables)) {
                    return -1;
                }
                return length;
            }
        }
        return 0;
    }

    protected boolean isEndCharacter(Character endCharacter) {
        for (Node childNode : childNodesList) {
            if (endCharacter == childNode.getFirstCharacter() && endCharacter == URIUtil.DOT_SEGMENT) {
                return true;
            }
        }
        return false;
    }
}
