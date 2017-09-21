/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 * A helper class relations ballerina environment connectors.
 *
 * @class ConnectorHelper
 */
class ConnectorHelper {
    /**
     * Get the default values of the connector parameters according to the bType
     * @param bType
     * @returns {*}
     */
    static getDefaultValuesAccordingToBType(bType) {
        let defaultValue;
        switch (bType) {
            case 'string':
                defaultValue = '';
                break;
            case 'int':
                defaultValue = -1;
                break;
            case 'boolean':
                defaultValue = false;
                break;
            case 'array':
                defaultValue = '[]';
                break;
            default:
                defaultValue = '';
        }
        return defaultValue;
    }

    /**
     * Gets the connector parameters of a connector
     * @param environment The ballerina environment.
     * @param fullPackageName of the connector
     * @returns {Array}
     */
    static getConnectorParameters(environment, fullPackageName) {
        const connectorParameters = [];
        for (const packageDefintion of environment.getPackages()) {
            if (packageDefintion.getName() === fullPackageName) {
                for (const connector of packageDefintion.getConnectors()) {
                    // Get Connection Properties
                    connector.getParams().map((parameter) => {
                        let structFields = null;
                        if (parameter.type === 'ConnectionProperties') {
                            for (const structDef of packageDefintion.getStructDefinitions()) {
                                if (structDef.getName() === parameter.type) {
                                    // Iterate over the struct fields to get their default values
                                    structFields = structDef.getFields();
                                    structFields.map((field) => {
                                        if (field.getDefaultValue() === undefined) {
                                            field.setDefaultValue(this
                                                .getDefaultValuesAccordingToBType(field.getType()));
                                        }
                                    });
                                }
                            }
                        }

                        // Check the bType of each attribute and set the default values accordingly
                        const keyValuePair = {
                            identifier: parameter.name,
                            bType: parameter.type,
                            desc: parameter.name,
                            fields: structFields,
                            value: this.getDefaultValuesAccordingToBType(parameter.type),
                        };
                        connectorParameters.push(keyValuePair);
                    });
                }
                break;
            }
        }
        return connectorParameters;
    }

}

export default ConnectorHelper;
