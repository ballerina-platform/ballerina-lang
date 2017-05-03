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
import StatementContainer from './statement-container';
import PanelDecorator from './panel-decorator';
import {getComponentForNodeArray} from './utils';
import {lifeLine} from './../configs/designer-defaults';

class FunctionDefinition extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        const bBox = this.props.model.viewState.bBox;
        const name = this.props.model.getFunctionName();
        const statementContainerBBox = this.props.model.getViewState().components.statementContainer;

        //lets calculate function worker lifeline bounding box.
        let function_worker_bBox = {};
        function_worker_bBox.x = statementContainerBBox.x + (statementContainerBBox.w - lifeLine.width)/2;
        function_worker_bBox.y = statementContainerBBox.y - lifeLine.head.height ;
        function_worker_bBox.w = lifeLine.width;
        function_worker_bBox.h = statementContainerBBox.h + lifeLine.head.height * 2;

        var children = getComponentForNodeArray(this.props.model.getChildren());

        // change icon for main function
        let icons = "tool-icons/function";
        if('main' == name){
            icons = "tool-icons/main-function";
        }

        return (<PanelDecorator icon={icons} title={name} bBox={bBox}
                        model={this.props.model}
                        dropTarget={this.props.model}
                        dropSourceValidateCB={(node) => this.canDropToPanelBody(node)}>
                    <StatementContainer  dropTarget={this.props.model}
                      title="StatementContainer" bBox={statementContainerBBox}/>
                    <LifeLine title="FunctionWorker" bBox={function_worker_bBox}/>
                    {children}
                </PanelDecorator>);
    }

    canDropToPanelBody (nodeBeingDragged) {
          let nodeFactory = this.props.model.getFactory();
          // IMPORTANT: override default validation logic
          // Panel's drop zone is for worker and connector declarations only.
          // Statements should only be allowed on top of function worker's dropzone.
          return nodeFactory.isConnectorDeclaration(nodeBeingDragged)
              || nodeFactory.isWorkerDeclaration(nodeBeingDragged);
    }


}

export default FunctionDefinition;
