/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.bre;

import org.ballerinalang.model.NodeLocation;

/**
 * Bean class to hold meta information of a node in the control stack (i.e: a function/action/resource/service),
 * including name, type, package and the location in the ballerina source (see {@link NodeLocation}).
 *
 * @since 0.8.0
 */
public class CallableUnitInfo {
    private String unitName;
    private String unitPackage;
    private NodeLocation location;

    /**
     * Creates a {@link CallableUnitInfo}.
     *
     * @param unitName     Identifier of the CallableUnit
     * @param unitPackage  Package of the CallableUnit
     * @param location Location in the ballerina source
     */
    public CallableUnitInfo(String unitName, String unitPackage, NodeLocation location) {
        this.unitName = unitName;
        this.unitPackage = unitPackage;
        this.location = location;
    }

    /**
     * Get the name of this CallableUnit.
     *
     * @return Name of this CallableUnit
     */
    public String getName() {
        return this.unitName;
    }

    /**
     * Get the package of this CallableUnit.
     *
     * @return Package of this CallableUnit
     */
    public String getPackage() {
        return this.unitPackage;
    }

    /**
     * Set the package of this CallableUnit.
     *
     * @param packageName Package of this CallableUnit
     */
    public void setPackage(String packageName) {
        this.unitPackage = packageName;
    }

    /**
     * Get the location of this CallableUnit in the ballerina source file.
     *
     * @return Location of this CallableUnit in the ballerina source
     */
    public NodeLocation getNodeLocation() {
        return location;
    }
}
