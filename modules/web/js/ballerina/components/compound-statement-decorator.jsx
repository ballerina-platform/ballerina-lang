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
import { statement } from './../configs/designer-defaults';
import { lifeLine } from './../configs/designer-defaults';
import ASTNode from '../ast/node';
import DragDropManager from '../tool-palette/drag-drop-manager';
import './compound-statement-decorator.css';

class CompoundStatementDecorator extends React.Component {

    constructor(props, context) {
        super(props, context);

        this.startDropZones = this.startDropZones.bind(this);
        this.stopDragZones = this.stopDragZones.bind(this);

        this.state = {
            innerDropZoneActivated: false,
            innerDropZoneDropNotAllowed: false,
            innerDropZoneExist: false,
            active: 'hidden',
        };
    }

    componentDidMount() {
        const { dragDropManager } = this.context;
        dragDropManager.on('drag-start', this.startDropZones);
        dragDropManager.on('drag-stop', this.stopDragZones);
    }

    componentWillUnmount() {
        const { dragDropManager } = this.context;
        dragDropManager.off('drag-start', this.startDropZones);
        dragDropManager.off('drag-stop', this.stopDragZones);
    }

    startDropZones() {
        this.setState({ innerDropZoneExist: true });
    }

    stopDragZones() {
        this.setState({ innerDropZoneExist: false });
    }

    render() {
        const { bBox, model } = this.props;
        // we need to draw a drop box above the statement
        const drop_zone_x = bBox.x + (bBox.w - lifeLine.width) / 2;
        const innerDropZoneActivated = this.state.innerDropZoneActivated;
        const innerDropZoneDropNotAllowed = this.state.innerDropZoneDropNotAllowed;
        const dropZoneClassName = ((!innerDropZoneActivated) ? 'inner-drop-zone' : 'inner-drop-zone active')
    											+ ((innerDropZoneDropNotAllowed) ? ' block' : '');

        const fill = this.state.innerDropZoneExist ? {} : { fill: 'none' };

        return (<g className="compound-statement">
          <rect
            x={drop_zone_x} y={bBox.y} width={lifeLine.width} height={statement.gutter.v}
            className={dropZoneClassName} {...fill}
            onMouseOver={e => this.onDropZoneActivate(e)}
            onMouseOut={e => this.onDropZoneDeactivate(e)}
          />
          {this.props.children}
        </g>);
    }

    onDropZoneActivate(e) {
  			const dragDropManager = this.context.dragDropManager,
  						dropTarget = this.props.model.getParent(),
  						model = this.props.model;
  			if (dragDropManager.isOnDrag()) {
  					if (_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)) {
  							return;
  					}
  					dragDropManager.setActivatedDropTarget(dropTarget,
  							nodeBeingDragged =>
  									// IMPORTANT: override node's default validation logic
  									// This drop zone is for statements only.
  									// Statements should only be allowed here.
  									 model.getFactory().isStatement(nodeBeingDragged),
  							() => dropTarget.getIndexOfChild(model),
  					);
  					this.setState({ innerDropZoneActivated: true,
  							innerDropZoneDropNotAllowed: !dragDropManager.isAtValidDropTarget(),
  					});
  					dragDropManager.once('drop-target-changed', function () {
  							this.setState({ innerDropZoneActivated: false, innerDropZoneDropNotAllowed: false });
  					}, this);
  			}
  	}

  	onDropZoneDeactivate(e) {
  			const dragDropManager = this.context.dragDropManager,
  						dropTarget = this.props.model.getParent();
  			if (dragDropManager.isOnDrag()) {
  					if (_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)) {
  							dragDropManager.clearActivatedDropTarget();
  							this.setState({ innerDropZoneActivated: false, innerDropZoneDropNotAllowed: false });
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
    model: PropTypes.instanceOf(ASTNode).isRequired,
};

CompoundStatementDecorator.contextTypes = {
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    renderingContext: PropTypes.instanceOf(Object).isRequired,
};

export default CompoundStatementDecorator;
