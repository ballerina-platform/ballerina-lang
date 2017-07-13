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
import ASTNode from '../ast/node';
import ActionBox from './action-box';
import DragDropManager from '../tool-palette/drag-drop-manager';
import SimpleBBox from './../ast/simple-bounding-box';
import { lifeLine, actionBox } from '../configs/designer-defaults.js';
import MessageManager from './../visitors/message-manager';
import './statement-decorator.css';
import ExpressionEditor from '../../expression-editor/expression-editor-utils';
import Breakpoint from './breakpoint';
import ActiveArbiter from './active-arbiter';

/**
 * Wraps other UI elements and provide box with a heading.
 * Enrich elements with a action box and expression editors.
 */
class StatementDecorator extends React.PureComponent {

    /**
     * Calculate statement box.
     * @param {object} props - New props.
     * @return {SimpleBBox} - Statement box.
     */
    static calculateStatementBox(props) {
        const { viewState } = props;
        const { bBox } = viewState;
        const innerZoneHeight = viewState.components['drop-zone'].h;
        return new SimpleBBox(bBox.x, bBox.y + innerZoneHeight, bBox.w, bBox.h - innerZoneHeight);
    }

    /**
     *
     * @param {object} props - Init props.
     * Initialize the statement decorator.
     */
    constructor(props) {
        super();

        this.startDropZones = this.startDropZones.bind(this);
        this.stopDragZones = this.stopDragZones.bind(this);
        this.onDropZoneActivate = this.onDropZoneActivate.bind(this);
        this.onDropZoneDeactivate = this.onDropZoneDeactivate.bind(this);
        this.setActionVisibilityFalse = this.setActionVisibility.bind(this, false);
        this.setActionVisibilityTrue = this.setActionVisibility.bind(this, true);

        this.state = {
            innerDropZoneActivated: false,
            innerDropZoneDropNotAllowed: false,
            innerDropZoneExist: false,
            active: 'hidden',
            statementBox: StatementDecorator.calculateStatementBox(props),
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
     * Calculate statement box on props change.
     * @param {object} props - Next props.
     */
    componentWillReceiveProps(props) {
        this.setState({ statementBox: StatementDecorator.calculateStatementBox(props) });
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
     * Handles click event of breakpoint, adds/remove breakpoint from the node when click event fired
     *
     */
    onBreakpointClick() {
        const { model } = this.props;
        const { isBreakpoint = false } = model;
        if (isBreakpoint) {
            model.removeBreakpoint();
        } else {
            model.addBreakpoint();
        }
    }

    /**
     * Removes self on delete button click.
     * @returns {void}
     */
    onDelete() {
        this.props.model.remove();
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
     * Navigates to code line in the source view from the design view node
     */
    onJumpToCodeLine() {
        const { viewState: { fullExpression } } = this.props;
        const { editor } = this.context;

        editor.setActiveView('SOURCE_VIEW');
        editor.jumpToLine({ expression: fullExpression });
    }

    /**
     * Call-back for when a new value is entered via expression editor.
     */
    onUpdate() {
        // TODO: implement validate logic.
    }

    /**
     * Shows the action box.
     * @param {boolean} show - Display action box if true or else hide.
     */
    setActionVisibility(show) {
        if (!this.context.dragDropManager.isOnDrag()) {
            if (show) {
                this.context.activeArbiter.readyToActivate(this);
            } else {
                this.context.activeArbiter.readyToDeactivate(this);
            }
        }
    }

    /**
     * renders an ExpressionEditor in the statement box.
     */
    openEditor() {
        const options = this.props.editorOptions;
        const packageScope = this.context.environment;
        if (options) {
            new ExpressionEditor(this.state.statementBox,
                text => this.onUpdate(text), options, packageScope).render(this.context.getOverlayContainer());
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
     * Renders breakpoint indicator
     * @return {XML} Breakpoint react element.
     */
    renderBreakpointIndicator() {
        const breakpointSize = 14;
        const bBox = this.state.statementBox;
        const breakpointHalf = breakpointSize / 2;
        const pointX = bBox.getRight() - breakpointHalf;
        const pointY = bBox.y - breakpointHalf;
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
     * Override the rendering logic.
     * @returns {XML} rendered component.
     */
    render() {
        const { viewState, expression, model } = this.props;
        const bBox = viewState.bBox;
        const innerZoneHeight = viewState.components['drop-zone'].h;

        // calculate the bBox for the statement
        const textX = bBox.x + (bBox.w / 2);
        const textY = this.state.statementBox.y + (this.state.statementBox.h / 2);
        const dropZoneX = bBox.x + ((bBox.w - lifeLine.width) / 2);
        const innerDropZoneActivated = this.state.innerDropZoneActivated;
        const innerDropZoneDropNotAllowed = this.state.innerDropZoneDropNotAllowed;
        const dropZoneClassName = ((!innerDropZoneActivated) ? 'inner-drop-zone' : 'inner-drop-zone active')
            + ((innerDropZoneDropNotAllowed) ? ' block' : '');
        const titleH = bBox.h;

        const fill = this.state.innerDropZoneExist ? {} : { fill: 'none' };
        const actionBoxBbox = new SimpleBBox(
            bBox.x + ((bBox.w - actionBox.width) / 2),
            bBox.y + titleH + actionBox.padding.top,
            actionBox.width,
            actionBox.height);
        let statementRectClass = 'statement-rect';
        if (model.isDebugHit) {
            statementRectClass = `${statementRectClass} debug-hit`;
        }
        let tooltip = null;
        if (viewState.fullExpression !== expression) {
            tooltip = (<title>{this.props.viewState.fullExpression}</title>);
        }

        return (
            <g
                className="statement"
                onMouseOut={this.setActionVisibilityFalse}
                onMouseOver={this.setActionVisibilityTrue}
                ref={(group) => {
                    this.myRoot = group;
                }}
            >
                <rect
                    x={dropZoneX}
                    y={bBox.y}
                    width={lifeLine.width}
                    height={innerZoneHeight}
                    className={dropZoneClassName}
                    {...fill}
                    onMouseOver={this.onDropZoneActivate}
                    onMouseOut={this.onDropZoneDeactivate}
                />
                <rect
                    x={bBox.x}
                    y={this.state.statementBox.y}
                    width={bBox.w}
                    height={this.state.statementBox.h}
                    className={statementRectClass}
                    onClick={e => this.openEditor(e)}
                >
                    {tooltip}
                </rect>
                <g className="statement-body">
                    {tooltip}
                    <text x={textX} y={textY} className="statement-text" onClick={e => this.openEditor(e)}>
                        {expression}
                    </text>
                </g>
                <ActionBox
                    bBox={actionBoxBbox}
                    show={this.state.active}
                    isBreakpoint={model.isBreakpoint}
                    onDelete={() => this.onDelete()}
                    onJumptoCodeLine={() => this.onJumpToCodeLine()}
                    onBreakpointClick={() => this.onBreakpointClick()}
                />
                {model.isBreakpoint && this.renderBreakpointIndicator()}
                {this.props.children}
            </g>);
    }

}

StatementDecorator.defaultProps = {
    editorOptions: null,
    children: null,
};

StatementDecorator.propTypes = {
    viewState: PropTypes.shape({
        bBox: PropTypes.instanceOf(SimpleBBox),
        fullExpression: PropTypes.string,
        components: PropTypes.objectOf(PropTypes.instanceOf(SimpleBBox)),
    }).isRequired,
    children: PropTypes.node,
    model: PropTypes.instanceOf(ASTNode).isRequired,
    expression: PropTypes.string.isRequired,
    editorOptions: PropTypes.shape({
        propertyType: PropTypes.string,
        key: PropTypes.string,
        model: PropTypes.instanceOf(ASTNode),
        getterMethod: PropTypes.func,
        setterMethod: PropTypes.func,
    }),
};

StatementDecorator.contextTypes = {
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    messageManager: PropTypes.instanceOf(MessageManager).isRequired,
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
    activeArbiter: PropTypes.instanceOf(ActiveArbiter).isRequired,
};


export default StatementDecorator;
