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

package org.ballerinalang.net.ws;

import org.ballerinalang.net.uri.parser.Node;
import org.ballerinalang.net.uri.parser.NodeCreator;
import org.ballerinalang.net.uri.parser.PathSegment;

/**
 * Node creator for WebSocket Service.
 */
public class WsNodeCreator implements NodeCreator<WsNodeItem> {

    @Override
    public Node<WsNodeItem> createNode(PathSegment expression) {
        WsNodeItem nodeItem = new WsNodeItem();
        return new Node<>(nodeItem, expression);
    }
}
