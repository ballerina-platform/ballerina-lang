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

import _ from 'lodash';

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
                defaultValue = 0;
                break;
            case 'boolean':
                defaultValue = false;
                break;
            case 'map':
                defaultValue = null;
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
    static getConnectorParameters(environment, pkgAlias, connectorIdentifier) {
        const connectorParameters = [];
        for (const packageDefintion of environment.getPackages()) {
            if (environment.getPackageByIdentifier(pkgAlias)) {
                const fullPackageName = environment.getPackageByIdentifier(pkgAlias).getName();
                if (packageDefintion.getName() === fullPackageName) {
                    for (const connector of packageDefintion.getConnectors()) {
                        // Get Connection Properties
                        const pkgName = (packageDefintion.getName() === 'Current Package')
                            ? '' : packageDefintion.getName();
                        const identifier = _.last(_.split(pkgName, '.')) + ':' + connector.getName();

                        if (connectorIdentifier === identifier) {
                            connector.getParams().map((parameter) => {
                                let paramType = parameter.type;
                                let fields = [];
                                let paramName = parameter.name;
                                if (parameter.type.startsWith(fullPackageName)) {
                                    paramName = parameter.type.split(':')[1];
                                    // Check if the param is of struct type
                                    paramType = this.getTypeOfParam(paramName, packageDefintion.getStructDefinitions(),
                                      packageDefintion.getEnums());
                                    if (paramType === 'struct') {
                                        fields = this.getStructDataFields(fullPackageName,
                                    packageDefintion.getStructDefinitions(), packageDefintion.getEnums(),
                                          paramName, fields);
                                    } else if (paramType === 'enum') {
                                        fields = this.getEnumeratorValues(paramName, packageDefintion.getEnums());
                                    }
                                }

                                let propertyConnectorParams;
                                if (parameter.isConnector) {
                                    const cIdentifier = parameter.pkgAlias + ':' + parameter.type;
                                    propertyConnectorParams = this.getConnectorParameters(environment,
                                      parameter.pkgAlias, cIdentifier);
                                }
                              // Check the bType of each attribute and set the default values accordingly
                                const keyValuePair = {
                                    identifier: parameter.name,
                                    bType: paramType,
                                    desc: parameter.name,
                                    fields,
                                    isConnector: parameter.isConnector,
                                    connectorParams: propertyConnectorParams,
                                    pkgAlias: parameter.pkgAlias,
                                    value: this.getDefaultValuesAccordingToBType(parameter.type),
                                    defaultValue: this.getDefaultValuesAccordingToBType(parameter.type),
                                    variableName: paramName,
                                };
                                connectorParameters.push(keyValuePair);
                            });
                        }
                    }
                    break;
                }
            }
        }
        return connectorParameters;
    }

    static getStructDataFields(fullPackageName, structDefinitions, enums, structName, structFields) {
        for (const structDef of structDefinitions) {
            if (structDef.getName() === structName) {
                structDef.getFields().map((field) => {
                    let paramType = field.getType();
                    let fieldArray = [];
                    let variableName = field.getName();
                    if (field.getType().startsWith(fullPackageName)) {
                        variableName = field.getType().split(':')[1];
                      // Check if the param is of struct type
                        paramType = this.getTypeOfParam(variableName, structDefinitions, enums);
                        if (paramType === 'struct') {
                            field.setDefaultValue(this.getDefaultValuesAccordingToBType(field.type));
                            fieldArray = (this.getStructDataFields(fullPackageName, structDefinitions, enums,
                              variableName, fieldArray));
                        } else if (paramType === 'enum') {
                            fieldArray = this.getEnumeratorValues(variableName, enums);
                        }
                    } else if (field.getDefaultValue() === undefined) {
                        field.setDefaultValue(field.defaultValue ? field.defaultValue :
                          this.getDefaultValuesAccordingToBType(field.getType()));
                    }
                    const keyValuePair = {
                        identifier: field.getName(),
                        bType: paramType,
                        desc: field.getName(),
                        fields: fieldArray,
                        value: field.getDefaultValue(),
                        defaultValue: field.getDefaultValue(),
                        variableName,
                    };
                    structFields.push(keyValuePair);
                });
            }
        }
        return structFields;
    }

    static getTypeOfParam(identifier, structs, enums) {
        let type;
        // Iterate over the structs
        structs.forEach((struct) => {
            if (identifier === struct.getName()) {
                type = 'struct';
            }
        });
        // Iterate over the enums
        enums.forEach((enumerator) => {
            if (identifier === enumerator.getName()) {
                type = 'enum';
            }
        });
        return type;
    }

    static getEnumeratorValues(paramName, enums) {
        const fieldArray = [];
        // Iterate over the enums
        enums.forEach((enumNode) => {
            if (paramName === enumNode.getName()) {
                enumNode.getEnumerators().forEach((enumerator) => {
                    fieldArray.push(enumerator.getName());
                });
            }
        });
        return fieldArray;
    }
}
export default ConnectorHelper;
