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
import {statement} from './../configs/designer-defaults';
import {lifeLine} from './../configs/designer-defaults';
import * as DesignerDefaults from './../configs/designer-defaults';
import ASTNode from '../ast/node';
import ActionBox from './action-box';
import SimpleBBox from './../ast/simple-bounding-box';
import DragDropManager from '../tool-palette/drag-drop-manager';
import './compound-statement-decorator.css';

class CompoundStatementDecorator extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            innerDropZoneActivated: false,
            innerDropZoneDropNotAllowed: false,
            showActions: false
        };
    }

    render() {
        const { bBox } = this.props;
        // we need to draw a drop box above the statement
        let drop_zone_x = bBox.x + (bBox.w - lifeLine.width)/2;
        const innerDropZoneActivated = this.state.innerDropZoneActivated;
        const innerDropZoneDropNotAllowed = this.state.innerDropZoneDropNotAllowed;
        const dropZoneClassName = ((!innerDropZoneActivated) ? "inner-drop-zone" : "inner-drop-zone active")
    											+ ((innerDropZoneDropNotAllowed) ? " block" : "");

        const actionBbox = new SimpleBBox();
        actionBbox.w = DesignerDefaults.actionBox.width;
        actionBbox.h = DesignerDefaults.actionBox.height;
        actionBbox.x = bBox.x + ( bBox.w - actionBbox.w) / 2;
        actionBbox.y = bBox.y + bBox.h + DesignerDefaults.actionBox.padding.top;

        return (<g className="compound-statement"
				   onMouseOut={ this.setActionVisibility.bind(this, false) }
				   onMouseOver={ (e) => {
                       if (!this.context.dragDropManager.isOnDrag()) {
                           this.setActionVisibility(true)
                       }
                   }}>
			<rect x={drop_zone_x} y={bBox.y} width={lifeLine.width} height={statement.gutter.v}
				  className={dropZoneClassName}
				  onMouseOver={(e) => this.onDropZoneActivate(e)}
				  onMouseOut={(e) => this.onDropZoneDeactivate(e)}/>
            {this.props.children}
			<ActionBox
				bBox={ actionBbox }
				show={ this.state.showActions }
				isBreakpoint={ false }
				onDelete={ () => this.onDelete() }
				onJumptoCodeLine={ () => this.onJumptoCodeLine() }
				onBreakpointClick={ () => this.onBreakpointClick() }
			/>
		</g>);
    }

    setActionVisibility(show) {
        this.setState({showActions: show})
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
	 dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired
};

export default CompoundStatementDecorator;
