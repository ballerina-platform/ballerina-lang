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

import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import ConnectorHelper from './../../../../../env/helpers/connector-helper';
import './properties-form.css';
import PropertyWindow from './property-window';
import TreeUtils from './../../../../../model/tree-util';
import FragmentUtils from './../../../../../utils/fragment-utils';
import TreeBuilder from './../../../../../model/tree-builder';
/**
 * React component for a connector prop window
 *
 * @class ConnectorPropertiesForm
 * @extends {React.Component}
 */
class ConnectorPropertiesForm extends React.Component {

    constructor(props) {
        super(props);
        this.getSupportedProps = this.getSupportedProps.bind(this);
        this.getDataAddedToConnectorInit = this.getDataAddedToConnectorInit.bind(this);
        this.setDataToConnectorInitArgs = this.setDataToConnectorInitArgs.bind(this);
        this.getConnectorInstanceString = this.getConnectorInstanceString.bind(this);
        this.getAddedValueOfProp = this.getAddedValueOfProp.bind(this);
        this.addQuotationForStringValues = this.addQuotationForStringValues.bind(this);
        this.getStringifiedMap = this.getStringifiedMap.bind(this);
        this.getValueOfStructs = this.getValueOfStructs.bind(this);
        this.isVarDefInit = false;
        this.variableDefInitReference;
    }

    /**
     * Get already added values to properties
     * @param node
     * @returns {string}
     */
    getAddedValueOfProp(node) {
        let value = '';
        if (TreeUtils.isLiteral(node)) { // If its a direct value
            value = node.getValue();
        } else if (TreeUtils.isSimpleVariableRef(node)) { // If its a reference variable
            value = node.getVariableName().value;
        }
        return value;
    }

    /**
     * Get the connector properties supported by each connector
     * @returns {*}
     */
    getSupportedProps() {
        const environment = this.context.environment;
        const props = this.props.model.props;
        // Get the pkg alias
        const initialExpr = TreeUtils.getConnectorInitFromStatement(props.model);
        let pkgAlias;
        let connectorName;
        this.variableDefInitReference = {
            identifier: 'varDefInitRef',
            bType: 'string',
            desc: 'varDefInitRef',
            isConnector: false,
            value: _.get(initialExpr, 'variableName.value') || '',
            defaultValue: '',
        };
        if (!initialExpr || TreeUtils.isSimpleVariableRef(initialExpr)) {
            const constraint = _.get(props.model, 'variable.typeNode.constraint');
            pkgAlias = _.get(constraint, 'packageAlias.value') || '';
            connectorName = _.get(constraint, 'typeName.value') || '';
            this.isVarDefInit = true;
        } else if (TreeUtils.isConnectorInitExpr(initialExpr)) {
            pkgAlias = initialExpr.connectorType.packageAlias.value || '';
            connectorName = initialExpr.connectorType.typeName.value;
            this.isVarDefInit = false;
        } else if (TreeUtils.isInvocation(initialExpr)) {
            const functionName = initialExpr.getName().value;
            if (!initialExpr.packageAlias.value || !initialExpr.name.value) {
                if (environment.getCurrentPackage()) {
                    for (const functionDefinition of environment.getCurrentPackage().getFunctionDefinitions()) {
                        if (functionDefinition.getName() === functionName) {
                            const returnParams = functionDefinition.getReturnParams();
                            returnParams.forEach((parameter) => {
                                if (parameter.isConnector) {
                                    pkgAlias = parameter.pkgAlias;
                                    const types = parameter.type.split(':');
                                    if (types.length > 1) {
                                        connectorName = types[types.length - 1];
                                    }
                                }
                            });
                        }
                    }
                }
            } else {
                pkgAlias = initialExpr.packageAlias.value || '';
                connectorName = initialExpr.name.value;
            }
        } else {
            console.warn('Invalid init expression found: ' + initialExpr);
        }
        const connectorIdentifier = `${pkgAlias}:${connectorName}`;
        const connectorProps = ConnectorHelper.getConnectorParameters(environment, pkgAlias, connectorIdentifier);
        const addedValues = this.getDataAddedToConnectorInit();
        connectorProps.map((property, index) => {
            if (addedValues.length > 0) {
                // For simple fields -> Literal node or a simple variable reference
                if (TreeUtils.isSimpleVariableRef(addedValues[index]) || TreeUtils.isLiteral(addedValues[index])) {
                    property.value = this.getAddedValueOfProp(addedValues[index]);
                } else if (TreeUtils.isRecordLiteralExpr(addedValues[index])) { // For structs
                    this.getValueOfStructs(addedValues[index], property.fields);
                    property.value = this.getStringifiedMap(property.fields);
                } else if (TreeUtils.isFieldBasedAccessExpr(addedValues[index])) {
                    property.value = addedValues[index].getFieldName().value;
                } else if (TreeUtils.isConnectorInitExpr(addedValues[index])) {
                    property.value = addedValues[index].getSource();
                }
            }
        });
        return connectorProps;
    }

