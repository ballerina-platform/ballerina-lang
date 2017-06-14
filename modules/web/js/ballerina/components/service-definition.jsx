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
import ResourceDefinition from './resource-definition.jsx';
import StatementView from './statement-decorator.jsx';
import PanelDecorator from './panel-decorator';
import { getComponentForNodeArray } from './utils';
import GlobalExpanded from './globals-expanded';
import GlobalDefinitions from './global-definitions';
import * as DesignerDefaults from './../configs/designer-defaults';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';

class ServiceDefinition extends React.Component {

    constructor(props) {
        super(props);
        this.variableDefRegex = /\s*(int|string|boolean)\s+([a-zA-Z0-9_]+)\s*=\s*(.*)/g; // This is not 100% accurate
        this.handleAddVariable = this.handleAddVariable.bind(this);
        this.handleDeleteVariable = this.handleDeleteVariable.bind(this);
        this.handleVarialblesBadgeClick = this.handleVarialblesBadgeClick.bind(this);
    }

    render() {
        const model = this.props.model;
        const viewState = model.getViewState();
        const components = viewState.components;
        const bBox = model.viewState.bBox;
        const variables = model.filterChildren(child => BallerinaASTFactory.isVariableDefinitionStatement(child));

        // get the service name
        const title = model.getServiceName();

        const childrenWithNoVariables = model.filterChildren(child => !BallerinaASTFactory.isVariableDefinitionStatement(child));

        /**
         * Here we skip rendering the variables
         */
        const children = getComponentForNodeArray(childrenWithNoVariables);

        const expandedVariablesBBox = {
            x: bBox.x + DesignerDefaults.panel.body.padding.left,
            y: components.body.y + DesignerDefaults.panel.body.padding.top,
        };

        return (<PanelDecorator
          icon="tool-icons/service" title={title} bBox={bBox}
          model={model}
          dropTarget={this.props.model}
          dropSourceValidateCB={node => this.canDropToPanelBody(node)}
        >
          {
                    viewState.variablesExpanded ?
                      <GlobalExpanded
                        bBox={expandedVariablesBBox} globals={variables} onCollapse={this.handleVarialblesBadgeClick}
                        title="Variables" addText={'+ Add Variable'} onAddNewValue={this.handleAddVariable} onDeleteClick={this.handleDeleteVariable}
                        getValue={g => (g.getStatementString())}
                      /> :
                      <GlobalDefinitions
                        bBox={expandedVariablesBBox} numberOfItems={variables.length}
                        title={'Variables'} onExpand={this.handleVarialblesBadgeClick}
                      />
                }
          {children}
        </PanelDecorator>);
    }

    handleVarialblesBadgeClick() {
        this.props.model.setAttribute('viewState.variablesExpanded', !this.props.model.viewState.variablesExpanded);
    }

    canDropToPanelBody(nodeBeingDragged) {
        const nodeFactory = this.props.model.getFactory();
          // IMPORTANT: override default validation logic
          // Panel's drop zone is for resource defs and connector declarations only.
        return nodeFactory.isConnectorDeclaration(nodeBeingDragged)
              || nodeFactory.isResourceDefinition(nodeBeingDragged);
    }

    handleAddVariable(value) {
        const variableDefRegex = /\s*(int|string|boolean)\s+([a-zA-Z0-9_]+)\s*=\s*(.*)/g; // This is not 100% accurate
        const match = variableDefRegex.exec(value);
        if (match && match[1] && match[2] && match[3]) {
            this.props.model.addVariableDefinitionStatement(match[1], match[2], match[3]);
        }
    }

    handleDeleteVariable(deletedGlobal) {
        this.props.model.removeVariableDefinitionStatement(deletedGlobal.getID());
    }
}

export default ServiceDefinition;
