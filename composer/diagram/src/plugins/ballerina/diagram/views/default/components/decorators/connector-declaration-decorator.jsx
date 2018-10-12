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
import LifeLine from '../decorators/lifeline';
import ImageUtil from '../../../../image-util';
import TreeUtils from './../../../../../model/tree-util';

/**
 * Connector Declaration Decorator.
 * */
class ConnectorDeclarationDecorator extends React.Component {

    /**
     * Constructor for connector declaration
     * @param {objects} props - properties
     */
    constructor(props) {
        super(props);
        this.getConnectorName = this.getConnectorName.bind(this);
        this.setConnectorName = this.setConnectorName.bind(this);
    }

    /**
     * ToDo Update the edited expression
     */
    updateExpression(value) {
    }

    /**
     * Get connector name to be displayed
     * @return {*} - name of the connector
     */
    getConnectorName() {
        const model = this.props.model;
        let connectorName;
        if (TreeUtils.isVariableDef(model)) {
            connectorName = model.getVariableName().value;
        }
        return connectorName;
    }

    /**
     * Set connector name callback for the expression editor
     * @param {string} newName - new connector name
     */
    setConnectorName(newName) {
        const model = this.props.model;
        if (TreeUtils.isVariableDef(model)) {
            const oldVariable = this.props.model.getVariable();
            oldVariable.name.value = newName;
            model.setVariable(oldVariable, false);
        }
    }

    /**
     * Render Function for the Connector Declaration Decorator
     * */
    render() {
        // create lifelines for connector declarations.
        const packageAlias = this.props.model.variable.typeNode.packageAlias.value;
        const connectorClasses = {
            lineClass: 'connector-life-line',
            polygonClass: 'connector-life-line-polygon',
            textClass: 'connector-icon',
        };

        // Editor options for the expression editor
        this.editorOptions = {
            propertyType: 'text',
            key: 'ConnectorDeclaration',
            model: this.props.model,
            getterMethod: this.getConnectorName,
            setterMethod: this.setConnectorName,
        };
        return (
            <g>
                <LifeLine
                    model={this.props.model}
                    title={this.props.title}
                    bBox={this.props.bBox}
                    classes={connectorClasses}
                    icon={ImageUtil.getCodePoint('endpoint')}
                    editorOptions={this.editorOptions}
                />
            </g>
        );
    }
}

ConnectorDeclarationDecorator.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

export default ConnectorDeclarationDecorator;
