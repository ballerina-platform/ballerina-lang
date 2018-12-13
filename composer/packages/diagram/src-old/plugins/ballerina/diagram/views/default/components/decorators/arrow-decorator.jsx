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

/**
 * React component for an arrow.
 *
 * @class Arrow
 * @extends {React.Component}
 */
class Arrow extends React.Component {

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
        const { start, end, dashed, arrowSize, description, classNameArrow,
            classNameArrowHead, arrowHeadPosition } = this.props;
        const descriptionX = (start.x + end.x) / 2;
        const descriptionY = start.y - 3;

        let className = classNameArrow;
        if (dashed) {
            className = classNameArrow + ' action-dash-line';
        }

        const arrowHeadX = arrowHeadPosition.x || end.x;
        const arrowHeadY = arrowHeadPosition.y || end.y;

        return (
            <g >
                <text
                    x={descriptionX}
                    y={descriptionY}
                >
                    {description}
                </text>
                <line
                    x1={start.x}
                    x2={end.x}
                    y1={start.y}
                    y2={end.y}
                    className={className}
                />
                <polygon
                    points={`-${arrowSize},-${arrowSize * (7 / 10)} 0,0 -${arrowSize},${arrowSize * (7 / 10)}`}
                    transform={`translate(${arrowHeadX}, ${arrowHeadY})
                                rotate(${this.getArrowAngle(start, end)}, 0, 0)`}
                    className={classNameArrowHead}
                />
            </g>);
    }
}


Arrow.propTypes = {
    start: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
    }).isRequired,
    end: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
    }).isRequired,
    dashed: PropTypes.bool,
    arrowSize: PropTypes.number,
    description: PropTypes.string,
    classNameArrow: PropTypes.string,
    classNameArrowHead: PropTypes.string,
    // This was introduced to place the arrow head in a specific point of the arrow decorator
    // Was used for client responding and worker init arrows. No longer required.
    arrowHeadPosition: PropTypes.shape({
        x: PropTypes.number,
        y: PropTypes.number,
    }),
};

Arrow.defaultProps = {
    dashed: false,
    arrowSize: 5,
    description: '',
    classNameArrow: 'action-arrow',
    classNameArrowHead: 'action-arrow-head',
    arrowHeadPosition: {
        x: undefined,
        y: undefined,
    },
};

export default Arrow;
