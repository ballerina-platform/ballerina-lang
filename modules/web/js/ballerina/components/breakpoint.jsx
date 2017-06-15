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
import ImageUtil from './image-util';
import PropTypes from 'prop-types';

class Breakpoint extends React.Component {

    constructor() {
        super();
        this.state = {
            canShowRemove: false,
        };
    }

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
                className="property-pane-action-button-breakpoint"
                onMouseOver={() => { this.showRemoveBreakpointIcon(true); }}
                onMouseOut={() => { this.showRemoveBreakpointIcon(false); }}
                height={this.props.size}
                width={this.props.size}
                x={this.props.x}
                y={this.props.y}
                onClick={this.props.onClick}
            />
        );
    }
}


Breakpoint.propTypes = {
    x: PropTypes.number.isRequired,
    y: PropTypes.number.isRequired,
    size: PropTypes.number.isRequired,
    isBreakpoint: PropTypes.bool,
    onClick: PropTypes.func,
};

export default Breakpoint;
