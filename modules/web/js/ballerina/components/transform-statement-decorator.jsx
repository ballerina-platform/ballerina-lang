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
import ActionBox from './action-box';
import DragDropManager from '../tool-palette/drag-drop-manager';
import SimpleBBox from '../ast/simple-bounding-box';
import * as DesignerDefaults from '../configs/designer-defaults';
import './statement-decorator.css';
import ActiveArbiter from './active-arbiter';
import ImageUtil from './image-util';

class TransformStatementDecorator extends React.Component {

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

    componentWillMount() {
        const { designView } = this.context;

        if (designView.getTransformActive()) {
            designView.setTransformActive(true, this.props.model);
        }
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

    onDelete() {
        this.props.model.remove();
    }

    /**
     * Navigates to codeline in the source view from the design view node
     *
     */
    onJumptoCodeLine() {
        const { viewState: { fullExpression } } = this.props;
        const { editor } = this.context;

        editor.setActiveView('SOURCE_VIEW');
        editor.jumpToLine({ expression: fullExpression });
    }

    /**
     * Renders breakpoint indicator
     */
    renderBreakpointIndicator() {
        const breakpointSize = 14;
        const pointX = this.statementBox.x + this.statementBox.w - (breakpointSize / 2);
        const pointY = this.statementBox.y - (breakpointSize / 2);
        return (
            <Breakpoint
                x={pointX}
                y={pointY}
                size={breakpointSize}
                isBreakpoint={this.props.model.isBreakpoint}
                onClick={() => this.onBreakpointClick()}
            />
        );
    }
    /**
     * Handles click event of breakpoint, adds/remove breakpoint from the node when click event fired
     *
     */
    onBreakpointClick() {
        const { model } = this.props;
        const { isBreakpoint = false } = model;
        if (model.isBreakpoint) {
            model.removeBreakpoint();
        } else {
            model.addBreakpoint();
        }
    }

    onExpand() {
        const { designView } = this.context;
        designView.setTransformActive(true, this.props.model);
    }

    render() {
        const { viewState, expression, model } = this.props;
        const bBox = viewState.bBox;
        const innerZoneHeight = viewState.components['drop-zone'].h;

        // calculate the bBox for the statement
        this.statementBox = {};
        this.statementBox.h = bBox.h - innerZoneHeight;
        this.statementBox.y = bBox.y + innerZoneHeight;
        this.statementBox.w = bBox.w;
        this.statementBox.x = bBox.x;
        // we need to draw a drop box above and a statement box
        const text_x = bBox.x + (bBox.w / 2);
        const text_y = this.statementBox.y + (this.statementBox.h / 2);
        const expand_button_x = bBox.x + (bBox.w / 2) + 40;
        const expand_button_y = this.statementBox.y + (this.statementBox.h / 2) - 7;
        const drop_zone_x = bBox.x + (bBox.w - DesignerDefaults.lifeLine.width) / 2;
        const innerDropZoneActivated = this.state.innerDropZoneActivated;
        const innerDropZoneDropNotAllowed = this.state.innerDropZoneDropNotAllowed;
        const dropZoneClassName = ((!innerDropZoneActivated) ? 'inner-drop-zone' : 'inner-drop-zone active')
                                            + ((innerDropZoneDropNotAllowed) ? ' block' : '');

        const actionBbox = new SimpleBBox();
        const fill = this.state.innerDropZoneExist ? {} : { fill: 'none' };
        const iconSize = 14;
        actionBbox.w = DesignerDefaults.actionBox.width;
        actionBbox.h = DesignerDefaults.actionBox.height;
        actionBbox.x = bBox.x + (bBox.w - actionBbox.w) / 2;
        actionBbox.y = bBox.y + bBox.h + DesignerDefaults.actionBox.padding.top;
        let statementRectClass = 'statement-rect';
        if (model.isDebugHit) {
            statementRectClass = `${statementRectClass} debug-hit`;
        }

        return (
            <g
                className="statement"
                onMouseOut={this.setActionVisibility.bind(this, false)}
                onMouseOver={this.setActionVisibility.bind(this, true)}
            >
                <rect
                    x={drop_zone_x}
                    y={bBox.y}
                    width={DesignerDefaults.lifeLine.width}
                    height={innerZoneHeight}
                    className={dropZoneClassName}
                    {...fill}
                    onMouseOver={e => this.onDropZoneActivate(e)}
                    onMouseOut={e => this.onDropZoneDeactivate(e)}
                />
                <rect
                    x={bBox.x}
                    y={this.statementBox.y}
                    width={bBox.w}
                    height={this.statementBox.h}
                    className={statementRectClass}
                    onClick={e => this.onExpand()}
                />
                <g className="statement-body">
                    <text x={text_x} y={text_y} className="transform-action" onClick={e => this.onExpand()}>{expression}</text>
                    <image className="transform-action-icon" x={expand_button_x} y={expand_button_y} width={iconSize} height={iconSize} onClick={e => this.onExpand()} xlinkHref={ImageUtil.getSVGIconString('expand')} />
                </g>
                <ActionBox
                    bBox={actionBbox}
                    show={this.state.active}
                    onDelete={() => this.onDelete()}
                    onJumptoCodeLine={() => this.onJumptoCodeLine()}
                />
                {model.isBreakpoint && this.renderBreakpointIndicator()}
                {this.props.children}
            </g>);
    }

    setActionVisibility(show) {
        if (!this.context.dragDropManager.isOnDrag()) {
            if (show) {
                this.context.activeArbiter.readyToActivate(this);
            } else {
                this.context.activeArbiter.readyToDeactivate(this);
            }
        }
    }
}

TransformStatementDecorator.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
    model: PropTypes.instanceOf(ASTNode).isRequired,
    expression: PropTypes.string.isRequired,
};

TransformStatementDecorator.contextTypes = {
    designView: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
    activeArbiter: PropTypes.instanceOf(ActiveArbiter).isRequired,
};

export default TransformStatementDecorator;
