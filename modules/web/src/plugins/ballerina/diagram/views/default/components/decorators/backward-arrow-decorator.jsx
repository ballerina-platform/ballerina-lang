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
 * React component for a backward arrow.
 *
 * @class BackwardArrow
 * @extends {React.Component}
 */
class BackwardArrow extends React.Component {

    /**
     * Creates an instance of BackwardArrow.
     * @param {Object} props React props.
     * @param {Object} context React context.
     * @memberof BackwardArrow
     */
    constructor(props, context) {
        super(props);
        this.state = { enable: true };
    }

    /**
     * Gets the angle of the arrow.
     * @param {Object} start Starting point of the arrow.
     * @param {Object} end Ending point of the arrow.
     * @returns {number} The degrees.
     * @memberof BackwardArrow
     */
    getArrowAngle(start, end) {
        const deltaX = end.x - start.x;
        const deltaY = end.y - start.y;
        const rad = Math.atan2(deltaY, deltaX);
        const deg = rad * (180 / Math.PI);

        return deg;
    }

    /**
     * Renders the view for a backward arrow.
     * @returns {ReactElement} The view.
     * @memberof BackwardArrow
     */
    render() {
        const { start, end, arrowSize } = this.props;
        const enable = this.props.enable;

        const className = 'action-arrow action-dash-line';
        return (<g >
            {enable && <line
                x1={start.x}
                x2={end.x}
                y1={start.y}
                y2={end.y}
                className={className}
            /> }
            {enable &&
            <polygon
                points={`-${arrowSize},-${arrowSize} 0,0 -${arrowSize},${arrowSize}`}
                transform={`translate(${start.x}, ${end.y})
                            rotate(${this.getArrowAngle(start, end)}, 0, 0)`}
                className='action-arrow-head'
            />
            }
        </g>);
    }
}

BackwardArrow.propTypes = {
    start: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
    }),
    end: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
    }),
    arrowSize: PropTypes.number,
    enable: PropTypes.bool,
};

BackwardArrow.defaultProps = {
    start: undefined,
    end: undefined,
    arrowSize: 5,
    enable: false,
};

export default BackwardArrow;
