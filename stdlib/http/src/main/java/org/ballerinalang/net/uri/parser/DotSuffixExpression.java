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

    protected boolean isEndCharacter(Character endCharacter) {
        for (Node childNode : childNodesList) {
            if (endCharacter == childNode.getFirstCharacter() && endCharacter == URIUtil.DOT_SEGMENT) {
                return true;
            }
        }
        return false;
    }
}
