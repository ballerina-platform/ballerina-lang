/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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

import java.util.List;

/**
 * SimpleStringExpression represents path segments that have single path param.
 * ex - /{foo}/
 *
 * @param <NODE_ITEM> Specific node item created by the user.
 */
public class SimpleStringExpression<NODE_ITEM extends NodeItem> extends SimpleSplitStringExpression<NODE_ITEM> {

    public SimpleStringExpression(NODE_ITEM nodeItem, String token) throws URITemplateException {
        super(nodeItem, token);
    }

    @Override
    protected boolean isEndCharacter(List<? extends Node> childNodesList, Character endCharacter) {
        return endCharacter == '/';
    }
}
