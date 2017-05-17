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
import * as DesignerDefaults from './../configs/designer-defaults';
import DragDropManager from '../tool-palette/drag-drop-manager';
import ReactDOM from "react-dom";
import ActionBox from "./action-box";
import ActiveArbiter from './active-arbiter';

class LifeLine extends React.Component {

    constructor(props){
        super(props);
        let bBox = this.props.bBox;
        this.topBox = new SimpleBBox( bBox.x, bBox.y, bBox.w , lifeLine.head.height );
        this.state = {active: 'hidden'};
    }

    onJumptoCodeLine() {
        const {renderingContext: {ballerinaFileEditor}} = this.context;
        const container = ballerinaFileEditor._container;
        $(container).find('.view-source-btn').trigger('click');
        ballerinaFileEditor.getSourceView().jumpToLine({});
    }

    render() {
        const bBox = this.props.bBox;
        const lineClass = this.props.classes.lineClass + " unhoverable";
        const polygonClassTop = this.props.classes.polygonClass;
        const polygonClassBottom = this.props.classes.polygonClass + " unhoverable";
        const centerX = bBox.x + (bBox.w / 2);
        const y2 = bBox.h + bBox.y;
        const titleBoxH = lifeLine.head.height;

        const actionBbox = new SimpleBBox();
        actionBbox.w = (3 * DesignerDefaults.actionBox.width - 14) / 4;
        actionBbox.h = DesignerDefaults.actionBox.height;
        actionBbox.x = bBox.x + ( bBox.w - actionBbox.w) / 2;
        actionBbox.y = bBox.y + titleBoxH + DesignerDefaults.actionBox.padding.top;

        return (<g className="life-line-group"
                   onMouseOut={ this.setActionVisibility.bind(this, false) }
                   onMouseOver={ this.setActionVisibility.bind(this, true)}>
                        <line x1={ centerX } y1={ bBox.y + titleBoxH / 2} x2={ centerX } y2={ y2 - titleBoxH / 2 }
                              className={lineClass}/>
                        <rect x={ bBox.x } y={ bBox.y } width={ bBox.w } height={ titleBoxH } rx="0" ry="0"
                              className={polygonClassTop} onClick={(e) => this.openExpressionEditor(e)}/>
                        <rect x={ bBox.x } y={ y2 - titleBoxH } width={ bBox.w } height={ titleBoxH } rx="0" ry="0"
                              className={polygonClassBottom}/>
                        <text x={ centerX } y={ bBox.y + titleBoxH / 2 } textAnchor="middle" alignmentBaseline="central"
                              dominantBaseline="central" className="life-line-text genericT"
                              onClick={(e) => this.openExpressionEditor(e)}>{ this.props.title }</text>
                        <text x={ centerX } y={ y2 - titleBoxH / 2 } textAnchor="middle" alignmentBaseline="central"
                              dominantBaseline="central" className="life-line-text genericT unhoverable">{ this.props.title }</text>
                        {this.props.onDelete &&
                            <ActionBox
                              show={this.state.active}
                              bBox={actionBbox}
                              onDelete={ () => this.props.onDelete() }
                              onJumptoCodeLine={ ()=> this.onJumptoCodeLine()}
                            />
                        }
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

    setActionVisibility(show, e) {
        if (!this.context.dragDropManager.isOnDrag()) {
            let elm = e.target;
            const myRoot = ReactDOM.findDOMNode(this);
            const regex = new RegExp('(^|\\s)unhoverable(\\s|$)');
            let isUnhighliable = false;
            while (elm && elm !== myRoot && elm.getAttribute) {
                if (regex.test(elm.getAttribute('class'))) {
                    isUnhighliable = true;
                }
                elm = elm.parentNode;
            }

            if (!isUnhighliable && show) {
                this.context.activeArbiter.readyToActivate(this);
            } else {
                this.context.activeArbiter.readyToDeactivate(this);
            }
            if (isUnhighliable) {
            }
        }
    }

    onDelete() {
        this.props.onDelete();
    }
}

LifeLine.contextTypes = {
	container: PropTypes.instanceOf(Object).isRequired,
	renderingContext: PropTypes.instanceOf(Object).isRequired,
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    activeArbiter: PropTypes.instanceOf(ActiveArbiter).isRequired
};

export default LifeLine;
