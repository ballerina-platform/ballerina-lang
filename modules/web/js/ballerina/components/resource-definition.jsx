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
import LifeLineDecorator from './lifeline.jsx';
import StatementContainer from './statement-container';
import PanelDecorator from './panel-decorator';
import ParameterView from './parameter-view';
import {getComponentForNodeArray} from './utils';
import {lifeLine} from './../configs/designer-defaults';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';

class ResourceDefinition extends React.Component {

    render() {
        const bBox = this.props.model.viewState.bBox;
        const name = this.props.model.getResourceName();
        const statementContainerBBox = this.props.model.getViewState().components.statementContainer;
        let annotations = this.props.model.getChildren().filter(function(child){
            return BallerinaASTFactory.isAnnotation(child);
        });

        //lets calculate function worker lifeline bounding box.
        let resource_worker_bBox = {};
        resource_worker_bBox.x = statementContainerBBox.x + (statementContainerBBox.w - lifeLine.width)/2;
        resource_worker_bBox.y = statementContainerBBox.y - lifeLine.head.height ;
        resource_worker_bBox.w = lifeLine.width;
        resource_worker_bBox.h = statementContainerBBox.h + lifeLine.head.height * 2;

        var children = getComponentForNodeArray(this.props.model.getChildren());

        let titleComponentData = this.props.model.getParameters().length > 0 ? [{ component: ParameterView, 
            title: 'Parameters: ', 
            prefixView: this.props.model.getViewState().components.parametersPrefixContainer,
            prefixViewClassName: 'parameters-prefix-wrapper', 
            models: this.props.model.getParameters()}
            ] : [];

        return (<PanelDecorator icon="tool-icons/resource" title={name} annotations={annotations} bBox={bBox}
                        model={this.props.model}
                        dropTarget={this.props.model}
                        dropSourceValidateCB={(node) => this.canDropToPanelBody(node)}
                        titleComponentData={titleComponentData}>
            <g>
                <StatementContainer dropTarget={this.props.model} bBox={statementContainerBBox}/>
                <LifeLineDecorator title="ResourceWorker" bBox={resource_worker_bBox}/>
                {children}
            </g>
        </PanelDecorator>);
    }

    canDropToPanelBody (nodeBeingDragged) {
          let nodeFactory = this.props.model.getFactory();
          // IMPORTANT: override default validation logic
          // Panel's drop zone is for worker and connector declarations only.
          // Statements should only be allowed on top of resource worker's dropzone.
          return nodeFactory.isConnectorDeclaration(nodeBeingDragged)
              || nodeFactory.isWorkerDeclaration(nodeBeingDragged);
    }
}

export default ResourceDefinition;
