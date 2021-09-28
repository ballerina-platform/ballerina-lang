/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.diagramutil.connector.models.connector;

import org.ballerinalang.central.client.model.connector.BalConnector;
import org.ballerinalang.central.client.model.connector.BalFunction;

import java.util.List;
import java.util.Map;

/**
 * Connector model.
 */
public class Connector extends BalConnector {

    public Connector(String orgName, String moduleName, String packageName, String version, String name,
                     String documentation, Map<String, String> displayAnnotation, List<BalFunction> functions) {
        super(orgName, moduleName, packageName, version, name, documentation, displayAnnotation, functions);
    }
}
