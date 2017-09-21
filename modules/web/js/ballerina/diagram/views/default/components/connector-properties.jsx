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
import ConnectorHelper from '../../../../env/helpers/connector-helper';
import './properties-form.css';
import ASTFactory from '../../../../ast/ast-factory';
import PropertiesWindow from './property-window';
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
        this.getFullPkgPath = this.getFullPkgPath.bind(this);
    }

    /**
     * Get already added values to properties
     * @param node
     * @returns {string}
     */
    getAddedValueOfProp(node) {
        let value = '';
        if (ASTFactory.isBasicLiteralExpression(node)) {
            value = node.getBasicLiteralValue();
        } else if (ASTFactory.isSimpleVariableReferenceExpression(node)) {
            value = node.getVariableName();
        }
        return value;
    }

    /**
     * Get full package path of connector
     * @param pkgName
     * @returns {string}
     */
    getFullPkgPath(pkgName) {
        let pkgPath = '';
        switch (pkgName) {
            case 'twitter':
                pkgPath = 'org.wso2.ballerina.connectors.twitter';
                break;
            case 'googlespreadsheet':
                pkgPath = 'org.wso2.ballerina.connectors.googlespreadsheet';
                break;
            case 'amazonlambda':
                pkgPath = 'org.wso2.ballerina.connectors.amazonlambda';
                break;
            case 'medium':
                pkgPath = 'org.wso2.ballerina.connectors.medium';
                break;
            case 'facebook':
                pkgPath = 'org.wso2.ballerina.connectors.facebook';
                break;
            case 'basicauth':
                pkgPath = 'org.wso2.ballerina.connectors.basicauth';
                break;
            case 'jira':
                pkgPath = 'org.wso2.ballerina.connectors.jira';
                break;
            case 'gmail':
                pkgPath = 'org.wso2.ballerina.connectors.gmail';
                break;
            case 'http':
                pkgPath = 'ballerina.net.http';
                break;
            case 'salesforcerest':
                pkgPath = 'org.wso2.ballerina.connectors.salesforcerest';
                break;
            case 'ftp':
                pkgPath = 'ballerina.net.ftp';
                break;
            case 'amazons3':
                pkgPath = 'org.wso2.ballerina.connectors.amazons3';
                break;
            case 'linkedin':
                pkgPath = 'org.wso2.ballerina.connectors.linkedin';
                break;
            case 'oauth2':
                pkgPath = 'org.wso2.ballerina.connectors.oauth2';
                break;
            case 'amazonauth':
                pkgPath = 'org.wso2.ballerina.connectors.amazonauth';
                break;
            case 'sql':
                pkgPath = 'ballerina.data.sql';
                break;
            case 'salesforcesoap':
                pkgPath = 'org.wso2.ballerina.connectors.salesforcesoap';
                break;
            case 'etcd':
                pkgPath = 'org.wso2.ballerina.connectors.etcd';
                break;
            case 'ws':
                pkgPath = 'ballerina.net.ws';
                break;
            case 'jms':
                pkgPath = 'ballerina.net.jms';
                break;
            case 'soap':
                pkgPath = 'org.wso2.ballerina.connectors.soap';
                break;
        }
        return pkgPath;
    }

    /**
     * Get the connector properties supported by each connector
     * @returns {*}
     */
    getSupportedProps() {
        const fullPkgPath = this.getFullPkgPath(this.props.model.getDeclarationStatement().getRightExpression()
            .getConnectorName().getPackageName());
        const connectorProps = ConnectorHelper.getConnectorParameters(this.props.environment, fullPkgPath);
        const addedValues = this.getDataAddedToConnectorInit();
        connectorProps.map((property, index) => {
            if (addedValues.length > 0) {
                property.value = this.getAddedValueOfProp(addedValues[index]);
            }
        });
        return connectorProps;
    }

    /**
     * Create the connector init string
     * @param connectorInit
     * @param data
     * @returns {string}
     */
    getConnectorInstanceString(connectorInit, data) {
        let spacesBeforeNameRef = '';
        let spacesNameRefToArgStart = '';

        if (connectorInit.getIsFilterConnectorInitExpr()) {
            spacesBeforeNameRef = connectorInit.whiteSpace.useDefault ? ' ' : connectorInit.getWSRegion(0);
            spacesNameRefToArgStart = connectorInit.whiteSpace.useDefault ? ' ' : connectorInit.getWSRegion(1);
        } else {
            spacesBeforeNameRef = connectorInit.whiteSpace.useDefault ? ' ' : connectorInit.getWSRegion(1);
            spacesNameRefToArgStart = connectorInit.whiteSpace.useDefault ? ' ' : connectorInit.getWSRegion(2);
        }

        let connectorInstanceString = spacesBeforeNameRef +
            connectorInit.getConnectorName().toString().trim()
            + spacesNameRefToArgStart
            + '(';

        Object.keys(data).forEach((key, index) => {
            if (index !== 0) {
                connectorInstanceString += ', ';
            }
            this.getSupportedProps().map((property) => {
                if (key === property.identifier) {
                    switch (property.bType) {
                        case 'string':
                            connectorInstanceString += JSON.stringify(data[key]);
                            break;
                        default:
                            connectorInstanceString += data[key];
                    }
                }
            });
        });

        return connectorInstanceString + ')';
    }

    /**
     * Set data to the connector init arguments and create the connector init string
     * @param data
     */
    setDataToConnectorInitArgs(data) {
        const connectorInit = this.props.model.getDeclarationStatement().getRightExpression();
        const expr = 'create' + this.getConnectorInstanceString(connectorInit, data);
        connectorInit.setExpressionFromString(expr);
    }

    /**
     * Get the values already added as arguments to the connector init expression
     * @returns {Expression[]}
     */
    getDataAddedToConnectorInit() {
        return this.props.model.getDeclarationStatement().getRightExpression().getArgs();
    }

    /**
     * Renders the view for a connector properties window
     *
     * @returns {ReactElement} The view.
     * @memberof connector properties window
     */
    render() {
        const positionX = (this.props.bBox.x) - 8 + 'px';
        const positionY = (this.props.bBox.y) + 'px';

        const styles = {
            popover: {
                top: this.props.bBox.y + 10 + 'px',
                left: positionX,
                height: '280px',
            },
            arrowStyle: {
                top: positionY,
                left: this.props.bBox.x + 'px',
            },
        };

        return (
            <PropertiesWindow
                model={this.props.model}
                formHeading='Connector Properties'
                key={`connectorProp/${this.props.model.id}`}
                styles={styles}
                supportedProps={this.getSupportedProps()}
                editor={this.props.editor}
                addedValues={this.setDataToConnectorInitArgs}
            />);
    }
}

export default ConnectorPropertiesForm;

ConnectorPropertiesForm.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
};
