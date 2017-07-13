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
import _ from 'lodash';
import { statement, lifeLine } from './../configs/designer-defaults';
import ASTNode from '../ast/node';
import DragDropManager from '../tool-palette/drag-drop-manager';
import './compound-statement-decorator.css';
import SimpleBBox from '../ast/simple-bounding-box';


/**
 * Adds drop zone to a another component (eg: to BlockStatementDecorator).
 */
class CompoundStatementDecorator extends React.Component {

    /**
     * Constructs CompoundStatementDecorator
     */
    constructor() {
        super();

        this.startDropZones = this.startDropZones.bind(this);
        this.stopDragZones = this.stopDragZones.bind(this);
        this.onDropZoneActivate = this.onDropZoneActivate.bind(this);
        this.onDropZoneDeactivate = this.onDropZoneDeactivate.bind(this);

        this.state = {
            innerDropZoneActivated: false,
            innerDropZoneDropNotAllowed: false,
            innerDropZoneExist: false,
            active: 'hidden',
        };
    }

    /**
     * registers drag drop call backs on mount.
     */
    componentDidMount() {
        const { dragDropManager } = this.context;
        dragDropManager.on('drag-start', this.startDropZones);
        dragDropManager.on('drag-stop', this.stopDragZones);
    }

    /**
     * un-registers drag drop call backs on mount.
     */
    componentWillUnmount() {
        const { dragDropManager } = this.context;
        dragDropManager.off('drag-start', this.startDropZones);
        dragDropManager.off('drag-stop', this.stopDragZones);
    }

    /**
     * Activates the drop zone.
     */
    onDropZoneActivate() {
        const dragDropManager = this.context.dragDropManager;
        const dropTarget = this.props.model.getParent();
        const model = this.props.model;
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
                () => dropTarget.getIndexOfChild(model));
            this.setState({
                innerDropZoneActivated: true,
                innerDropZoneDropNotAllowed: !dragDropManager.isAtValidDropTarget(),
            });
            dragDropManager.once('drop-target-changed', function () {
                this.setState({ innerDropZoneActivated: false, innerDropZoneDropNotAllowed: false });
            }, this);
        }
    }

    /**
     * Deactivates the drop zone.
     */
    onDropZoneDeactivate() {
        const dragDropManager = this.context.dragDropManager;
        const dropTarget = this.props.model.getParent();
        if (dragDropManager.isOnDrag()) {
            if (_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)) {
                dragDropManager.clearActivatedDropTarget();
                this.setState({ innerDropZoneActivated: false, innerDropZoneDropNotAllowed: false });
            }
        }
    }

    /**
     * Call back for drop manager.
     */
    startDropZones() {
        this.setState({ innerDropZoneExist: true });
    }

    /**
     * Call back for drop manager.
     */
    stopDragZones() {
        this.setState({ innerDropZoneExist: false });
    }

    /**
     * Override the rendering logic.
     * @returns {XML} rendered component.
     */
    render() {
        const { bBox } = this.props;
        const dropZoneX = bBox.x + ((bBox.w - lifeLine.width) / 2);
        const innerDropZoneActivated = this.state.innerDropZoneActivated;
        const innerDropZoneDropNotAllowed = this.state.innerDropZoneDropNotAllowed;
        const dropZoneClassName = ((!innerDropZoneActivated) ? 'inner-drop-zone' : 'inner-drop-zone active')
                                  + ((innerDropZoneDropNotAllowed) ? ' block' : '');

        const fill = this.state.innerDropZoneExist ? {} : { fill: 'none' };

        return (<g className="compound-statement">
            <rect
                x={dropZoneX}
                y={bBox.y}
                width={lifeLine.width}
                height={statement.gutter.v}
                className={dropZoneClassName}
                {...fill}
                onMouseOver={this.onDropZoneActivate}
                onMouseOut={this.onDropZoneDeactivate}
            />
            {this.props.children}
        </g>);
    }

}
CompoundStatementDecorator.defaultProps = {
    children: null,
};

CompoundStatementDecorator.propTypes = {
    bBox: PropTypes.instanceOf(SimpleBBox).isRequired,
    model: PropTypes.instanceOf(ASTNode).isRequired,
    children: React.PropTypes.oneOfType([
        React.PropTypes.arrayOf(React.PropTypes.node),
        React.PropTypes.node,
    ]),
};

CompoundStatementDecorator.contextTypes = {
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
};

export default CompoundStatementDecorator;
