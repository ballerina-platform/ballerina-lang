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
import _ from 'lodash';
import {statement} from './../configs/designer-defaults';
import {lifeLine} from './../configs/designer-defaults';
import ASTNode from '../ast/node';
import DragDropManager from '../tool-palette/drag-drop-manager';
import MessageManager from './../visitors/message-manager';
import ASTFactory from './../ast/ballerina-ast-factory';
import './statement-decorator.css';
import ArrowDecorator from './arrow-decorator';

const text_offset = 50;

class StatementView extends React.Component {

	constructor(props) {
		super(props);
		this.state = {innerDropZoneActivated: false, innerDropZoneDropNotAllowed: false, drawInvocationFlag: -1};
	}

	render() {
		const { bBox, expression, model} = this.props;
		// we need to draw a drop box above and a statement box
		let statement_h = bBox.h - statement.gutter.v;
		let statement_y = bBox.y + statement.gutter.v;
		const text_x = bBox.x + (bBox.w / 2);
		const text_y = statement_y + (statement_h / 2);
		const drop_zone_x = bBox.x + (bBox.w - lifeLine.width)/2;
		const innerDropZoneActivated = this.state.innerDropZoneActivated;
		const innerDropZoneDropNotAllowed = this.state.innerDropZoneDropNotAllowed;
		const drawInvocationFlag = this.state.drawInvocationFlag;
		const messageManager = this.context.messageManager;
		let arrowStart = { x: 0, y: 0 };
		let arrowEnd = { x: 0, y: 0 };

		const dropZoneClassName = ((!innerDropZoneActivated) ? "inner-drop-zone" : "inner-drop-zone active")
											+ ((innerDropZoneDropNotAllowed) ? " block" : "");

		const arrowStartPointX = bBox.getRight();
		const arrowStartPointY = statement_y + statement_h/2;
		const radius = 10;

		let actionInvocation;
		let isActionInvocation = false;
		if (ASTFactory.isAssignmentStatement(model)) {
			actionInvocation = model.getChildren()[1].getChildren()[0];
			isActionInvocation = !_.isNil(actionInvocation) && ASTFactory.isActionInvocationExpression(actionInvocation);
		}

		if (drawInvocationFlag > -1) {
			arrowStart.x = arrowStartPointX;
			arrowStart.y = arrowStartPointY;
			arrowEnd.x = messageManager.getMessageEnd().x;
			arrowEnd.y = messageManager.getMessageEnd().y;
		}
		return (<g className="statement" >
			<rect x={drop_zone_x} y={bBox.y} width={lifeLine.width} height={statement.gutter.v}
					className={dropZoneClassName}
			 		onMouseOver={(e) => this.onDropZoneActivate(e)}
					onMouseOut={(e) => this.onDropZoneDeactivate(e)}/>
			<rect x={bBox.x} y={statement_y} width={bBox.w} height={statement_h} className="statement-rect" />
			<g className="statement-body">
				<text x={text_x} y={text_y} className="statement-text">{expression}</text>
			</g>
			{isActionInvocation &&
				<g>
					<circle cx={arrowStartPointX}
					cy={arrowStartPointY}
					r={radius}
					fill="#444"
					fillOpacity={0}
					onMouseOver={(e) => this.onArrowStartPointMouseOver(e)}
					onMouseOut={(e) => this.onArrowStartPointMouseOut(e)}
					onMouseDown={(e) => this.onMouseDown(e)}
					onMouseUp={(e) => this.onMouseUp(e)}/>
					{drawInvocationFlag > -1 && <ArrowDecorator start={arrowStart} end={arrowEnd} enable={enable}/>}
				</g>
			}
		</g>);
	}

	onDropZoneActivate (e) {
			const dragDropManager = this.context.dragDropManager,
						dropTarget = this.props.model.getParent(),
						model = this.props.model;
			if(dragDropManager.isOnDrag()) {
					if(_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)){
							return;
					}
					dragDropManager.setActivatedDropTarget(dropTarget,
							(nodeBeingDragged) => {
									// IMPORTANT: override node's default validation logic
									// This drop zone is for statements only.
									// Statements should only be allowed here.
									return model.getFactory().isStatement(nodeBeingDragged);
							},
							() => {
									return dropTarget.getIndexOfChild(model);
							}
					);
					this.setState({innerDropZoneActivated: true,
							innerDropZoneDropNotAllowed: !dragDropManager.isAtValidDropTarget()
					});
					dragDropManager.once('drop-target-changed', function(){
							this.setState({innerDropZoneActivated: false, innerDropZoneDropNotAllowed: false});
					}, this);
			}
	}

	onDropZoneDeactivate (e) {
			const dragDropManager = this.context.dragDropManager,
						dropTarget = this.props.model.getParent();
			if(dragDropManager.isOnDrag()){
					if(_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)){
							dragDropManager.clearActivatedDropTarget();
							this.setState({innerDropZoneActivated: false, innerDropZoneDropNotAllowed: false});
					}
			}
	}

	onArrowStartPointMouseOver (e) {
		e.target.style.fill = '#444';
		e.target.style.fillOpacity = 0.5;
		e.target.style.cursor = 'url(images/BlackHandwriting.cur), pointer';
	}

	onArrowStartPointMouseOut (e) {
		e.target.style.fill = '#444';
		e.target.style.fillOpacity = 0;
	}

	onMouseDown (e) {
		const messageManager = this.context.messageManager;
		const model = this.props.model;
		const bBox = model.getViewState().bBox;
		const statement_h = bBox.h - statement.gutter.v;
		const messageStartX = bBox.x +  bBox.w;
		const messageStartY = bBox.y +  statement_h/2;
		messageManager.setSource(this.props.model);
		messageManager.setIsOnDrag(true);
		messageManager.setMessageStart(messageStartX, messageStartY);
		messageManager.startDrawMessage();
	}

	onMouseUp (e) {
		const messageManager = this.context.messageManager;
		messageManager.reset();
	}

}

StatementView.propTypes = {
	bBox: PropTypes.shape({
		x: PropTypes.number.isRequired,
		y: PropTypes.number.isRequired,
		w: PropTypes.number.isRequired,
		h: PropTypes.number.isRequired,
	}),
	expression: PropTypes.string.isRequired,
	model: PropTypes.instanceOf(ASTNode).isRequired
};

StatementView.contextTypes = {
	 dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
	 messageManager: PropTypes.instanceOf(MessageManager).isRequired
};


export default StatementView;
