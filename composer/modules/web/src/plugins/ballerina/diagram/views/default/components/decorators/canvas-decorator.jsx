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
import CSSTransitionGroup from 'react-transition-group/CSSTransitionGroup';
import PropTypes from 'prop-types';
import Node from '../../../../../model/tree/node';
import DropZone from '../../../../../drag-drop/DropZone';
import './canvas-decorator.css';
import { setCanvasOverlay } from './../../../../../configs/app-context';
import ArrowDecorator from './arrow-decorator';
import BackwardArrowDecorator from './backward-arrow-decorator';

/**
 * React component for a canvas decorator.
 *
 * @class CanvasDecorator
 * @extends {React.Component}
 */
class CanvasDecorator extends React.Component {

    /**
     * Renders view for a canvas.
     *
     * @returns {ReactElement} The view.
     * @memberof CanvasDecorator
     */
    render() {
        const arrowStart = {
            x: 0,
            y: 0,
        };
        const arrowEnd = {
            x: 0,
            y: 0,
        };
        const { fitToWidth } = this.context;
        const svgSize = {
            w: fitToWidth ? this.props.containerSize.width : this.props.bBox.w,
            h: this.props.bBox.h,
        };
        const viewBox = fitToWidth ? `0 0 ${this.props.bBox.w} ${this.props.bBox.h}` : '';
        return (
            <div className='' style={{ width: svgSize.w }} >
                <div ref={(x) => { setCanvasOverlay(x); }}>
                    {/* This space is used to render html elements over svg */ }
                </div>
                {(!fitToWidth) ? this.props.overlay : null }
                <svg
                    className='svg-container'
                    width={svgSize.w}
                    height={svgSize.h}
                    viewBox={viewBox}
                    preserveAspectRatio='xMinYMin'
                    style={{ pointerEvents: fitToWidth ? 'none' : 'auto' }}
                >
                    <DropZone
                        x='0'
                        y='0'
                        width='100%'
                        height='100%'
                        baseComponent='rect'
                        dropTarget={this.props.dropTarget}
                    />
                    {this.props.children}
                    <ArrowDecorator start={arrowStart} end={arrowEnd} enable />
                    <BackwardArrowDecorator start={arrowStart} end={arrowEnd} enable />
                </svg>
            </div>
        );
    }
}

CanvasDecorator.propTypes = {
    containerSize: PropTypes.shape({
        height: PropTypes.number.isRequired,
        width: PropTypes.number.isRequired,
    }).isRequired,
    bBox: PropTypes.shape({
        h: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
    }).isRequired,
    children: PropTypes.node.isRequired,
    dropTarget: PropTypes.instanceOf(Node).isRequired,
    annotations: PropTypes.arrayOf(PropTypes.element),
    overlay: PropTypes.arrayOf(PropTypes.element),
    errorList: PropTypes.arrayOf(PropTypes.element),
};

CanvasDecorator.contextTypes = {
    fitToWidth: PropTypes.bool,
};

CanvasDecorator.defaultProps = {
    annotations: [],
    overlay: null,
    errorList: [],
};
export default CanvasDecorator;