    /**
     * Get value of structs
     */
    getValueOfStructs(addedValues, fields) {
        addedValues.getKeyValuePairs().forEach((element) => {
            if (TreeUtils.isRecordLiteralKeyValue(element)) {
                const key = element.getKey().getVariableName().value ||
                    element.getKey().value;
                // If the value is a Literal Node
                if (TreeUtils.isLiteral(element.getValue())
                    || TreeUtils.isSimpleVariableRef(element.getValue())) {
                    const obj = _.find(fields, { identifier: key });
                    obj.value = (this.getAddedValueOfProp(element.getValue()));
                } else if (TreeUtils.isRecordLiteralExpr(element.getValue())) {
                    const propName = _.find(fields, { identifier: key });
                    this.getValueOfStructs(element.getValue(), propName.fields);
                    propName.value = this.getStringifiedMap(propName.fields);
                }
            }
        });
    }

    /**
     * Add quotation for strings
     */
    addQuotationForStringValues(value) {
        if (!value.startsWith('"')) {
            value = '"' + value + '"';
        }
        return value;
    }

    /**
     * Create the connector init string
     * @param {object} connectorInit - connector init expression
     * @param {object} pkgAlias - package alias
     * @param {object} connectorType - connector type
     * @param {object} data - updated data
     * @param {boolean} setVarRef - whether to set as a variable Reference or not
     */
    getConnectorInstanceString(connectorInit, pkgAlias, connectorType, data, setVarRef) {
        if (connectorInit && TreeUtils.isConnectorInitExpr(connectorInit)) {
            if (setVarRef) {
                if (data.value === '') {
                    delete connectorInit.parent.initialExpression;
                    return;
                }
                const pkgStr = pkgAlias !== 'Current Package' ? `${pkgAlias}:` : '';
                const constraint = `<${pkgStr}${_.get(connectorInit, 'connectorType.typeName.value')}>`;
                const varRefString = data.value !== '' ? (data.value + ';') : '';
                const endpointSource = `endpoint ${constraint} endpoint1 {${varRefString}}`;
                const fragment = FragmentUtils.createEndpointVarDefFragment(endpointSource);
                const parsedJson = FragmentUtils.parseFragment(fragment);
                const nodeForFragment = TreeBuilder.build(parsedJson);
                nodeForFragment.viewState.showOverlayContainer = false;
                connectorInit.parent.setInitialExpression(nodeForFragment.variable.initialExpression, true);
            } else {
                // Set the expressions to null
                connectorInit.setExpressions([], true);
                const varDefNode = this.getConnectorInitVariableDefinition(data, connectorType, pkgAlias);
                connectorInit.setExpressions(varDefNode.getVariable().getInitialExpression().getExpressions());
            }
        } else if (setVarRef) {
            if (connectorInit) {
                if (data.value === '') {
                    delete connectorInit.parent.initialExpression;
                    return;
                }
                connectorInit.variableName.value = data.value;
            } else {
                const pkgStr = (pkgAlias !== 'Current Package') ? (pkgAlias + ':') : '';
                const constraint = `<${pkgStr}${connectorType}>`;
                const endpointSource = `endpoint ${constraint} endpoint1 {${data.value};}`;
                const fragment = FragmentUtils.createEndpointVarDefFragment(endpointSource);
                const parsedJson = FragmentUtils.parseFragment(fragment);
                const nodeForFragment = TreeBuilder.build(parsedJson);
                nodeForFragment.viewState.showOverlayContainer = false;
                _.get(this.props, 'model.props.model.variable')
                        .setInitialExpression(nodeForFragment.getVariable().getInitialExpression(), true);
            }
        } else {
            const pkgStr = pkgAlias !== 'Current Package' ? `${pkgAlias}:` : '';
            const constraint = `<${pkgStr}${connectorType}>`;
            const paramArray = [];
            let map;
            data.forEach((key) => {
                if (key.bType === 'string') {
                    key.value = this.addQuotationForStringValues(key.value);
                }
                paramArray.push(key.value);
            });
            if (map) {
                paramArray.push(map);
            }
            const connectorParamString = paramArray.join(',');
            const connectorInitString = 'create ' +
                    ' ' + (pkgAlias !== '' ? `${pkgAlias}:` : '') + `${connectorType}(${connectorParamString})`;
            const endpointSource = `endpoint ${constraint} endpoint1 {${connectorInitString};}`;
            const fragment = FragmentUtils.createEndpointVarDefFragment(endpointSource);
            const parsedJson = FragmentUtils.parseFragment(fragment);
            const nodeForFragment = TreeBuilder.build(parsedJson);
            nodeForFragment.viewState.showOverlayContainer = false;
            const newInit = nodeForFragment.variable.initialExpression;
            const varDefNode = this.getConnectorInitVariableDefinition(data, connectorType, pkgAlias);
            newInit.setExpressions(varDefNode.getVariable().getInitialExpression().getExpressions());
            if (connectorInit) {
                connectorInit.parent.setInitialExpression(newInit, true);
            } else {
                _.get(this.props, 'model.props.model.variable')
                        .setInitialExpression(varDefNode.getVariable().getInitialExpression(), true);
            }
        }
    }

