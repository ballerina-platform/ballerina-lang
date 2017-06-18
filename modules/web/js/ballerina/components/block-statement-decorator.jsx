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
import ReactDOM from 'react-dom';
import PropTypes from 'prop-types';
import { blockStatement, statement, actionBox } from '../configs/designer-defaults.js';
import StatementContainer from './statement-container';
import ASTNode from '../ast/node';
import SimpleBBox from '../ast/simple-bounding-box';
import './block-statement-decorator.css';
import ExpressionEditor from 'expression_editor_utils';
import ActionBox from './action-box';
import DragDropManager from '../tool-palette/drag-drop-manager';
import ActiveArbiter from './active-arbiter';
import Breakpoint from './breakpoint';

class BlockStatementDecorator extends React.Component {

    constructor() {
        super();
        this.state = {
            active: 'hidden',
        };
        this.onDelete = this.onDelete.bind(this);
        this.onBreakpointClick = this.onBreakpointClick.bind(this);
        this.onJumptoCodeLine = this.onJumptoCodeLine.bind(this);
    }

    onBreakpointClick() {
        const { model } = this.props;
        const { isBreakpoint = false } = model;
        if (model.isBreakpoint) {
            model.removeBreakpoint();
        } else {
            model.addBreakpoint();
        }
    }

    onDelete() {
        const model = this.props.model || this.props.dropTarget;
        model.remove();
    }
    /**
     * Navigates to codeline in the source view from the design view node
     *
     */
    onJumptoCodeLine() {
        const { renderingContext: { ballerinaFileEditor } } = this.context;

        const container = ballerinaFileEditor._container;
        $(container).find('.view-source-btn').trigger('click');
    }

    onUpdate(text) {
    }

    setActionVisibility(show, e) {
        if (!this.context.dragDropManager.isOnDrag()) {
            const myRoot = ReactDOM.findDOMNode(this);
            if (show) {
                const isInChildStatement = this.isInStatementWithinMe(e.target);
                const isFromChildStatement = this.isInStatementWithinMe(e.relatedTarget);

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
                    if (elm === myRoot) {
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

    isInStatementWithinMe(elmToCheck) {
        const thisNode = ReactDOM.findDOMNode(this);
        const regex = new RegExp('(^|\\s)((compound-)?statement|life-line-group)(\\s|$)');
        let isInStatement = false;
        let elm = elmToCheck;
        while (elm && elm !== thisNode && elm.getAttribute) {
            if (regex.test(elm.getAttribute('class'))) {
                isInStatement = true;
            }
            elm = elm.parentNode;
        }
        return isInStatement;
    }

    openExpressionEditor(value, options, e) {
        const packageScope = this.context.renderingContext.packagedScopedEnvironemnt;
        if (value && options) {
            new ExpressionEditor(this.conditionBox, this.context.container, text => this.onUpdate(text), options, packageScope);
        }
    }

    renderBreakpointIndicator() {
        const breakpointSize = 14;
        const { bBox } = this.props;
        const pointX = bBox.x + bBox.w - breakpointSize / 2;
        const pointY = bBox.y - breakpointSize / 2 + statement.gutter.v;
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

    render() {
        const { bBox, title, dropTarget, expression } = this.props;
        const model = this.props.model || dropTarget;

        const title_h = blockStatement.heading.height;
        let title_w = blockStatement.heading.width;

        // If Available get the title width from given props.
        if (this.props.titleWidth) {
            title_w = this.props.titleWidth;
        }

        const p1_x = bBox.x;
        const p1_y = bBox.y + title_h;
        const p2_x = bBox.x + title_w;
        const p2_y = bBox.y + title_h;
        const p3_x = bBox.x + title_w + 10;
        const p3_y = bBox.y;

        const stc_y = bBox.y + title_h;
        const stc_h = bBox.h - title_h;

        const title_x = bBox.x + title_w / 2;
        const title_y = bBox.y + title_h / 2;

        const statementContainerBBox = new SimpleBBox(bBox.x, stc_y, bBox.w, stc_h);

        let expression_x = 0;
        if (expression) {
            expression_x = p3_x + statement.padding.left;
        }
        let paramSeparator_x = 0;
        let parameterText = null;
        if (this.props.parameterBbox && this.props.parameterEditorOptions) {
            paramSeparator_x = this.props.parameterBbox.x;
            parameterText = this.props.parameterEditorOptions.value;
        }

        this.conditionBox = new SimpleBBox(bBox.x, bBox.y, bBox.w, title_h);

        const actionBoxBbox = {
            x: bBox.x + (bBox.w - actionBox.width) / 2,
            y: bBox.y + title_h + actionBox.padding.top,
            w: actionBox.width,
            h: actionBox.height,
        };

        const utilClassName = this.state.active === 'hidden' ? 'hide-action' :
            (this.state.active === 'visible' ? 'show-action' : 'delayed-hide-action');

        const expressionEditor = this.openExpressionEditor.bind(this, this.props.expression, this.props.editorOptions);
        const paramEditor = this.openExpressionEditor.bind(this, parameterText, this.props.parameterEditorOptions);
        return (<g
            onMouseOut={this.setActionVisibility.bind(this, false)}
            onMouseOver={this.setActionVisibility.bind(this, true)}
        >
            <rect x={bBox.x} y={bBox.y} width={bBox.w} height={bBox.h} className="background-empty-rect" />
            <rect
                x={bBox.x} y={bBox.y} width={bBox.w} height={title_h} rx="0" ry="0" className="statement-title-rect"
                onClick={!parameterText && expressionEditor}
            />
            <text x={title_x} y={title_y} className="statement-text">{title}</text>

            {(expression) &&
            <text
                x={expression_x} y={title_y} className="condition-text"
                onClick={expressionEditor}
            >
                {expression.text}
            </text>}

            {parameterText &&
            <g>
                <line
                    x1={paramSeparator_x} y1={title_y - title_h / 3} y2={title_y + title_h / 3}
                    x2={paramSeparator_x}
                    className="parameter-separator"
                />
                <text
                    x={paramSeparator_x + blockStatement.heading.paramPaddingX} y={title_y} className="condition-text"
                    onClick={paramEditor}
                >
                    ( {parameterText} )
                </text>
            </g>}

            <polyline points={`${p1_x},${p1_y} ${p2_x},${p2_y} ${p3_x},${p3_y}`} className="statement-title-polyline" />
            <StatementContainer bBox={statementContainerBBox} dropTarget={dropTarget} draggable={this.props.draggable}>
                {this.props.children}
            </StatementContainer>
            {this.props.undeletable ||
            <ActionBox
                bBox={actionBoxBbox}
                show={this.state.active}
                isBreakpoint={model.isBreakpoint}
                onDelete={this.onDelete}
                onJumptoCodeLine={this.onJumptoCodeLine}
                onBreakpointClick={this.onBreakpointClick}
            />}
            {
                <g className={utilClassName}>
                    {this.props.utilities || null}
                </g>
            }
            { model.isBreakpoint && this.renderBreakpointIndicator() }
        </g>);
    }
}

BlockStatementDecorator.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
    dropTarget: PropTypes.instanceOf(ASTNode).isRequired,
};

BlockStatementDecorator.contextTypes = {
    container: PropTypes.instanceOf(Object).isRequired,
    renderingContext: PropTypes.instanceOf(Object).isRequired,
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    activeArbiter: PropTypes.instanceOf(ActiveArbiter).isRequired,
};

export default BlockStatementDecorator;
