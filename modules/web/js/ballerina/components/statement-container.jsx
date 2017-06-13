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
import MessageManager from './../visitors/message-manager';
import ASTFactory from './../ast/ballerina-ast-factory';
import './statement-container.css';

class StatementContainer extends React.Component {

    constructor(props, context) {
        super(props, context);
        const { dragDropManager, messageManager } = context;
        this.startDropZones = this.startDropZones.bind(this);
        this.stopDragZones = this.stopDragZones.bind(this);
        this.state = {
            dropZoneExist: false,
            statementDropZoneActivated: false,
            dropZoneDropNotAllowed: false,
        };
    }

    componentDidMount() {
        const { dragDropManager, messageManager } = this.context;
        dragDropManager.on('drag-start', this.startDropZones);
        dragDropManager.on('drag-stop', this.stopDragZones);
        messageManager.on('message-draw-start', this.stopDragZones);
        messageManager.on('message-draw-stop', this.stopDragZones);
    }

    componentWillUnmount() {
        const { dragDropManager, messageManager } = this.context;
        dragDropManager.off('drag-start', this.startDropZones);
        dragDropManager.off('drag-stop', this.stopDragZones);
        messageManager.off('message-draw-start', this.stopDragZones);
        messageManager.off('message-draw-stop', this.stopDragZones);
    }

    startDropZones() {
        this.setState({ dropZoneExist: true });
    }

    stopDragZones() {
        this.setState({ dropZoneExist: false });
    }

    render() {
        const bBox = this.props.bBox;
        const dropZoneActivated = this.state.statementDropZoneActivated;
        const dropZoneDropNotAllowed = this.state.dropZoneDropNotAllowed;
        const dropZoneClassName = ((!dropZoneActivated) ? 'drop-zone' : 'drop-zone active')
              + ((dropZoneDropNotAllowed) ? ' block' : '');
        const containerClassName = ((dropZoneActivated) ? 'statement-container drop-zone active' : 'statement-container');

        const fill = this.state.dropZoneExist ? {} : { fill: 'none' };

        return (<g className={containerClassName}>
          <rect
x={bBox.x} y={bBox.y} width={bBox.w} height={bBox.h}
              {...fill}
              className={dropZoneClassName}
              onMouseOver={e => this.onDropZoneActivate(e)}
              onMouseOut={e => this.onDropZoneDeactivate(e)}
              onMouseUp={e => this.onDropZoneMouseUp(e)} />
          {this.props.children}
        </g>);
    }

    onDropZoneActivate(e) {
  			const dragDropManager = this.context.dragDropManager,
      dropTarget = this.props.dropTarget,
      messageManager = this.context.messageManager;
  			if (dragDropManager.isOnDrag()) {
  					if (_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)) {
  							return;
  					}
  					dragDropManager.setActivatedDropTarget(dropTarget,
  							(nodeBeingDragged) => {
  									// IMPORTANT: override node's default validation logic
  									// This drop zone is for statements only.
                                    // Unless it's in a Fork, in that case only Worker are allowed.
      const factory = dropTarget.getFactory();
      const callback = this.props.draggable;
      return callback ? callback(dropTarget, nodeBeingDragged) : factory.isStatement(nodeBeingDragged);
  							},
            );
  					this.setState({ statementDropZoneActivated: true,
      dropZoneDropNotAllowed: !dragDropManager.isAtValidDropTarget() });
  					dragDropManager.once('drop-target-changed', () => {
  							this.setState({ statementDropZoneActivated: false, dropZoneDropNotAllowed: false });
  					});
      e.stopPropagation();
  			} else if (messageManager.isOnDrag()) {
				/**
				 * Hover on a worker declaration while drawing an arrow starting from a worker invocation
				 */
      messageManager.setDestination(dropTarget);
      this.setState({ statementDropZoneActivated: true, dropZoneDropNotAllowed: !messageManager.isAtValidDestination() });
      e.stopPropagation();
  }
  	}

  	onDropZoneDeactivate(e) {
  			const dragDropManager = this.context.dragDropManager,
      dropTarget = this.props.dropTarget,
      messageManager = this.context.messageManager;
  			if (dragDropManager.isOnDrag()) {
  					if (_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)) {
  							dragDropManager.clearActivatedDropTarget();
  							this.setState({ statementDropZoneActivated: false, dropZoneDropNotAllowed: false });
  					}
      e.stopPropagation();
  			} else if (messageManager.isOnDrag()) {
      this.setState({ statementDropZoneActivated: false, dropZoneDropNotAllowed: false });
      messageManager.setDestination(undefined);
      e.stopPropagation();
  }
  	}

  	onDropZoneMouseUp(e) {
      this.setState({ statementDropZoneActivated: false, dropZoneDropNotAllowed: false });
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
    dropTarget: PropTypes.instanceOf(ASTNode).isRequired,
};

StatementContainer.contextTypes = {
	 dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
	 messageManager: PropTypes.instanceOf(MessageManager).isRequired,
};

export default StatementContainer;