    getConnectorInitVariableDefinition(data, connectorType, pkgAlias) {
        const paramArray = [];
        let map;
        data.forEach((key) => {
            if (key.bType === 'string') {
                key.value = this.addQuotationForStringValues(key.value);
            }
            if (key.bType === 'enum') {
                let enumValue = 'null';
                if (key.value && key.value !== 'null') {
                    enumValue = pkgAlias + ':' + key.variableName + '.' + key.value;
                }
                paramArray.push(enumValue);
            } else {
                if (!key.value) {
                    key.value = key.defaultValue;
                }
                paramArray.push(key.value);
            }
        });
        if (map) {
            paramArray.push(map);
        }
        const connectorParamString = paramArray.join(',');

        const connectorInitString = (pkgAlias !== '' ? `${pkgAlias}:` : '') + `${connectorType} __endpoint1 = create ` +
            ' ' + (pkgAlias !== '' ? `${pkgAlias}:` : '') + `${connectorType}(${connectorParamString})`;
        const fragment = FragmentUtils.createStatementFragment(`${connectorInitString};`);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        return TreeBuilder.build(parsedJson);
    }

    /**
     * Get the stringified map
     * @param properties
     * @returns {string} stringified map
     */
    getStringifiedMap(properties) {
        const valueArr = [];
        properties.forEach((field) => {
            // Remove quotation marks of the identifier
            if (field.identifier.startsWith('"')) {
                field.identifier = JSON.parse(field.identifier);
            }
            if ((!field.value && field.bType !== 'struct') ||
                (field.value === field.defaultValue && field.bType !== 'struct')) {
                return;
            }
            // If the btype is string, add quotation marks to the value
            if (field.bType === 'string') {
                field.value = this.addQuotationForStringValues(field.value);
            }
            if (field.bType !== 'struct') {
                valueArr.push(field.identifier + ':' + field.value);
            } else if (field.bType === 'struct') {
                if (!field.value || (field.value.startsWith('{') && field.value.endsWith('}'))) {
                    let localMap = this.getStringifiedMap(field.fields);
                    if (localMap !== '{}') {
                        localMap = field.identifier + ': ' + localMap;
                        valueArr.push(localMap);
                    }
                } else {
                    valueArr.push(field.identifier + ':' + field.value);
                }
            }
        });
        const map = '{' + valueArr.join(',') + '}';
        return map;
    }
    /**
     * Set data to the connector init arguments and create the connector init string
     * @param data
     */
    setDataToConnectorInitArgs(data, setVarRef) {
        const props = this.props.model.props;
        const connectorInit = TreeUtils.getConnectorInitFromStatement(props.model);
        let pkgAlias;
        let connectorType;
        if (connectorInit && TreeUtils.isConnectorInitExpr(connectorInit)) {
            pkgAlias = connectorInit.getConnectorType().getPackageAlias().value;
            connectorType = connectorInit.connectorType.typeName.value;
        } else {
            pkgAlias = _.get(props.model, 'variable.typeNode.constraint.packageAlias.value');
            connectorType = _.get(props.model, 'variable.typeNode.constraint.typeName.value');
        }
        this.getConnectorInstanceString(connectorInit, pkgAlias, connectorType, data, setVarRef);
    }

    /**
     * Get the values already added as arguments to the connector init expression
     * @returns {Expression[]}
     */
    getDataAddedToConnectorInit() {
        const props = this.props.model.props;
        const initialExpr = TreeUtils.getConnectorInitFromStatement(props.model);
        if (initialExpr && TreeUtils.isConnectorInitExpr(initialExpr)) {
            return initialExpr.getExpressions();
        } else {
            return [];
        }
    }

    /**
     * Renders the view for a connector properties window
     *
     * @returns {ReactElement} The view.
     * @memberof connector properties window
     */
    render() {
        const props = this.props.model.props;
        const positionX = (props.bBox.x) - 8 + 'px';
        const positionY = (props.bBox.y) + 'px';
        const constraint = _.get(props.model, 'variable.typeNode.constraint');
        const pkgAlias = _.get(constraint, 'packageAlias.value') || '';
        const connectorName = _.get(constraint, 'typeName.value') || '';
        const formH = `${pkgAlias !== '' ? pkgAlias + ':' : ''}${connectorName}`;
        const styles = {
            popover: {
                top: props.bBox.y + 10 + 'px',
                left: positionX,
                minWidth: '500px',
            },
            arrowStyle: {
                top: positionY,
                left: props.bBox.x + 'px',
            },
        };
        const supportedProps = this.getSupportedProps();
        let propertiesExist = true;
        if (!supportedProps.length) {
            propertiesExist = false;
        }

        return (
            <PropertyWindow
                model={props.model}
                formHeading={formH}
                key={`connectorProp/${props.model.id}`}
                styles={styles}
                supportedProps={supportedProps}
                addedValues={this.setDataToConnectorInitArgs}
                propertiesExist={propertiesExist}
                varDefInit={this.isVarDefInit}
                varDefInitRef={this.variableDefInitReference}
            />);
    }
}

export default ConnectorPropertiesForm;

ConnectorPropertiesForm.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
};
