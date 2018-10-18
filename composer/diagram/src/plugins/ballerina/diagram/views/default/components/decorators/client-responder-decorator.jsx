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
import breakpointHoc from 'plugins/debugger/views/BreakpointHoc';
import SimpleBBox from './../../../../../model/view/simple-bounding-box';
import './statement-decorator.css';
import Breakpoint from './breakpoint';
import Node from '../../../../../model/tree/node';
import ArrowDecorator from './arrow-decorator';
import SizingUtils from '../../sizing-util';
import TreeUtil from '../../../../../model/tree-util';

/**
 * Wraps other UI elements and provide box with a heading.
 * Enrich elements with a action box and expression editors.
 */
class ClientResponderDecorator extends React.Component {

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

        this.state = {
            active: 'hidden',
            statementBox: ClientResponderDecorator.calculateStatementBox(props),
        };
    }

    /**
     * Calculate statement box on props change.
     * @param {object} props - Next props.
     */
    componentWillReceiveProps(props) {
        this.setState({ statementBox: ClientResponderDecorator.calculateStatementBox(props) });
    }

    /**
     * Call-back for when a new value is entered via expression editor.
     */
    onUpdate() {
        // TODO: implement validate logic.
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
        const { viewState, model, isBreakpoint } = this.props;
        const { designer } = this.context;

        let expression = viewState.displayParameterText;

        const fullExp = _.trimEnd(this.props.viewState.fullExpression, ';').trim();

        let backwardArrowStart;
        let backwardArrowEnd;

        if (viewState.isClientResponder) {
            if (viewState.components.invocation) {
                backwardArrowStart = Object.assign({}, viewState.components.invocation.end);
                backwardArrowStart.y = viewState.components['statement-box'].y
                    + viewState.components['statement-box'].h - 10;
                backwardArrowEnd = Object.assign({}, viewState.components.invocation.start);
                backwardArrowEnd.y = backwardArrowStart.y;
            }

            if (TreeUtil.isVariableDef(model) ||
                TreeUtil.isAssignment(model) ||
                TreeUtil.isExpressionStatement(model)) {
                let exp;

                if (TreeUtil.isVariableDef(model)) {
                    exp = model.getVariable().getInitialExpression();
                } else {
                    exp = model.getExpression();
                }

                if (TreeUtil.isMatchExpression(exp) || TreeUtil.isCheckExpr(exp)) {
                    exp = exp.getExpression();
                }

                const functionNameWidth = new SizingUtils().getTextWidth(exp.getFunctionName(), 0);
                const nodeWidth = designer.config.clientLine.width + designer.config.lifeLine.gutter.h;

                if (functionNameWidth.w > nodeWidth) {
                    const truncatedFunctionNameWidth = new SizingUtils().getTextWidth(
                        exp.getFunctionName(), 0, nodeWidth);
                    expression = (<tspan>
                        {truncatedFunctionNameWidth.text}
                    </tspan>);
                } else {
                    const displayExpressionWidth = nodeWidth - functionNameWidth.w;
                    const expressionDisplayText = new SizingUtils().getTextWidth(model.viewState.displayParameterText, 0,
                        displayExpressionWidth).text;
                    expression = (<tspan>
                        {exp.getFunctionName()}
                        (<tspan className='client-responder-parameter-text'>{expressionDisplayText}</tspan>)
                    </tspan>);
                }
            }
        }

        return (
            <g
                className='statement'
                ref={(group) => {
                    this.myRoot = group;
                }}
            >
                <g>
                    <text
                        x={viewState.components.invocation.end.x + this.context.designer.config.statement.gutter.h}
                        y={viewState.components.invocation.end.y - 5}
                    >
                        {expression}
                        <title>{fullExp}</title>
                    </text>
                </g>
                <g>
                    <ArrowDecorator
                        start={viewState.components.invocation.start}
                        end={viewState.components.invocation.end}
                    />
                </g>
                {isBreakpoint && this.renderBreakpointIndicator()}
                {this.props.children}
            </g>);
    }

}

ClientResponderDecorator.defaultProps = {
    editorOptions: null,
    children: null,
};

ClientResponderDecorator.propTypes = {
    viewState: PropTypes.shape({
        bBox: PropTypes.instanceOf(SimpleBBox),
        fullExpression: PropTypes.string,
        components: PropTypes.object,
    }).isRequired,
    children: PropTypes.node,
    model: PropTypes.instanceOf(Node).isRequired,
    displayText: PropTypes.string.isRequired,
    onBreakpointClick: PropTypes.func.isRequired,
    isBreakpoint: PropTypes.bool.isRequired,
};

ClientResponderDecorator.contextTypes = {
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
    mode: PropTypes.string,
    designer: PropTypes.instanceOf(Object),
};


export default breakpointHoc(ClientResponderDecorator);
