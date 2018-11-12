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
import TreeUtil from '../../../../../model/tree-util';
import SizingUtils from '../../sizing-util';
import splitVariableDefByLambda from '../../../../../model/lambda-util';
import { getComponentForNodeArray } from '../../../../diagram-util';

/**
 * Wraps other UI elements and provide box with a heading.
 * Enrich elements with a action box and expression editors.
 */
class InvocationDecorator extends React.Component {

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
            statementBox: InvocationDecorator.calculateStatementBox(props),
        };
    }

    /**
     * Calculate statement box on props change.
     * @param {object} props - Next props.
     */
    componentWillReceiveProps(props) {
        this.setState({ statementBox: InvocationDecorator.calculateStatementBox(props) });
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
        const statementBox = viewState.components['statement-box'];
        const { designer } = this.context;

        let expression = viewState.parameterText;
        let tooltip = null;

        if (viewState.fullExpression !== expression) {
            const fullExp = _.trimEnd(this.props.viewState.fullExpression, ';');
            tooltip = (<title>{fullExp}</title>);
        }

        let dropDownItems;
        const dropDownItemMeta = [];
        const backwardArrowStart = {};
        const backwardArrowEnd = {};

        if (viewState.isActionInvocation) {
            // TODO: Need to remove the unique by filter whne the lang server item resolver is implemented
            dropDownItems = _.uniqBy(TreeUtil.getAllVisibleEndpoints(this.props.model.parent), (item) => {
                return item.name.value;
            });
            dropDownItems.forEach((item) => {
                const meta = {
                    text: _.get(item, 'variable.name.value'),
                    callback: (newEp) => {
                        TreeUtil.changeInvocationEndpoint(this.props.model, newEp);
                        this.props.model.trigger('tree-modified', {
                            origin: this.props.model,
                            type: 'invocation-endpoint-change',
                            title: 'Change Target Endpoint',
                            data: {
                                node: this.props.model,
                            },
                        });
                    },
                };
                dropDownItemMeta.push(meta);
            });

            if (viewState.components.invocation) {
                backwardArrowStart.x = viewState.components.invocation.end.x;
                backwardArrowStart.y = viewState.components.invocation.end.y
                                + designer.config.actionInvocationStatement.timelineHeight;
                backwardArrowEnd.x = statementBox.x + (designer.config.actionInvocationStatement.width / 2);
                backwardArrowEnd.y = backwardArrowStart.y;

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
                    const nodeWidth = viewState.components.text.x -
                        (designer.config.statement.padding.left + designer.config.statement.padding.right);

                    if (functionNameWidth.w > nodeWidth) {
                        const truncatedFunctionNameWidth = new SizingUtils().getTextWidth(
                            exp.getFunctionName(), 0, nodeWidth);

                        expression = (<tspan>
                            {truncatedFunctionNameWidth.text}
                        </tspan>);
                    } else {
                        const displayExpressionWidth = nodeWidth - functionNameWidth.w;
                        const expressionDisplayText = new SizingUtils().getTextWidth(model.viewState.parameterText, 0,
                            displayExpressionWidth).text;

                        expression = (<tspan>
                            {exp.getFunctionName()}
                            (<tspan className='client-responder-parameter-text'>{expressionDisplayText}</tspan>)
                        </tspan>);
                    }
                }
            }
        }

        const { lambdas } = splitVariableDefByLambda(this.props.model);
        const bBox = viewState.bBox;
        const hiderTop = viewState.components['statement-box'].y + viewState.components['statement-box'].h + 1;
        let children = [];
        let hiderBottom = hiderTop;
        const invocationComponent = viewState.components.invocation ?
                            viewState.components.invocation : { start: { x: 0, y: 0 }, end: { x: 0, y: 0 } };
        if (lambdas.length) {
            children = getComponentForNodeArray(lambdas);
            hiderBottom = lambdas[lambdas.length - 1].viewState.bBox.getBottom();
        }

        return (
            <g
                className='statement'
                ref={(group) => {
                    this.myRoot = group;
                }}
            >
                <line
                    x1={bBox.getCenterX()}
                    y1={hiderTop}
                    x2={bBox.getCenterX()}
                    y2={hiderBottom}
                    className='life-line-hider'
                />
                { children }
                <text
                    x={statementBox.x +
                        (designer.config.actionInvocationStatement.width / 2) +
                        designer.config.statement.gutter.h}
                    y={invocationComponent.start.y - 5}
                >
                    {expression}
                </text>
                <rect
                    x={invocationComponent.end.x}
                    y={invocationComponent.end.y}
                    width={designer.config.actionInvocationStatement.width}
                    height={designer.config.actionInvocationStatement.timelineHeight}
                    className='action-invocation-statement-rect'
                >
                    {tooltip}
                </rect>
                <g>
                    <ArrowDecorator
                        start={invocationComponent.start}
                        end={invocationComponent.end}
                    />
                    { !viewState.async &&
                    <ArrowDecorator
                        start={backwardArrowStart}
                        end={backwardArrowEnd}
                        dashed
                    />
                    }
                </g>
                {isBreakpoint && this.renderBreakpointIndicator()}
                {this.props.children}
            </g>);
    }

}

InvocationDecorator.defaultProps = {
    editorOptions: null,
    children: null,
};

InvocationDecorator.propTypes = {
    viewState: PropTypes.shape({
        bBox: PropTypes.instanceOf(SimpleBBox),
        fullExpression: PropTypes.string,
        components: PropTypes.objectOf(PropTypes.instanceOf(SimpleBBox)),
    }).isRequired,
    children: PropTypes.node,
    model: PropTypes.instanceOf(Node).isRequired,
    expression: PropTypes.string.isRequired,
    onBreakpointClick: PropTypes.func.isRequired,
    isBreakpoint: PropTypes.bool.isRequired,
};

InvocationDecorator.contextTypes = {
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
    mode: PropTypes.string,
    designer: PropTypes.instanceOf(Object),
};


export default breakpointHoc(InvocationDecorator);
