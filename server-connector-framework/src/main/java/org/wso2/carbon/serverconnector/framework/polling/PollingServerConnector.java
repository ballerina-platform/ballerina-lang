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
package org.wso2.carbon.serverconnector.framework.polling;

import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;

import java.util.Map;

/**
 * Abstract class for the polling type of server connectors.
 */
public abstract class PollingServerConnector extends ServerConnector {
    private static final String POLLING_INTERVAL = "pollingInterval";
    private Map<String, String> parameters;
    private long interval = 1000L;  //default polling interval
    private PollingTaskRunner pollingTaskRunner;

    public PollingServerConnector(String id) {
        super(id);
    }

    /**
     * The start polling method which should be called when starting the polling with given interval.
     * @param parameters parameters passed from starting this polling connector.
     */
    @Override
    public void start(Map<String, String> parameters) throws ServerConnectorException {
        this.parameters = parameters;
        String pollingInterval = parameters.get(POLLING_INTERVAL);
        if (pollingInterval != null) {
            this.interval = Long.parseLong(pollingInterval);
        }
        pollingTaskRunner = new PollingTaskRunner(this);
        pollingTaskRunner.start();
    }

    @Override
    public void stop() throws ServerConnectorException {
        if (pollingTaskRunner != null) {
            pollingTaskRunner.terminate();
        }
    }


    /**
     * Generic polling method which will be invoked with each polling invocation.
     */
    public abstract void poll();


    public long getInterval() {
        return interval;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}
