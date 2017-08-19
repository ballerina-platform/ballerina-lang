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
import MessageManager from './../visitors/message-manager';

/**
 * React component for an arrow.
 *
 * @class Arrow
 * @extends {React.Component}
 */
class Arrow extends React.Component {
    /**
     * Creates an instance of Arrow.
     * @param {any} props React properties.
     * @param {any} context React context.
     * @memberof Arrow
     */
    constructor(props, context) {
        super(props);
        this.state = { enable: true, drawOnMouseMoveFlag: -1 };
        if (this.props.moveWithMessageManager) {
            context.messageManager.setArrowDecorator(this);
        }
    }

    /**
     * Gets the angle of the arrow.
     *
     * @param {Object} start The starting point.
     * @param {Object} end The ending point.
     * @returns {number} Degrees.
     * @memberof Arrow
     */
    getArrowAngle(start, end) {
        const deltaX = end.x - start.x;
        const deltaY = end.y - start.y;
        const rad = Math.atan2(deltaY, deltaX);
        const deg = rad * (180 / Math.PI);

        return deg;
    }

    /**
     * Renders the view for an arrow.
     *
     * @returns {ReactElement} The view.
     * @memberof Arrow
     */
    render() {
        const { start, end, dashed, arrowSize } = this.props;
        const enable = this.props.enable;
        const drawOnMouseMove = this.state.drawOnMouseMoveFlag;
        const messageManager = this.context.messageManager;
        let arrowStart;
        let arrowEnd;

        if (drawOnMouseMove > -1) {
            arrowStart = messageManager.getMessageStart();
            arrowEnd = messageManager.getMessageEnd();
        } else {
            arrowStart = start;
            arrowEnd = end;
        }

        let className = 'action-arrow';
        if (dashed) {
            className = 'action-arrow action-dash-line';
        }
        return (<g >
            {enable &&
            <line
                x1={arrowStart.x}
                x2={arrowEnd.x}
                y1={arrowStart.y}
                y2={arrowEnd.y}
                className={className}
            /> }
            {enable &&
            <polygon
                points={`-${arrowSize},-${arrowSize} 0,0 -${arrowSize},${arrowSize}`}
                transform={`translate(${arrowEnd.x}, ${arrowEnd.y})
                            rotate(${this.getArrowAngle(arrowStart, arrowEnd)}, 0, 0)`}
                className="action-arrow-head"
            />
            }
        </g>);
    }
}

Arrow.contextTypes = {
    messageManager: PropTypes.instanceOf(MessageManager).isRequired,
};

Arrow.propTypes = {
    moveWithMessageManager: PropTypes.bool,
    start: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
    }),
    end: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
    }),
    dashed: PropTypes.bool,
    arrowSize: PropTypes.number,
    enable: PropTypes.bool,
};

Arrow.defaultProps = {
    moveWithMessageManager: false,
    start: undefined,
    end: undefined,
    dashed: false,
    arrowSize: 5,
    enable: false,
};

export default Arrow;
