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
package org.ballerinalang.toml.model;

/**
 * Defines the settings object which is created using the toml file configs.
 *
 * @since 0.964
 */
public class Settings {
    private Proxy proxy;
    private Central central;

    /**
     * Get the proxy configuration object.
     *
     * @return proxy config object
     */
    public Proxy getProxy() {
        return proxy;
    }

    /**
     * Set the proxy configuration object.
     *
     * @param proxy proxy config object
     */
    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    /**
     * Get the central configuration object.
     *
     * @return central config object
     */
    public Central getCentral() {
        return central;
    }

    /**
     * Set the central config object.
     *
     * @param central central config object
     */
    public void setCentral(Central central) {
        this.central = central;
    }


}
