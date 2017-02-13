/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.interpreter;

import org.wso2.ballerina.core.interpreter.nonblocking.BLangExecutionVisitor;
import org.wso2.ballerina.core.model.Node;
import org.wso2.ballerina.core.model.NodeExecutor;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * {@code ConnectorVarLocation} represents a location where a variable declared in a
 * {@link org.wso2.ballerina.core.model.Connector }  is stored at runtime.
 *
 * @since 0.8.0
 */
public class ConnectorVarLocation extends MemoryLocation implements Node {
    private int connectorMemAddrOffset;

    public ConnectorVarLocation(int connectorMemAddrOffset) {
        this.connectorMemAddrOffset = connectorMemAddrOffset;
    }

    public int getConnectorMemAddrOffset() {
        return connectorMemAddrOffset;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public BValue execute(NodeExecutor executor) {
        return executor.visit(this);
    }

    @Override
    public BValue access(BLangExecutionVisitor executor) {
        return executor.access(this);
    }
}
