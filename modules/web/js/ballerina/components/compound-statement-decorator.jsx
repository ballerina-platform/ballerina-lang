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
import ReactDOM from "react-dom";
import PropTypes from 'prop-types';
import {statement} from './../configs/designer-defaults';
import {lifeLine} from './../configs/designer-defaults';
import * as DesignerDefaults from './../configs/designer-defaults';
import ASTNode from '../ast/node';
import ActionBox from './action-box';
import SimpleBBox from './../ast/simple-bounding-box';
import DragDropManager from '../tool-palette/drag-drop-manager';
import './compound-statement-decorator.css';
import ActiveArbiter from './active-arbiter';
import Breakpoint from './breakpoint';

class CompoundStatementDecorator extends React.Component {

    constructor(props, context) {
        super(props, context);
        const {dragDropManager} = context;
        dragDropManager.on('drag-start', this.startDropZones.bind(this));
        dragDropManager.on('drag-stop', this.stopDragZones.bind(this));

        this.state = {
            innerDropZoneActivated: false,
            innerDropZoneDropNotAllowed: false,
            innerDropZoneExist: false,
            active: 'hidden'
        };
    }

    startDropZones() {
        this.setState({innerDropZoneExist: true});
    }

    stopDragZones() {
        this.setState({innerDropZoneExist: false});
    }

    render() {
        const { bBox, model } = this.props;
        // we need to draw a drop box above the statement
        let drop_zone_x = bBox.x + (bBox.w - lifeLine.width)/2;
        const innerDropZoneActivated = this.state.innerDropZoneActivated;
        const innerDropZoneDropNotAllowed = this.state.innerDropZoneDropNotAllowed;
        const dropZoneClassName = ((!innerDropZoneActivated) ? "inner-drop-zone" : "inner-drop-zone active")
    											+ ((innerDropZoneDropNotAllowed) ? " block" : "");

        const fill = this.state.innerDropZoneExist ? {} : {fill: 'none'};
        const actionBbox = new SimpleBBox();
        actionBbox.w = DesignerDefaults.actionBox.width;
        actionBbox.h = DesignerDefaults.actionBox.height;
        actionBbox.x = bBox.x + ( bBox.w - actionBbox.w) / 2;
        actionBbox.y = bBox.y + bBox.h + DesignerDefaults.actionBox.padding.top;

        return (<g className="compound-statement"
                   onMouseOut={ this.setActionVisibility.bind(this, false) }
                   onMouseOver={ this.setActionVisibility.bind(this, true)}>
			<rect x={drop_zone_x} y={bBox.y} width={lifeLine.width} height={statement.gutter.v}
                  className={dropZoneClassName} {...fill}/>
            {this.props.children}
			<ActionBox
                bBox={ actionBbox }
                show={ this.state.active }
                isBreakpoint={ model.isBreakpoint }
                onDelete={ () => this.onDelete() }
                onJumptoCodeLine={ () => this.onJumptoCodeLine() }
                onBreakpointClick={ () => this.onBreakpointClick() }
			/>
      {		model.isBreakpoint &&
          this.renderBreakpointIndicator()
      }
		</g>);
    }

    onBreakpointClick() {
        const { model } = this.props;
        const { isBreakpoint = false } = model;
        if(model.isBreakpoint) {
            model.removeBreakpoint();
        } else {
            model.addBreakpoint();
        }
    }

    renderBreakpointIndicator() {
        const breakpointSize = 14;
        const { bBox } = this.props;
        const pointX = bBox.x + bBox.w - breakpointSize/2;
        const pointY = bBox.y - breakpointSize/2 + statement.gutter.v;
        return (
            <Breakpoint
                x={pointX}
                y={pointY}
                size={breakpointSize}
                isBreakpoint={this.props.model.isBreakpoint}
                onClick = { () => this.onBreakpointClick() }
            />
        );
    }

    setActionVisibility(show, e) {
        if (!this.context.dragDropManager.isOnDrag()) {
            const myRoot = ReactDOM.findDOMNode(this);
            if (show) {
                const regex = new RegExp('(^|\\s)(compound-)?statement(\\s|$)');
                let isInChildStatement = false;
                let isInStatement = false;
                let isFromChildStatement = false;

                let elm = e.target;
                while (elm && elm !== myRoot && elm.getAttribute) {
                    if (regex.test(elm.getAttribute('class'))) {
                        isInStatement = true;
                    }
                    elm = elm.parentNode;
                }
                if (elm === myRoot && isInStatement) {
                    isInChildStatement = true;
                }

                elm = e.relatedTarget;
                isInStatement = false;
                while (elm && elm !== myRoot && elm.getAttribute) {
                    if (regex.test(elm.getAttribute('class'))) {
                        isInStatement = true;
                    }
                    elm = elm.parentNode;
                }
                if (elm === myRoot && isInStatement) {
                    isFromChildStatement = true;
                }

                if (!isInChildStatement) {
                    if (isFromChildStatement) {
                        this.context.activeArbiter.readyToDelayedActivate(this);
                    } else {
                        this.context.activeArbiter.readyToActivate(this);
                    }
                }
            } else {
                let elm = e.relatedTarget;
                let isInMe = false;
                while (elm && elm.getAttribute) {
                    if (elm === myRoot) {
                        isInMe = true;
                    }
                    elm = elm.parentNode;
                }
                if (!isInMe) {
                    this.context.activeArbiter.readyToDeactivate(this);
                }
            }
        }
    }

    onDelete() {
        this.props.model.remove();
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
}

CompoundStatementDecorator.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
    model: PropTypes.instanceOf(ASTNode).isRequired
};

CompoundStatementDecorator.contextTypes = {
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    activeArbiter: PropTypes.instanceOf(ActiveArbiter).isRequired
};

export default CompoundStatementDecorator;
