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
import {getComponentForNodeArray} from './utils';
import StatementContainer from './statement-container';
import * as DesignerDefaults from './../configs/designer-defaults';

// require possible themes
function requireAll(requireContext) {
    let components = {};
    requireContext.keys().map((item, index) => {
        var module = requireContext(item);
        if(module.default){
            components[module.default.name] = module.default;
        }
    });
    return components;
}
var components = requireAll(require.context('./', true, /\.jsx$/));

class WorkerDeclaration extends React.Component {

    constructor(props) {
        super(props);
        this.components = components;
    }

    render() {
        const statementContainerBBox = this.props.model.viewState.components.statementContainer;
        let workerBBox = {};
        var children = getComponentForNodeArray(this.props.model.getChildren());
        workerBBox.x = statementContainerBBox.x + (statementContainerBBox.w - DesignerDefaults.lifeLine.width)/2;
        workerBBox.y = statementContainerBBox.y - DesignerDefaults.lifeLine.head.height;
        workerBBox.w = DesignerDefaults.lifeLine.width;
        workerBBox.h = statementContainerBBox.h + DesignerDefaults.lifeLine.head.height * 2;

        return (<g>
                <StatementContainer dropTarget={this.props.model} bBox={statementContainerBBox}/>
                <LifeLine title="Worker" bBox={workerBBox}/>
                {children}
            </g>
        );
    }
}

export default WorkerDeclaration;
