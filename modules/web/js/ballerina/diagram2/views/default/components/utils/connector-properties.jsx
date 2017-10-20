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
        const initialExpr = TreeUtils.getConnectorInitFromStatement(props.model);
        const pkgAlias = initialExpr.connectorType.packageAlias.value || '';
        const connectorProps = ConnectorHelper.getConnectorParameters(this.context.environment, pkgAlias);
        const addedValues = this.getDataAddedToConnectorInit();
        connectorProps.map((property, index) => {
            if (addedValues.length > 0) {
                // For simple fields -> Literal node or a simple variable reference
                if (TreeUtils.isSimpleVariableRef(addedValues[index]) || TreeUtils.isLiteral(addedValues[index])) {
                    property.value = this.getAddedValueOfProp(addedValues[index]);
                } else if (TreeUtils.isRecordLiteralExpr(addedValues[index])) { // For structs
                    this.getValueOfStructs(addedValues[index], property.fields);
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
            if (field.value === field.defaultValue && field.fields.length === 0) {
                return;
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
                let localMap = this.getStringifiedMap(field.fields);
                localMap = field.identifier + ': ' + localMap;
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
        const connectorInit = TreeUtils.getConnectorInitFromStatement(props.model);
        const pkgAlias = connectorInit.getConnectorType().getPackageAlias().value;
        this.getConnectorInstanceString(connectorInit, pkgAlias, data);
    }

    /**
     * Get the values already added as arguments to the connector init expression
     * @returns {Expression[]}
     */
    getDataAddedToConnectorInit() {
        const props = this.props.model.props;
        const initialExpr = TreeUtils.getConnectorInitFromStatement(props.model);
        return initialExpr.getExpressions();
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
        const initialExpr = TreeUtils.getConnectorInitFromStatement(props.model);
        const pkgAlias = initialExpr.connectorType.packageAlias.value || '';
        const connectorName = props.model.getVariableName().value || '';
        const formH = `${pkgAlias} Client Connector ${connectorName}`;
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
            />);
    }
}

export default ConnectorPropertiesForm;

ConnectorPropertiesForm.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
};
