/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.context;

/**
 * Class which manage server agent related details, such as agent port etc.
 *
 * @since 0.982.0
 */
public class AgentManager {

    private static final String BALLERINA_AGENT_PORT_START_VALUE = "ballerina.agent.port.start.value";

    private static final String DEFAULT_AGENT_PORT_START = "7000";

    private static AgentManager agentManager;

    private int agentPort;

    private AgentManager() {
        agentPort = Integer.parseInt(System.getProperty(BALLERINA_AGENT_PORT_START_VALUE, DEFAULT_AGENT_PORT_START));
    }

    public static AgentManager getInstance() {
        if (agentManager != null) {
            return agentManager;
        }
        return createInstance();
    }

    private static synchronized AgentManager createInstance() {
        if (agentManager != null) {
            return agentManager;
        }
        agentManager = new AgentManager();
        return agentManager;
    }

    synchronized int getNextPort() {
        return agentPort++;
    }
}
