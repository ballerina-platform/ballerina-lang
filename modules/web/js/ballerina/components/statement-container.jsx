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
import PropTypes from 'prop-types';
import ASTNode from '../ast/node';
import DragDropManager from '../tool-palette/drag-drop-manager';
import './statement-container.css';

class StatementContainer extends React.Component {

    constructor(props) {
        super(props);
        this.state = {statementDropZoneActivated: false, dropZoneDropNotAllowed: false}
    }

    render() {
        const bBox = this.props.bBox;
        const dropZoneActivated = this.state.statementDropZoneActivated;
        const dropZoneDropNotAllowed = this.state.dropZoneDropNotAllowed;
        const dropZoneClassName = ((!dropZoneActivated) ? "drop-zone" : "drop-zone active")
              + ((dropZoneDropNotAllowed) ? " block" : "");
        return (<g className="statement-container">
            <rect x={ bBox.x } y={ bBox.y } width={ bBox.w } height={ bBox.h }
                className={dropZoneClassName}
                onMouseOver={(e) => this.onDropZoneActivate(e)}
                onMouseOut={(e) => this.onDropZoneDeactivate(e)}/>
            {this.props.children}
        </g>);
    }

    onDropZoneActivate (e) {
  			const dragDropManager = this.context.dragDropManager,
  						dropTarget = this.props.dropTarget;
  			if(dragDropManager.isOnDrag()) {
  					if(_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)){
  							return;
  					}
  					dragDropManager.setActivatedDropTarget(dropTarget,
  							(nodeBeingDragged) => {
  									// IMPORTANT: override node's default validation logic
  									// This drop zone is for statements only.
  									// Statements should only be allowed here.
  									return dropTarget.getFactory().isStatement(nodeBeingDragged);
  							}
            );
  					this.setState({statementDropZoneActivated: true,
                  dropZoneDropNotAllowed: !dragDropManager.isAtValidDropTarget()});
  					dragDropManager.once('drop-target-changed', () => {
  							this.setState({statementDropZoneActivated: false, dropZoneDropNotAllowed: false});
  					});
  			}
        e.stopPropagation();
  	}

  	onDropZoneDeactivate (e) {
  			const dragDropManager = this.context.dragDropManager,
  						dropTarget = this.props.dropTarget;
  			if(dragDropManager.isOnDrag()){
  					if(_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)){
  							dragDropManager.clearActivatedDropTarget();
  							this.setState({statementDropZoneActivated: false, dropZoneDropNotAllowed: false});
  					}
  			}
        e.stopPropagation();
  	}
}


StatementContainer.propTypes = {
	bBox: PropTypes.shape({
		x: PropTypes.number.isRequired,
		y: PropTypes.number.isRequired,
		w: PropTypes.number.isRequired,
		h: PropTypes.number.isRequired,
	}),
	dropTarget: PropTypes.instanceOf(ASTNode).isRequired
};

StatementContainer.contextTypes = {
	 dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired
};

export default StatementContainer;
