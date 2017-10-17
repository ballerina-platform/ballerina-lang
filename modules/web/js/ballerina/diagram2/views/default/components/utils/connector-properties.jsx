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
        const props = this.props.model.props;
        // Get the pkg alias
        const pkgAlias = props.model.getVariable().getInitialExpression().getConnectorType().getPackageAlias().value;
        const connectorProps = ConnectorHelper.getConnectorParameters(this.context.environment, pkgAlias);
        const addedValues = this.getDataAddedToConnectorInit();
        connectorProps.map((property, index) => {
            if (addedValues.length > 0) {
                if (TreeUtils.isSimpleVariableRef(addedValues[index]) || TreeUtils.isLiteral(addedValues[index])) {
                    property.value = this.getAddedValueOfProp(addedValues[index]);
                } else if (TreeUtils.isRecordLiteralExpr(addedValues[index])) {
                    addedValues[index].getKeyValuePairs().forEach((element) => {
                        if (TreeUtils.isRecordLiteralKeyValue(element)) {
                            const key = element.getKey().getVariableName().value ||
                                element.getKey().value;
                                // If the value is a Literal Node
                            if (TreeUtils.isLiteral(element.getValue())) {
                                const obj = _.find(property.fields, { identifier: key });
                                obj.value = (this.getAddedValueOfProp(element.getValue()));
                            } else if (TreeUtils.isRecordLiteralExpr(element.getValue())) {
                                const propName = _.find(property.fields, { identifier: key });
                                element.getValue().getKeyValuePairs().map((innerElement) => {
                                    if (TreeUtils.isRecordLiteralKeyValue(innerElement)) {
                                        const innerKey = innerElement.getKey().getVariableName().value ||
                                            innerElement.getKey().value;
                                            // If the value is a Literal Node
                                        if (TreeUtils.isLiteral(innerElement.getValue())) {
                                            const obj = _.find(propName.fields, { identifier: innerKey });
                                            obj.value = (this.getAddedValueOfProp(innerElement.getValue()));
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
        return connectorProps;
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
     * @param connectorInit
     * @param data
     * @returns {string}
     */
    getConnectorInstanceString(connectorInit, pkgAlias, data) {
        // Set the expressions to null
        connectorInit.setExpressions([], true);

        let connectorParamString = '';
        const paramArray = [];
        let map;
        data.forEach((key) => {
            if (key.bType === 'string') {
                key.value = this.addQuotationForStringValues(key.value);
            }
            // For simple fields
            if (key.fields.length === 0) {
                paramArray.push(key.value);
            } else if (key.value) {
                paramArray.push(key.value);
            } else {
                map = this.getStringifiedMap(key.fields);
            }
        });
        if (map) {
            paramArray.push(map);
        }
        connectorParamString = paramArray.join(',');
        const connectorInitString = pkgAlias + ':ClientConnector __endpoint1 = ' +
                    'create ' + pkgAlias + ':ClientConnector(' + connectorParamString + ')';
        const fragment = FragmentUtils.createStatementFragment(`${connectorInitString};`);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const varDefNode = TreeBuilder.build(parsedJson);
        connectorInit.setExpressions(varDefNode.getVariable().getInitialExpression().getExpressions());
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
            // If the btype is string, add quotation marks to the value
            if (field.bType === 'string') {
                field.value = this.addQuotationForStringValues(field.value);
            }
            if (field.fields.length === 0) {
                valueArr.push(field.identifier + ':' + field.value);
            } else if (field.value) {
                valueArr.push(field.identifier + ':' + field.value);
            } else {
                const innerValueArr = [];
                field.fields.forEach((innerField) => {
                    if (innerField.identifier.startsWith('"')) {
                        innerField.identifier = JSON.parse(innerField.identifier);
                    }
                    if (innerField.bType === 'string') {
                        innerField.value = this.addQuotationForStringValues(innerField.value);
                    }
                    innerValueArr.push(innerField.identifier + ':' + innerField.value);
                });
                const localMap = field.identifier + ': {' + innerValueArr.join(',') + '}';
                valueArr.push(localMap);
            }
        });
        const map = '{' + valueArr.join(',') + '}';
        return map;
    }
    /**
     * Set data to the connector init arguments and create the connector init string
     * @param data
     */
    setDataToConnectorInitArgs(data) {
        const props = this.props.model.props;
        const connectorInit = props.model.getVariable().getInitialExpression();
        const pkgAlias = connectorInit.getConnectorType().getPackageAlias().value;
        this.getConnectorInstanceString(connectorInit, pkgAlias, data);
    }

    /**
     * Get the values already added as arguments to the connector init expression
     * @returns {Expression[]}
     */
    getDataAddedToConnectorInit() {
        const props = this.props.model.props;
        return props.model.getVariable().getInitialExpression().getExpressions();
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

        const styles = {
            popover: {
                top: props.bBox.y + 10 + 'px',
                left: positionX,
                height: '370px',
                minWidth: '500px',
            },
            arrowStyle: {
                top: positionY,
                left: props.bBox.x + 'px',
            },
        };
        const supportedProps = this.getSupportedProps();
        if (!supportedProps.length) {
            return null;
        }
        return (
            <PropertyWindow
                model={props.model}
                formHeading='Connector Properties'
                key={`connectorProp/${props.model.id}`}
                styles={styles}
                supportedProps={this.getSupportedProps()}
                addedValues={this.setDataToConnectorInitArgs}
            />);
    }
}

export default ConnectorPropertiesForm;

ConnectorPropertiesForm.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
};
