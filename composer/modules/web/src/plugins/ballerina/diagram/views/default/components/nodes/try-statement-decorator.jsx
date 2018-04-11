/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import breakpointHoc from 'src/plugins/debugger/views/BreakpointHoc';
import SimpleBBox from 'plugins/ballerina/model/view/simple-bounding-box';
import Node from '../../../../../model/tree/node';
import DropZone from '../../../../../drag-drop/DropZone';
import './compound-statement-decorator.css';
import ActionBox from '../decorators/action-box';
import ActiveArbiter from '../decorators/active-arbiter';
import Breakpoint from '../decorators/breakpoint';
import { getComponentForNodeArray } from './../../../../diagram-util';
import FinallyStatementDecorator from './finally-statement-decorator';
import CatchStatementDecorator from './catch-statement-decorator';
import ArrowDecorator from '../decorators/arrow-decorator';

/**
 * Wraps other UI elements and provide box with a heading.
 * Enrich elements with a action box and expression editors.
 */
class TryStatementDecorator extends React.Component {

    /**
     * Initialize the block decorator.
     */
    constructor() {
        super();
        this.state = {
            active: 'hidden',
        };
        this.onDelete = this.onDelete.bind(this);
        this.onJumpToCodeLine = this.onJumpToCodeLine.bind(this);
        this.setActionVisibilityFalse = this.setActionVisibility.bind(this, false);
        this.setActionVisibilityTrue = this.setActionVisibility.bind(this, true);
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
        const model = this.props.model;
        model.remove();
    }
    /**
     * Navigates to codeline in the source view from the design view node
     *
     */
    onJumpToCodeLine() {
        const { editor } = this.context;
        editor.goToSource(this.props.model);
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
        e.stopPropagation();
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
        const { bBox, isBreakpoint, isDebugHit } = this.props;
        const { designer } = this.context;

        const model = this.props.model;
        const viewState = model.viewState;
        const titleH = designer.config.statement.height;
        const titleW = designer.config.compoundStatement.heading.width;
        const statementBBox = viewState.components['statement-box'];
        const gapLeft = viewState.bBox.leftMargin;
        const gapTop = designer.config.compoundStatement.padding.top;

        const finallyStmt = model.finallyBody;
        const blockHeaderHeight = viewState.components['block-header'].h -
                                    designer.config.compoundStatement.padding.top;


        // Defining coordinates of the diagram
        // (x,y)
        // (P1)
        //       |   try   | -------------------|(p4X)
        // (P2Y) |              |               |
        //       |              |(p8)           |
        //       |              |               |---------------[catch]
        //       |            __|__ (p12)       |
        //       |            a = 1;            |
        //       |              |               |
        //       |               (p10)          |
        //       |                              |
        //   (p7)|_____________(P6)_____________| (P5)
        //                      |

        const p1X = bBox.x - gapLeft;
        const p1Y = bBox.y + gapTop;

        const p2Y = p1Y + (titleH / 2);

        const p4X = p1X + gapLeft + statementBBox.w;

        const p8X = bBox.x;
        const p8Y = p2Y + (titleH / 2);

        const p11X = p1X;
        const p11Y = p1Y + (titleH / 2);

        const actionBoxBbox = new SimpleBBox();
        actionBoxBbox.w = (3 * designer.config.actionBox.width) / 4;
        actionBoxBbox.h = designer.config.actionBox.height;
        actionBoxBbox.x = p8X - (actionBoxBbox.w / 2);
        actionBoxBbox.y = p8Y;

        let statementRectClass = 'compound-statement-rect';
        if (isDebugHit) {
            statementRectClass = `${statementRectClass} debug-hit`;
        }

        const body = getComponentForNodeArray(this.props.model.body);
        const disableDeleteForFinally = model.catchBlocks.length <= 0 && model.finallyBody;
        const disableDeleteForCatch = model.catchBlocks.length === 1 && (!model.finallyBody);

        const blockBox = {
            w: statementBBox.w + gapLeft,
            h: statementBBox.h + blockHeaderHeight,
        };

        let onlyFinally = false;
        if (model.catchBlocks.length === 0 && finallyStmt) {
            // if there are no catch blocks, the block box is drawn together for try and catch
            onlyFinally = true;
            blockBox.h += viewState.components['finally-block'].h;
        }

        return (
            <g
                onMouseOut={this.setActionVisibilityFalse}
                onMouseOver={this.setActionVisibilityTrue}
                ref={(group) => {
                    this.myRoot = group;
                }}
            >
                <rect
                    x={p1X}
                    y={p1Y}
                    width={blockBox.w}
                    height={blockBox.h}
                    className={statementRectClass}
                    rx='5'
                    ry='5'
                />
                {onlyFinally &&
                    <line
                        x1={p1X}
                        y1={p1Y + statementBBox.h + blockHeaderHeight}
                        x2={p1X + blockBox.w}
                        y2={p1Y + statementBBox.h + blockHeaderHeight}
                        className={statementRectClass}
                    />
                }
                <text
                    x={p1X + designer.config.compoundStatement.text.padding}
                    y={p2Y}
                    className='statement-title-text-left'
                >try
                </text>
                <DropZone
                    x={p11X}
                    y={p11Y}
                    width={statementBBox.w}
                    height={statementBBox.h}
                    baseComponent='rect'
                    dropTarget={this.props.model.body}
                    enableDragBg
                    enableCenterOverlayLine={!this.props.disableDropzoneMiddleLineOverlay}
                />
                { isBreakpoint && this.renderBreakpointIndicator() }
                {this.props.children}
                {body}
                <ActionBox
                    bBox={actionBoxBbox}
                    show={this.state.active}
                    isBreakpoint={isBreakpoint}
                    onDelete={() => this.onDelete()}
                    onJumptoCodeLine={() => this.onJumpToCodeLine()}
                    onBreakpointClick={() => this.props.onBreakpointClick()}
                    disableButtons={this.props.disableButtons}
                />
                {(() => {
                    if (model.catchBlocks.length > 0) {
                        let connectorEdgeTopX = p4X;
                        let connectorEdgeBottomX = p4X;
                        let connectorEdgeBottomY = model.catchBlocks[0].viewState.bBox.y +
                            model.catchBlocks[0].viewState.bBox.h;
                        if (finallyStmt) {
                            connectorEdgeBottomY +=
                                (model.viewState.components['finally-block'].components['block-header'].h
                                    - designer.config.statement.gutter.h);
                        }
                        const catchComponents = model.catchBlocks.map((catchStmt) => {
                            const setParameter = catchStmt.setParameter.bind(catchStmt);
                            const getParameter = catchStmt.getParameter.bind(catchStmt);
                            const catchEditorOptions = {
                                propertyType: 'text',
                                key: 'Catch Parameter',
                                model: catchStmt,
                                getterMethod: getParameter,
                                setterMethod: setParameter,
                            };
                            const catchExpression = {
                                text: catchStmt.getParameter().getSource(true),
                            };
                            const catchComp = (
                                <g key={catchStmt.id}>
                                    <CatchStatementDecorator
                                        bBox={catchStmt.viewState.bBox}
                                        model={catchStmt}
                                        body={catchStmt}
                                        connectorStartX={connectorEdgeTopX}
                                        expression={catchExpression}
                                        disableButtons={{ delete: disableDeleteForCatch }}
                                        editorOptions={catchEditorOptions}
                                    />
                                    <line
                                        x1={catchStmt.viewState.bBox.x}
                                        y1={connectorEdgeBottomY}
                                        x2={connectorEdgeBottomX}
                                        y2={connectorEdgeBottomY}
                                        className='flowchart-background-empty-rect'
                                    />
                                    <line
                                        x1={catchStmt.viewState.bBox.x}
                                        y1={catchStmt.viewState.bBox.y + catchStmt.viewState.bBox.h}
                                        x2={catchStmt.viewState.bBox.x}
                                        y2={connectorEdgeBottomY}
                                        className='flowchart-background-empty-rect'
                                    />
                                </g>);
                            connectorEdgeTopX = catchStmt.viewState.bBox.x + (titleW / 2);
                            connectorEdgeBottomX = catchStmt.viewState.bBox.x;
                            return catchComp;
                        });
                        let connectorLineComp;
                        if (finallyStmt) {
                            const arrowY = finallyStmt.viewState.bBox.y - designer.config.statement.gutter.h;
                            // for arrow head add 5 when try and finally ends are on same X line
                            // because the start and end x positions overlap
                            const arrowStartX = (statementBBox.w === finallyStmt.viewState.bBox.w) ? p4X + 5 : p4X;
                            connectorLineComp = (
                                <g>
                                    <ArrowDecorator
                                        start={{
                                            x: arrowStartX,
                                            y: arrowY,
                                        }}
                                        end={{
                                            x: finallyStmt.viewState.bBox.x + finallyStmt.viewState.bBox.w,
                                            y: arrowY,
                                        }}
                                        classNameArrow='flowchart-action-arrow'
                                        classNameArrowHead='flowchart-action-arrow-head'
                                    />
                                </g>);
                        } else {
                            connectorLineComp = (
                                <ArrowDecorator
                                    start={{
                                        x: p4X,
                                        y: bBox.y + bBox.h,
                                    }}
                                    end={{
                                        x: bBox.x,
                                        y: bBox.y + bBox.h,
                                    }}
                                    classNameArrow='flowchart-action-arrow'
                                    classNameArrowHead='flowchart-action-arrow-head'
                                />);
                        }
                        return (<g>
                            {connectorLineComp}
                            {<g>{ catchComponents }</g>}
                        </g>);
                    }
                    return (null);
                })()}
                {finallyStmt &&
                <FinallyStatementDecorator
                    bBox={model.viewState.components['finally-block']}
                    model={finallyStmt}
                    body={finallyStmt}
                    disableButtons={{ delete: disableDeleteForFinally }}
                    drawBox={!onlyFinally}
                />}
            </g>);
    }
}

