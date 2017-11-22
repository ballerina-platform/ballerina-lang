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
import ImageUtil from '../../../../image-util';

/**
 * React Component for a break point.
 *
 * @class Breakpoint
 * @extends {React.Component}
 */
class Breakpoint extends React.Component {

    /**
     * Creates an instance of Breakpoint.
     * @memberof Breakpoint
     */
    constructor() {
        super();
        this.state = {
            canShowRemove: false,
        };
    }

    /**
     * Show break point on hover.
     *
     * @param {boolean} isMouseOver Whether mouse is hovering over the break point.
     * @memberof Breakpoint
     */
    showRemoveBreakpointIcon(isMouseOver) {
        let canShowRemove;
        if (this.props.isBreakpoint && isMouseOver) {
            canShowRemove = true;
        } else {
            canShowRemove = false;
        }
        this.setState({
            canShowRemove,
        });
    }

    /**
     * Renders the view for a break point.
     *
     * @returns {ReactElement} The view.
     * @memberof Breakpoint
     */
    render() {
        let icon;

        if (this.state.canShowRemove) {
            icon = ImageUtil.getSVGIconString('debug-point-remove');
        } else {
            icon = ImageUtil.getSVGIconString('debug-point');
        }

        return (
            <image
                xlinkHref={icon}
                className='property-pane-action-button-breakpoint'
                onMouseOver={() => { this.showRemoveBreakpointIcon(true); }}
                onMouseOut={() => { this.showRemoveBreakpointIcon(false); }}
                height={this.props.size}
                width={this.props.size}
                x={this.props.x}
                y={this.props.y}
                onClick={this.props.onClick}
            >
                <title>Break point</title> </image>
        );
    }
}

Breakpoint.propTypes = {
    x: PropTypes.number.isRequired,
    y: PropTypes.number.isRequired,
    size: PropTypes.number.isRequired,
    isBreakpoint: PropTypes.bool,
    onClick: PropTypes.func.isRequired,
};

Breakpoint.defaultProps = {
    isBreakpoint: false,
    onClick: () => {},
};

export default Breakpoint;
