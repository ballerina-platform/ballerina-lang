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
     * ToDo Update the edited expression
     */
    updateExpression(value) {
    }

    /**
     * Render Function for the Connector Declaration Decorator
     * */
    render() {
        // create lifelines for connector declarations.
        const connectorInitExpression = TreeUtils.getConnectorInitFromStatement(this.props.model);
        const connectorClasses = {
            lineClass: 'connector-life-line',
            polygonClass: 'connector-life-line-polygon',
        };
        return (
            <g>
                <LifeLine
                    model={this.props.model}
                    title={this.props.title}
                    bBox={this.props.bBox}
                    classes={connectorClasses}
                    icon={ImageUtil.getConnectorIcon(connectorInitExpression.connectorType.packageAlias.value)}
                    editorOptions={this.editorOptions}
                    iconColor='#1a8278'
                />
            </g>
        );
    }
}

ConnectorDeclarationDecorator.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

export default ConnectorDeclarationDecorator;
