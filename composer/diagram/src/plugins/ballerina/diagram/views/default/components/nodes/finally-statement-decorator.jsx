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
import breakpointHoc from 'plugins/debugger/views/BreakpointHoc';
import SimpleBBox from 'plugins/ballerina/model/view/simple-bounding-box';
import HoverGroup from 'plugins/ballerina/graphical-editor/controller-utils/hover-group';
import Node from '../../../../../model/tree/node';
import DropZone from '../../../../../drag-drop/DropZone';
import './compound-statement-decorator.css';
import Breakpoint from '../decorators/breakpoint';
import { getComponentForNodeArray } from './../../../../diagram-util';

/**
 * Wraps other UI elements and provide box with a heading.
 * Enrich elements with a action box and expression editors.
 */
class FinallyStatementDecorator extends React.Component {

    /**
     * Initialize the block decorator.
     */
    constructor() {
        super();
        this.state = {
            active: 'hidden',
        };
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
        const { bBox, isBreakpoint, isDebugHit } = this.props;
        const { designer } = this.context;

        const viewState = bBox;
        const titleH = this.context.designer.config.statement.height;
        const statementBBox = viewState.components['statement-box'];
        const gapLeft = viewState.leftMargin;

        // Defining coordinates of the diagram
        // (x,y)
        // (P1)
        //       | finally ---------------------|
        // (P11) |              |               | (statementBox)
        //       |              |(p8)           |
        //       |              |               |
        //       |         true |               |
        //       |            __|__ (p12)       |
        //       |            a = 1;            |
        //       |              |               |
        //       |                              |
        //       |                              |
        //  (P7) |_____________(P6)_____________|(P5)
        //                      |

        const p1X = bBox.x - gapLeft;
        const p1Y = bBox.y; // + gapTop;

        const p2Y = p1Y + (titleH / 2);

        const p8X = bBox.x;
        const p8Y = p2Y + (titleH / 2);

        const p11X = p1X;
        const p11Y = p1Y + (titleH / 2);

        let statementRectClass = 'compound-statement-rect';
        if (isDebugHit) {
            statementRectClass = `${statementRectClass} debug-hit`;
        }

        const body = getComponentForNodeArray(this.props.model);

        const blockBox = {
            w: statementBBox.w + gapLeft,
            h: statementBBox.h + viewState.components['block-header'].h,
        };


        return (
            <g
                ref={(group) => {
                    this.myRoot = group;
                }}
            >
                {this.props.drawBox && <rect
                    x={p1X}
                    y={p1Y}
                    width={blockBox.w}
                    height={blockBox.h}
                    className={statementRectClass}
                    rx='5'
                    ry='5'
                />}
                <text
                    x={p1X + designer.config.compoundStatement.text.padding}
                    y={p2Y}
                    className='statement-title-text-left'
                >finally
                </text>
                <DropZone
                    x={p11X}
                    y={p11Y}
                    width={statementBBox.w}
                    height={statementBBox.h}
                    baseComponent='rect'
                    dropTarget={this.props.model}
                    enableDragBg
                    enableCenterOverlayLine={!this.props.disableDropzoneMiddleLineOverlay}
                />
                { isBreakpoint && this.renderBreakpointIndicator() }
                {this.props.children}
                {body}
                <HoverGroup model={this.props.model} region='actionBox'>
                    <rect
                        x={p8X}
                        y={p8Y - 25}
                        width={50}
                        height={25}
                        className='invisible-rect'
                    />
                </HoverGroup>
                <HoverGroup model={this.props.model} region='main'>
                    <rect
                        x={p8X}
                        y={p8Y}
                        width={50}
                        height={50}
                        className='invisible-rect'
                    />
                </HoverGroup>
            </g>);
    }
}

FinallyStatementDecorator.defaultProps = {
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
    drawBox: true,
};

FinallyStatementDecorator.propTypes = {
    model: PropTypes.instanceOf(Node).isRequired,
    children: PropTypes.arrayOf(PropTypes.node),
    bBox: PropTypes.instanceOf(SimpleBBox).isRequired,
    onBreakpointClick: PropTypes.func.isRequired,
    isBreakpoint: PropTypes.bool.isRequired,
    disableDropzoneMiddleLineOverlay: PropTypes.bool,
    isDebugHit: PropTypes.bool,
    drawBox: PropTypes.bool,
};

FinallyStatementDecorator.contextTypes = {
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
    mode: PropTypes.string,
    designer: PropTypes.instanceOf(Object),
};

export default breakpointHoc(FinallyStatementDecorator);