TryStatementDecorator.defaultProps = {
    draggable: null,
    children: null,
    undeletable: false,
    editorOptions: null,
    parameterEditorOptions: null,
    utilities: null,
    parameterBbox: null,
    disableButtons: {
        debug: false,
        delete: false,
        jump: false,
    },
    disableDropzoneMiddleLineOverlay: false,
    isDebugHit: false,
};

TryStatementDecorator.propTypes = {
    model: PropTypes.instanceOf(Node).isRequired,
    children: PropTypes.arrayOf(PropTypes.node),
    bBox: PropTypes.instanceOf(SimpleBBox).isRequired,
    onBreakpointClick: PropTypes.func.isRequired,
    isBreakpoint: PropTypes.bool.isRequired,
    disableButtons: PropTypes.shape({
        debug: PropTypes.bool.isRequired,
        delete: PropTypes.bool.isRequired,
        jump: PropTypes.bool.isRequired,
    }),
    disableDropzoneMiddleLineOverlay: PropTypes.bool,
    isDebugHit: PropTypes.bool,
};

TryStatementDecorator.contextTypes = {
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
    mode: PropTypes.string,
    activeArbiter: PropTypes.instanceOf(ActiveArbiter).isRequired,
    designer: PropTypes.instanceOf(Object),
};

export default breakpointHoc(TryStatementDecorator);
