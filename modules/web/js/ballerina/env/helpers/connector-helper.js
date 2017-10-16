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
    constructor() {
        this.propertyArray = [];
    }
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
                defaultValue = 0;
                break;
            case 'boolean':
                defaultValue = false;
                break;
            case 'map':
                defaultValue = '{}';
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
    static getConnectorParameters(environment, pkgAlias) {
        const connectorParameters = [];
        for (const packageDefintion of environment.getPackages()) {
            if (environment.getPackageByIdentifier(pkgAlias)) {
                const fullPackageName = environment.getPackageByIdentifier(pkgAlias).getName();
                if (packageDefintion.getName() === fullPackageName) {
                    for (const connector of packageDefintion.getConnectors()) {
                        // Get Connection Properties
                        connector.getParams().map((parameter) => {
                            // let structFields = [];
                            /* if (parameter.type.startsWith(fullPackageName)) {
                                const structName = parameter.type.split(':')[1];
                                structFields = this.getStructDataFields(fullPackageName,
                                    packageDefintion.getStructDefinitions(), structName, structFields);
                            }*/
                            let structFields = null;
                            if (parameter.type.startsWith(fullPackageName)) {
                                const structName = parameter.type.split(':')[1];
                                for (const structDef of packageDefintion.getStructDefinitions()) {
                                    if (structDef.getName() === structName) {
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
        }
        return connectorParameters;
    }

    /* static getStructDataFields(fullPackageName, structDefinitions, structName, structFields) {
        for (const structDef of structDefinitions) {
            if (structDef.getName() === structName) {
                // Iterate over the struct fields to get their default values
                // structFields = structDef.getFields();
                structDef.getFields().map((field) => {
                    let fieldArray = null;
                    if (field.getType().startsWith(fullPackageName)) {
                        const innerStructName = field.getType().split(':')[1];
                        fieldArray = (this.getStructDataFields(fullPackageName, structDefinitions,
                            innerStructName, fieldArray));
                    } else if (field.getDefaultValue() === undefined) {
                        field.setDefaultValue(this
                            .getDefaultValuesAccordingToBType(field.getType()));
                    }
                    const keyValuePair = {
                        identifier: field.getName(),
                        bType: field.getType(),
                        desc: field.getName(),
                        fields: fieldArray,
                        value: this.getDefaultValuesAccordingToBType(field.getType()),
                    };
                    structFields.push(keyValuePair);
                });
            }
        }
        return structFields;
    }*/

}
export default ConnectorHelper;
