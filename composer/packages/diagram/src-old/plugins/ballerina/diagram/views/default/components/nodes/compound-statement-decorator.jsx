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
import breakpointHoc from 'plugins/debugger/views/BreakpointHoc';
import SimpleBBox from 'plugins/ballerina/model/view/simple-bounding-box';
import Node from '../../../../../model/tree/node';
import DropZone from '../../../../../drag-drop/DropZone';
import './compound-statement-decorator.css';
import Breakpoint from '../decorators/breakpoint';
import { getComponentForNodeArray } from './../../../../diagram-util';

const CLASS_MAP = {
    hidden: 'hide-action',
    visible: 'show-action',
    fade: 'delayed-hide-action',
};

/**
 * Wraps other UI elements and provide box with a heading.
 * Enrich elements with a action box and expression editors.
 */
class CompoundStatementDecorator extends React.Component {

    /**
     * Initialize the block decorator.
     */
    constructor() {
        super();
        this.state = {
            active: 'hidden',
        };
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
     * Call-back for when a new value is entered via expression editor.
     */
    onUpdate() {
        // TODO: implement validate logic.
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
        const { model: { viewState } } = this.props;
        const statementBBox = viewState.components['statement-box'];
        const pointY = statementBBox.y - breakpointHalf;
        return (
            <Breakpoint
                x={pointX}
                y={pointY}
                size={breakpointSize}
                isBreakpoint={this.props.isBreakpoint}
                onClick={() => this.props.onBreakpointClick()}
            />
        );
    }

    /**
     * Override the rendering logic.
     * @returns {XML} rendered component.
     */
    render() {
        const { bBox, title, expression, isBreakpoint, isDebugHit } = this.props;
        const model = this.props.model;
        const viewState = model.viewState;
        const titleH = this.context.designer.config.blockStatement.heading.height;
        const titleW = (this.props.titleWidth) ? this.props.titleWidth
            : this.context.designer.config.blockStatement.heading.width;
        const statementBBox = viewState.components['statement-box'];
        const displayExpression = viewState.components.expression;

        const p1X = statementBBox.x;
        const p1Y = statementBBox.y;
        const p2X = statementBBox.x + titleW;
        const p2Y = statementBBox.y + titleH;
        const p3X = statementBBox.x + titleW + 10;
        const p3Y = statementBBox.y;

        const titleX = p1X + this.context.designer.config.statement.padding.left;
        const titleY = p1Y + (titleH / 2);

        let expressionX = 0;
        if (expression) {
            expressionX = p3X + this.context.designer.config.statement.padding.left;
        }
        let paramSeparatorX = 0;
        let parameterText = null;
        if (this.props.parameterBbox && this.props.parameterEditorOptions) {
            paramSeparatorX = this.props.parameterBbox.x;
            parameterText = this.props.parameterEditorOptions.value;
        }

        this.conditionBox = new SimpleBBox(p1X, statementBBox.y, bBox.w, titleH);

        const utilClassName = CLASS_MAP[this.state.active];

        let statementRectClass = 'statement-title-rect';
        if (isDebugHit) {
            statementRectClass = `${statementRectClass} debug-hit`;
        }
        const separatorGapV = titleH / 3;

        let body;
        if (this.props.body.kind === 'ForkJoin') {
            body = getComponentForNodeArray(this.props.body.workers);
        } else {
            body = getComponentForNodeArray(this.props.body);
        }

        return (
            <g
                ref={(group) => {
                    this.myRoot = group;
                }}
            >
                <rect
                    x={statementBBox.x}
                    y={statementBBox.y}
                    width={statementBBox.w}
                    height={statementBBox.h}
                    className='background-empty-rect'
                />
                <DropZone
                    x={statementBBox.x}
                    y={statementBBox.y}
                    width={statementBBox.w}
                    height={statementBBox.h}
                    baseComponent='rect'
                    dropTarget={this.props.body}
                    enableDragBg
                    enableCenterOverlayLine={!this.props.disableDropzoneMiddleLineOverlay}
                />
                <rect
                    x={statementBBox.x}
                    y={statementBBox.y}
                    width={statementBBox.w}
                    height={titleH}
                    rx='0'
                    ry='0'
                    className={statementRectClass}
                />
                <text x={titleX} y={titleY} className='compound-title-text'>{title}</text>

                {expression &&
                    <text
                        x={expressionX}
                        y={titleY}
                        className='compound-condition-text'
                    >
                        {displayExpression.text}
                    </text>
                }
                <g>
                    <rect
                        x={p3X}
                        y={statementBBox.y}
                        width={statementBBox.w - p3X + statementBBox.x}
                        height={titleH}
                        className='invisible-rect'
                    />
                    {expression && <title> {expression.text} </title>}
                </g>
                {parameterText &&
                <g>
                    <line
                        x1={paramSeparatorX}
                        y1={titleY - separatorGapV}
                        y2={titleY + separatorGapV}
                        x2={paramSeparatorX}
                        className='parameter-separator'
                    />
                    <text
                        x={paramSeparatorX + this.context.designer.config.blockStatement.heading.paramPaddingX}
                        y={titleY}
                        className='compound-condition-text'
                    >
                        ( {parameterText} )
                    </text>
                    <rect
                        x={paramSeparatorX}
                        y={statementBBox.y}
                        width={statementBBox.w - paramSeparatorX + statementBBox.x}
                        height={titleH}
                        onClick={this.openParameterEditor}
                        className='invisible-rect'
                    />
                </g>}

                <polyline points={`${p2X},${p2Y} ${p3X},${p3Y}`} className='statement-title-polyline' />

                {
                    <g className={utilClassName}>
                        {this.props.utilities}
                    </g>
                }
                { isBreakpoint && this.renderBreakpointIndicator() }
                {this.props.children}
                {body}
            </g>);
    }
}

CompoundStatementDecorator.defaultProps = {
    draggable: null,
    children: null,
    undeletable: false,
    titleWidth: undefined,
    editorOptions: null,
    parameterEditorOptions: null,
    utilities: null,
    parameterBbox: null,
    expression: null,
    disableButtons: {
        debug: false,
        delete: false,
        jump: false,
    },
    disableDropzoneMiddleLineOverlay: false,
    isDebugHit: false,
};

CompoundStatementDecorator.propTypes = {
    title: PropTypes.string.isRequired,
    model: PropTypes.instanceOf(Node).isRequired,
    children: PropTypes.arrayOf(PropTypes.node),
    utilities: PropTypes.element,
    bBox: PropTypes.instanceOf(SimpleBBox).isRequired,
    parameterBbox: PropTypes.instanceOf(SimpleBBox),
    dropTarget: PropTypes.instanceOf(Node).isRequired,
    titleWidth: PropTypes.number,
    expression: PropTypes.shape({
        text: PropTypes.string,
    }),
    editorOptions: PropTypes.shape({
        propertyType: PropTypes.string,
        key: PropTypes.string,
        model: PropTypes.instanceOf(Node),
        getterMethod: PropTypes.func,
        setterMethod: PropTypes.func,
    }),
    parameterEditorOptions: PropTypes.shape({
        propertyType: PropTypes.string,
        key: PropTypes.string,
        value: PropTypes.string,
        model: PropTypes.instanceOf(Node),
        getterMethod: PropTypes.func,
        setterMethod: PropTypes.func,
    }),
    onBreakpointClick: PropTypes.func.isRequired,
    isBreakpoint: PropTypes.bool.isRequired,
    disableButtons: PropTypes.shape({
        debug: PropTypes.bool.isRequired,
        delete: PropTypes.bool.isRequired,
        jump: PropTypes.bool.isRequired,
    }),
    disableDropzoneMiddleLineOverlay: PropTypes.bool,
    body: PropTypes.instanceOf(Node).isRequired,
    isDebugHit: PropTypes.bool,
};

CompoundStatementDecorator.contextTypes = {
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
    mode: PropTypes.string,
    designer: PropTypes.instanceOf(Object),
};

export default breakpointHoc(CompoundStatementDecorator);
