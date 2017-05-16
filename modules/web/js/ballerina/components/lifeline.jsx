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
 
import React from "react";
import PropTypes from 'prop-types';
import SimpleBBox from '../ast/simple-bounding-box';
import {lifeLine} from '../configs/designer-defaults.js';
import ExpressionEditor from 'expression_editor_utils';

class LifeLine extends React.Component {
    
    constructor(props){
        super(props);
        let bBox = this.props.bBox;
        this.topBox = new SimpleBBox( bBox.x, bBox.y, bBox.w , lifeLine.head.height );
    }

    render() {
        const bBox = this.props.bBox;
        const centerX = bBox.x + (bBox.w / 2);
        const y2 = bBox.h + bBox.y;
        const titleBoxH = lifeLine.head.height;

        return (<g className="default-worker-life-line">
                    <line x1={ centerX } y1={ bBox.y + titleBoxH / 2} x2={ centerX } y2={ y2 - titleBoxH / 2 }></line>
                    <rect x={ bBox.x } y={ bBox.y } width={ bBox.w } height={ titleBoxH } rx="0" ry="0" className="connector-life-line-top-polygon" onClick={(e) => this.openExpressionEditor(e)} ></rect>
                    <rect x={ bBox.x } y={ y2 - titleBoxH } width={ bBox.w } height={ titleBoxH } rx="0" ry="0" className="connector-life-line-bottom-polygon"></rect>
                    <text x={ centerX } y={ bBox.y + titleBoxH / 2 } textAnchor="middle" alignmentBaseline="central" dominantBaseline="central" className="life-line-title genericT" onClick={(e) => this.openExpressionEditor(e)} >{ this.props.title }</text>
                    <text x={ centerX } y={ y2 - titleBoxH / 2 } textAnchor="middle" alignmentBaseline="central" dominantBaseline="central" className="life-line-title genericT">{ this.props.title }</text>
                </g>);
    }

	openExpressionEditor(e){
		let options = this.props.editorOptions;
		let packageScope = this.context.renderingContext.packagedScopedEnvironemnt;
		if(options){
			new ExpressionEditor( this.topBox , this.context.container , (text) => this.onUpdate(text), options , packageScope);
		}
	}

	onUpdate(text){
	}     
}

LifeLine.contextTypes = {
	 container: PropTypes.instanceOf(Object).isRequired,
	 renderingContext: PropTypes.instanceOf(Object).isRequired,     
};

export default LifeLine;
