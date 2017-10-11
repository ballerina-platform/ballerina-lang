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
import ConnectorHelper from './../../../../../env/helpers/connector-helper';
import './properties-form.css';
import PropertiesWindow from './property-window';
import TreeUtils from './../../../../../model/tree-util';
import NodeFactory from './../../../../../model/node-factory';
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
            if (addedValues.length > 0 && (index <= addedValues.length - 1)) {
                // Check for the connection properties
                if (property.identifier === 'connectorOptions') {
                    if (TreeUtils.isSimpleVariableRef(addedValues[index])) {
                        property.value = this.getAddedValueOfProp(addedValues[index]);
                    } else {
                        // Check the field values given
                        if (property.fields) {
                            if (TreeUtils.isRecordLiteralExpr(addedValues[index])) { // If its a map
                                // Get all the key-value pairs
                                if (addedValues[index].getKeyValuePairs()) {
                                    addedValues[index].getKeyValuePairs().map((element) => {
                                        if (TreeUtils.isRecordLiteralKeyValue(element)) {
                                            const key = element.getKey().getVariableName().value;
                                                // Get the value
                                            if (element.getValue()) {
                                                // Iterate over the property fields until the key matches the field name
                                                property.fields.map((field) => {
                                                    if (field.getName() === key) {
                                                        field.setDefaultValue(this
                                                            .getAddedValueOfProp(element.getValue()));
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                } else {
                    property.value = this.getAddedValueOfProp(addedValues[index]);
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
    getConnectorInstanceString(connectorInit, data) {
        // Set the expressions to null
        connectorInit.setExpressions([], true);
        const optionProps = {};
        // Filter all values in the connection option struct
        Object.keys(data).forEach((key) => {
            if (key.startsWith('connectorOptions:')) {
                const propName = key.replace('connectorOptions:', '');
                optionProps[propName] = data[key];
            }
        });
        // According to the values added, construct the nodes again
        Object.keys(data).forEach((key, indexOfKey) => {
            // We need the supported props to preserve the order of the entered params
            this.getSupportedProps().map((property) => {
                let nodeToBeAdded = null;
                let indexOfThisNode = 0;
                if (key === property.identifier) {
                    // Check for options
                    if (key === 'connectorOptions') {
                        if (data[key]) {
                            // Create an identifier node
                            const variableNameNode = NodeFactory.createIdentifier({ value: data[key] });
                            // Create a SimpleVarDef Node
                            const simpleVarDefNode = NodeFactory.createSimpleVariableRef({
                                variableName: variableNameNode });
                            let index = connectorInit.getExpressions().length - 1;
                            if (index === -1) {
                                index = 0;
                            }
                            nodeToBeAdded = simpleVarDefNode;
                            indexOfThisNode = index + 1;
                        } else {
                            // No reference value, then add the values for the fields
                            // Create a RecordLiteralExprNode
                            const recordLiteralExprNode = NodeFactory.createRecordLiteralExpr();
                            // Iterate over the connector options
                            Object.keys(optionProps).forEach((prop) => {
                                // Check if there are value
                                if (optionProps[prop]) {
                                    // Iterate over the property fields of the struct to preserve the
                                    // order and to get the bType
                                    if (property.fields) {
                                        property.fields.map((field) => {
                                            if (prop === field.getName()) {
                                                // Get value of property
                                                let value = optionProps[prop];
                                                // Get type of the prop field
                                                if (field.getType() === 'string') {
                                                    value = this.addQuotationForStringValues(value);
                                                }
                                                // Create a SimpleVarDef Node for the key
                                                const variableNameNode = NodeFactory.createIdentifier({ value: prop });
                                                const simpleVarDefNode = NodeFactory.createSimpleVariableRef({
                                                    variableName: variableNameNode });
                                                // Create a Literal Node for the value
                                                const literalNode = NodeFactory.createLiteral({ value });
                                                // Create a Record Literal Value Node
                                                const recordLiteralKeyValueNode = NodeFactory.createRecordLiteralKeyValue({ key: simpleVarDefNode, value: literalNode });
                                                let index = recordLiteralExprNode.getKeyValuePairs().length - 1;
                                                if (index === -1) {
                                                    index = 0;
                                                }
                                                // Add the key-value pair node to the RecordLiteralExpr node
                                                recordLiteralExprNode.addKeyValuePairs(recordLiteralKeyValueNode, index + 1, true);
                                            }
                                        });
                                    }
                                }
                            });
                            // Add the RecordLiteralExpr Node to the connector init expression
                            let index = connectorInit.getExpressions().length - 1;
                            if (index === -1) {
                                index = 0;
                            }
                            nodeToBeAdded = recordLiteralExprNode;
                            indexOfThisNode = index + 1;
                        }
                    } else {
                        // Assuming for the literals user can only give that type variables
                        let value = data[key];
                        if (property.bType === 'string') {
                            value = this.addQuotationForStringValues(value);
                        }
                        const literalNode = NodeFactory.createLiteral({ value });
                        let index = connectorInit.getExpressions().length - 1;
                        if (index === -1) {
                            index = 0;
                        }
                        nodeToBeAdded = literalNode;
                        indexOfThisNode = index + 1;
                    }
                    if (indexOfKey === ((Object.keys(data).length - Object.keys(optionProps).length) - 1)) {
                        connectorInit.addExpressions(nodeToBeAdded, indexOfThisNode);
                    } else {
                        connectorInit.addExpressions(nodeToBeAdded, indexOfThisNode, true);
                    }
                }
            });
        });
    }

    /**
     * Set data to the connector init arguments and create the connector init string
     * @param data
     */
    setDataToConnectorInitArgs(data) {
        const props = this.props.model.props;
        const connectorInit = props.model.getVariable().getInitialExpression();
        this.getConnectorInstanceString(connectorInit, data);
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
        // this.getSupportedProps();
        const props = this.props.model.props;
        const positionX = (props.bBox.x) - 8 + 'px';
        const positionY = (props.bBox.y) + 'px';

        const styles = {
            popover: {
                top: props.bBox.y + 10 + 'px',
                left: positionX,
                height: '340px',
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
            <PropertiesWindow
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
