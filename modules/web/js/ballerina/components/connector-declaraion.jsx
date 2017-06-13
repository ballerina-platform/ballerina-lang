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
import LifeLine from './lifeline.jsx';
import { getComponentForNodeArray } from './utils';
import ConnectorActivationContainer from './connector-activation-container';
import * as DesignerDefaults from './../configs/designer-defaults';

// require possible themes
function requireAll(requireContext) {
    const components = {};
    requireContext.keys().map((item, index) => {
        const module = requireContext(item);
        if (module.default) {
            components[module.default.name] = module.default;
        }
    });
    return components;
}
const components = requireAll(require.context('./', true, /\.jsx$/));

class ConnectorDeclaration extends React.Component {

    constructor(props) {
        super(props);
        this.components = components;

        this.editorOptions = {
            propertyType: 'text',
            key: 'ConnectorDeclaration',
            model: props.model,
            getterMethod: props.model.getConnectorExpression,
            setterMethod: props.model.setConnectorExpression,
        };
    }

    render() {
        const statementContainerBBox = this.props.model.viewState.components.statementContainer;
        const connectorBBox = {};
        const model = this.props.model;
        const connectorName = model.getViewState().variableTextTrimmed;
        const children = getComponentForNodeArray(this.props.model.getChildren());
        connectorBBox.x = statementContainerBBox.x + (statementContainerBBox.w - DesignerDefaults.lifeLine.width) / 2;
        connectorBBox.y = statementContainerBBox.y - DesignerDefaults.lifeLine.head.height;
        connectorBBox.w = DesignerDefaults.lifeLine.width;
        connectorBBox.h = statementContainerBBox.h + DesignerDefaults.lifeLine.head.height * 2;

        const classes = {
            lineClass: 'connector-life-line',
            polygonClass: 'connector-life-line-polygon',
        };

        return (<g>
          <ConnectorActivationContainer bBox={statementContainerBBox} activationTarget={model} />
          <LifeLine
            title={connectorName} bBox={connectorBBox} editorOptions={this.editorOptions}
            onDelete={this.onDelete.bind(this)} classes={classes}
          />
          {children}
        </g>
        );
    }

    onDelete() {
        this.props.model.remove();
    }
}

export default ConnectorDeclaration;
