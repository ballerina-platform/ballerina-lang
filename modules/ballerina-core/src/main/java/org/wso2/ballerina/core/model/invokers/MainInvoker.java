/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.model.invokers;

import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Node;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.NodeVisitor;

/**
 * {@code MainInvoker} is responsible for invoking the Main function.
 */
public class MainInvoker implements Node {

    private BallerinaFunction mainFunction;

    public MainInvoker(BallerinaFunction mainFunction) {
        this.mainFunction = mainFunction;
    }

    public BallerinaFunction getMainFunction() {
        return mainFunction;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeLocation getNodeLocation() {
        return null;
    }
}
