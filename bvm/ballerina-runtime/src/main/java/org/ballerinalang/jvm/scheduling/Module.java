/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.scheduling;

/**
 * This contains the module info at runtime.
 *
 * @since 2.0.0
 */
public class Module {

    /**
     * Organization name of module.
     */
    private final String orgName;

    /**
     * Name of module.
     */
    private final String name;

    /**
     * Version of module.
     */
    private final String version;

    public Module(String OrgName, String name, String version) {
        this.orgName = OrgName;
        this.name = name;
        this.version = version;
    }

    /**
     * Gets the module org name.
     *
     * @return Module org name.
     */
    public String getOrgName() {
        return orgName;
    }

    /**
     * Gets the module name.
     *
     * @return Module name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the module version.
     *
     * @return Module version.
     */
    public String getVersion() {
        return version;
    }
}
