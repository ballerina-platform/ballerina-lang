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
import StatementView from './statement-decorator';
import PanelDecorator from './panel-decorator';
import {getComponentForNodeArray} from './components';
import {panel} from './../configs/designer-defaults';
import {statement} from './../configs/designer-defaults';

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

class FunctionDefinition extends React.Component {

    constructor(props) {
        super(props);
        this.components = components;
    }    

    render() {
        const bBox = this.props.model.viewState.bBox;
        const name = this.props.model.getFunctionName();

        //lets calculate function worker lifeline bounding box.
        let func_worker_bBox = {};
        func_worker_bBox.x = bBox.x + panel.body.padding.left;
        func_worker_bBox.y = bBox.y + panel.heading.height + panel.body.padding.top;
        //@todo set the correct width
        func_worker_bBox.w = statement.width;
        func_worker_bBox.h = bBox.h - panel.heading.height - panel.body.padding.top - panel.body.padding.bottom;

        var children = getComponentForNodeArray(this.props.model.getChildren(),this.components);
        console.log(children);
        return (<PanelDecorator title={name} bBox={bBox}>
                    <LifeLine title="FunctionWorker" bBox={func_worker_bBox}/>
                    {children}
                </PanelDecorator>);
    }
}

export default FunctionDefinition;
