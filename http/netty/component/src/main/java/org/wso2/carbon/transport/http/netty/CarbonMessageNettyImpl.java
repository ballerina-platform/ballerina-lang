/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.transport.http.netty;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Pipe;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a  common message for all the transports and message processors. Canonical Format for represent
 * abstract message.
 */
public class CarbonMessageNettyImpl extends CarbonMessage {
    public static final int REQUEST = 0;
    public static final int RESPONSE = 1;

    private int port;
    private String protocol;
    private String host;
    private String to;
    private Pipe pipe;

    private CarbonCallback carbonCallback;

    private int direction;

    private Map<String, Object> properties = new HashMap<>();

    private final ReentrantLock lock = new ReentrantLock();

    public Pipe getPipe() {
        return pipe;
    }

    public void setPipe(Pipe pipe) {
        this.pipe = pipe;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getURI() {
        return to;
    }

    public void setURI(String to) {
        this.to = to;
    }


    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public String getProtocol() {
        return protocol;
    }


    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        if (properties != null) {
            return properties.get(key);
        } else {
            return null;
        }
    }

    public CarbonCallback getCarbonCallback() {
        return carbonCallback;
    }

    public void setCarbonCallback(CarbonCallback carbonCallback) {
        this.carbonCallback = carbonCallback;
    }

    public Lock getLock() {
        return lock;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    Map<String, String> getHeaders() {
        return (Map<String, String>) getProperty("TRANSPORT_HEADERS");
    }

    Object getPropertie(String key) {
        return (String) properties.get(key);
    }

}
