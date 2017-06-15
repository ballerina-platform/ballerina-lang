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

class BackwardArrow extends React.Component {
    constructor(props, context) {
        super(props);
        this.state = { enable: true, drawOnMouseMoveFlag: -1 };
        if (this.props.moveWithMessageManager) {
            context.messageManager.setBackwardArrowDecorator(this);
        }
    }
    getArrowAngle(start, end) {
        const deltaX = end.x - start.x;
        const deltaY = end.y - start.y;
        const rad = Math.atan2(deltaY, deltaX);
        const deg = rad * (180 / Math.PI);

        return deg;
    }
    render() {
        const { start, end, dashed, arrowSize } = this.props;
        const enable = this.props.enable;
        const drawOnMouseMove = this.state.drawOnMouseMoveFlag;
        const messageManager = this.context.messageManager;
        let arrowStart,
            arrowEnd;

        if (drawOnMouseMove > -1) {
            arrowStart = messageManager.getMessageStart();
            arrowEnd = messageManager.getMessageEnd();
        } else {
            arrowStart = start;
            arrowEnd = end;
        }

        const className = 'action-arrow action-dash-line';
        return (<g >
            {enable && <line x1={arrowStart.x} x2={arrowEnd.x} y1={arrowStart.y} y2={arrowEnd.y} className={className} /> }
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

BackwardArrow.contextTypes = {
    messageManager: PropTypes.instanceOf(MessageManager).isRequired,
};

BackwardArrow.propTypes = {
    start: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
    }),
    end: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
    }),
};

BackwardArrow.defaultProps = {
    dashed: false,
    arrowSize: 5,
};

export default BackwardArrow;
