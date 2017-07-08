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
import { blockStatement, statement, actionBox } from '../configs/designer-defaults.js';
import StatementContainer from './statement-container';
import ASTNode from '../ast/node';
import SimpleBBox from '../ast/simple-bounding-box';
import './block-statement-decorator.css';
import ExpressionEditor from '../../expression-editor/expression-editor-utils';
import ActionBox from './action-box';
import DragDropManager from '../tool-palette/drag-drop-manager';
import ActiveArbiter from './active-arbiter';
import Breakpoint from './breakpoint';

const CLASS_MAP = {
    hidden: 'hide-action',
    visible: 'show-action',
    fade: 'delayed-hide-action',
};

/**
 * Wraps other UI elements and provide box with a heading.
 * Enrich elements with a action box and expression editors.
 */
class BlockStatementDecorator extends React.Component {

    /**
     * Initialize the block decorator.
     */
    constructor() {
        super();
        this.state = {
            active: 'hidden',
        };
        this.onDelete = this.onDelete.bind(this);
        this.onBreakpointClick = this.onBreakpointClick.bind(this);
        this.onJumpToCodeLine = this.onJumpToCodeLine.bind(this);
        this.setActionVisibilityFalse = this.setActionVisibility.bind(this, false);
        this.setActionVisibilityTrue = this.setActionVisibility.bind(this, true);
        this.openExpressionEditor = e => this.openEditor(this.props.expression, this.props.editorOptions, e);
        this.openParameterEditor = e => this.openEditor(this.props.parameterEditorOptions.value,
            this.props.parameterEditorOptions, e);
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
     * Removes self on delete button click. Note that model is retried form dropTarget for
     * backward compatibility with old components written when model was not required.
     * @returns {void}
     */
    onDelete() {
        const model = this.props.model || this.props.dropTarget;
        model.remove();
    }
    /**
     * Navigates to codeline in the source view from the design view node
     *
     */
    onJumpToCodeLine() {
        document.getElementsByClassName('view-source-btn')[0].click();
    }

    /**
     * Call-back for when a new value is entered via expression editor.
     */
    onUpdate() {
        // TODO: implement validate logic.
    }

    /**
     * Shows the action box, depending on whether on child element, delays display.
     * @param {boolean} show - Display action box.
     * @param {MouseEvent} e - Mouse move event from moving on to or out of statement.
     */
    setActionVisibility(show, e) {
        if (!this.context.dragDropManager.isOnDrag()) {
            if (show) {
                const isInChildStatement = this.isInFocusableChild(e.target);
                const isFromChildStatement = this.isInFocusableChild(e.relatedTarget);

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
                    if (elm === this.myRoot) {
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

    /**
     * True if the given element is a child of this element that has it's own focus.
     * @private
     * @param {HTMLElement} elmToCheck - child to be checked.
     * @return {boolean} True if child is focusable.
     */
    isInFocusableChild(elmToCheck) {
        const regex = new RegExp('(^|\\s)((compound-)?statement|life-line-group)(\\s|$)');
        let isInStatement = false;
        let elm = elmToCheck;
        while (elm && elm !== this.myRoot && elm.getAttribute) {
            if (regex.test(elm.getAttribute('class'))) {
                isInStatement = true;
            }
            elm = elm.parentNode;
        }
        return isInStatement;
    }

    /**
     * renders an ExpressionEditor in the header space.
     * @param {string} value - Initial value.
     * @param {object} options - options to be sent to ExpressionEditor.
     */
    openEditor(value, options) {
        const packageScope = this.context.environment;
        if (value && options) {
            new ExpressionEditor(
                this.conditionBox,
                this.onUpdate.bind(this),
                options,
                packageScope).render(this.context.getOverlayContainer());
        }
    }

    /**
     * Render breakpoint element.
     * @private
     * @return {XML} React element of the breakpoint.
     */
    renderBreakpointIndicator() {
        const breakpointSize = 14;
        const { bBox } = this.props;
        const breakpointHalf = breakpointSize / 2;
        const pointX = bBox.getRight() - breakpointHalf;
        const pointY = (bBox.y + statement.gutter.v) - breakpointHalf;
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
        const { bBox, title, dropTarget, expression } = this.props;
        const model = this.props.model || dropTarget;

        const titleH = blockStatement.heading.height;
        const titleW = this.props.titleWidth;

        const p1X = bBox.x;
        const p1Y = bBox.y + titleH;
        const p2X = bBox.x + titleW;
        const p2Y = bBox.y + titleH;
        const p3X = bBox.x + titleW + 10;
        const p3Y = bBox.y;

        const stcY = bBox.y + titleH;
        const stcH = bBox.h - titleH;

        const titleX = bBox.x + (titleW / 2);
        const titleY = bBox.y + (titleH / 2);

        const statementContainerBBox = new SimpleBBox(bBox.x, stcY, bBox.w, stcH);

        let expressionX = 0;
        if (expression) {
            expressionX = p3X + statement.padding.left;
        }
        let paramSeparatorX = 0;
        let parameterText = null;
        if (this.props.parameterBbox && this.props.parameterEditorOptions) {
            paramSeparatorX = this.props.parameterBbox.x;
            parameterText = this.props.parameterEditorOptions.value;
        }

        this.conditionBox = new SimpleBBox(bBox.x, bBox.y, bBox.w, titleH);

        const actionBoxBbox = new SimpleBBox(
            bBox.x + ((bBox.w - actionBox.width) / 2),
            bBox.y + titleH + actionBox.padding.top,
            actionBox.width,
            actionBox.height);
        const utilClassName = CLASS_MAP[this.state.active];

        const separatorGapV = titleH / 3;
        return (
            <g
                onMouseOut={this.setActionVisibilityFalse}
                onMouseOver={this.setActionVisibilityTrue}
                ref={(group) => {
                    this.myRoot = group;
                }}
            >
                <rect x={bBox.x} y={bBox.y} width={bBox.w} height={bBox.h} className="background-empty-rect" />
                <rect
                    x={bBox.x}
                    y={bBox.y}
                    width={bBox.w}
                    height={titleH}
                    rx="0"
                    ry="0"
                    className="statement-title-rect"
                    onClick={!parameterText && this.openExpressionEditor}
                />
                <text x={titleX} y={titleY} className="statement-text">{title}</text>

                {expression &&
                <text
                    x={expressionX}
                    y={titleY}
                    className="condition-text"
                    onClick={this.openExpressionEditor}
                >
                    {expression.text}
                </text>}

                {parameterText &&
                <g>
                    <line
                        x1={paramSeparatorX}
                        y1={titleY - separatorGapV}
                        y2={titleY + separatorGapV}
                        x2={paramSeparatorX}
                        className="parameter-separator"
                    />
                    <text
                        x={paramSeparatorX + blockStatement.heading.paramPaddingX}
                        y={titleY}
                        className="condition-text"
                        onClick={this.openParameterEditor}
                    >
                        ( {parameterText} )
                    </text>
                </g>}

                <polyline points={`${p1X},${p1Y} ${p2X},${p2Y} ${p3X},${p3Y}`} className="statement-title-polyline" />
                <StatementContainer
                    bBox={statementContainerBBox}
                    dropTarget={dropTarget}
                    draggable={this.props.draggable}
                >
                    {this.props.children}
                </StatementContainer>
                {this.props.undeletable ||
                <ActionBox
                    bBox={actionBoxBbox}
                    show={this.state.active}
                    isBreakpoint={model.isBreakpoint}
                    onDelete={this.onDelete}
                    onJumptoCodeLine={this.onJumpToCodeLine}
                    onBreakpointClick={this.onBreakpointClick}
                />}
                {
                    <g className={utilClassName}>
                        {this.props.utilities}
                    </g>
                }
                { model.isBreakpoint && this.renderBreakpointIndicator() }
            </g>);
    }
}

BlockStatementDecorator.defaultProps = {
    draggable: null,
    undeletable: false,
    titleWidth: blockStatement.heading.width,
    editorOptions: null,
    parameterEditorOptions: null,
    utilities: null,
    parameterBbox: null,
    expression: null,
};

BlockStatementDecorator.propTypes = {
    draggable: PropTypes.func,
    title: PropTypes.string.isRequired,
    model: PropTypes.instanceOf(ASTNode).isRequired,
    children: PropTypes.arrayOf(React.PropTypes.node).isRequired,
    utilities: PropTypes.element,
    bBox: PropTypes.instanceOf(SimpleBBox).isRequired,
    parameterBbox: PropTypes.instanceOf(SimpleBBox),
    undeletable: PropTypes.bool,
    dropTarget: PropTypes.instanceOf(ASTNode).isRequired,
    titleWidth: PropTypes.number,
    expression: PropTypes.shape({
        text: PropTypes.string,
    }),
    editorOptions: PropTypes.shape({
        propertyType: PropTypes.string,
        key: PropTypes.string,
        model: PropTypes.instanceOf(ASTNode),
        getterMethod: PropTypes.func,
        setterMethod: PropTypes.func,
    }),
    parameterEditorOptions: PropTypes.shape({
        propertyType: PropTypes.string,
        key: PropTypes.string,
        value: PropTypes.string,
        model: PropTypes.instanceOf(ASTNode),
        getterMethod: PropTypes.func,
        setterMethod: PropTypes.func,
    }),
};

BlockStatementDecorator.contextTypes = {
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    activeArbiter: PropTypes.instanceOf(ActiveArbiter).isRequired,
};

export default BlockStatementDecorator;
