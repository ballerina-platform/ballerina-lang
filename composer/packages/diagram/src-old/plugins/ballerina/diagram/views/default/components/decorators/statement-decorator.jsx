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
import HoverGroup from 'plugins/ballerina/graphical-editor/controller-utils/hover-group';
import SimpleBBox from './../../../../../model/view/simple-bounding-box';
import './statement-decorator.css';
import Breakpoint from './breakpoint';
import Node from '../../../../../model/tree/node';
import DropZone from '../../../../../drag-drop/DropZone';
import splitVariableDefByLambda from '../../../../../model/lambda-util';
import { getComponentForNodeArray } from '../../../../diagram-util';

/**
 * Wraps other UI elements and provide box with a heading.
 * Enrich elements with a action box and expression editors.
 */
class StatementDecorator extends React.Component {

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
            statementBox: StatementDecorator.calculateStatementBox(props),
        };
    }

    /**
     * Calculate statement box on props change.
     * @param {object} props - Next props.
     */
    componentWillReceiveProps(props) {
        this.setState({ statementBox: StatementDecorator.calculateStatementBox(props) });
    }

    /**
     * Navigates to code line in the source view from the design view node
     */
    onJumpToCodeLine() {
        const { goToSource } = this.context;
        goToSource(this.props.model);
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
        const { viewState, expression, isBreakpoint } = this.props;
        const dropZone = viewState.components['drop-zone'];
        const text = viewState.components.text;

        const textClassName = 'statement-text';

        let tooltip = null;
        if (viewState.fullExpression !== expression) {
            const fullExp = _.trimEnd(this.props.viewState.fullExpression, ';');
            tooltip = (<title>{fullExp}</title>);
        }

        const { lambdas } = splitVariableDefByLambda(this.props.model);
        const bBox = viewState.bBox;
        const hiderTop = viewState.components['statement-box'].y + viewState.components['statement-box'].h + 1;
        let children = [];
        let hiderBottom = hiderTop;
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
                <HoverGroup model={this.props.model} region='main'>
                    <line
                        x1={bBox.getCenterX()}
                        y1={hiderTop}
                        x2={bBox.getCenterX()}
                        y2={hiderBottom}
                        className='life-line-hider'
                    />
                    {children}
                    <DropZone
                        model={this.props.model}
                        x={dropZone.x}
                        y={dropZone.y}
                        width={dropZone.w}
                        height={dropZone.h}
                        baseComponent='rect'
                        dropTarget={this.props.model.parent}
                        dropBefore={this.props.model}
                        renderUponDragStart
                        enableDragBg
                        enableCenterOverlayLine
                    />
                    <g className='statement-body' onClick={e => this.onJumpToCodeLine()}>
                        {tooltip}
                        <text
                            x={text.x}
                            y={text.y}
                            className={textClassName}
                            onClick={e => this.onJumpToCodeLine()}
                        >
                            {_.trimEnd(expression, ';')}
                        </text>
                    </g>
                    {isBreakpoint && this.renderBreakpointIndicator()}
                    {this.props.children}
                    <rect
                        x={bBox.x - 10}
                        y={bBox.y}
                        width={bBox.w + 10}
                        height={bBox.h}
                        fillOpacity={0}
                        cursor='pointer'
                    />
                </HoverGroup>
            </g>
        );
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
    model: PropTypes.instanceOf(Node).isRequired,
    expression: PropTypes.string.isRequired,
    onBreakpointClick: PropTypes.func.isRequired,
    isBreakpoint: PropTypes.bool.isRequired,
};

StatementDecorator.contextTypes = {
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    goToSource: PropTypes.func.isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
    mode: PropTypes.string,
    designer: PropTypes.instanceOf(Object),
};


export default breakpointHoc(StatementDecorator);
