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
 * React component for a button displayed on the panel header.
 *
 * @class PanelDecoratorButton
 * @extends {React.Component}
 */
class PanelDecoratorButton extends React.Component {

    /**
     * Renders the view of a panel button.
     *
     * @returns {ReactElement} The view.
     * @memberof PanelDecoratorButton
     */
    render() {
        const bBox = this.props.bBox;
        const iconSize = 14;
        return (<g className='panel-header-controls'>
            <rect
                x={bBox.x}
                y={bBox.y}
                height={bBox.height}
                width={bBox.width}
                rx='0'
                ry='0'
                className='panel-header-controls-wrapper'
            />
            <text
                x={bBox.x + 8}
                y={bBox.y + 21}
                width={iconSize}
                height={iconSize}
                className='control'
                fontFamily='font-ballerina'
                fontSize={iconSize}
                className='control-icons'
                onClick={this.props.onClick}
            >{this.props.icon}</text>
        </g>);
    }
}

PanelDecoratorButton.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        width: PropTypes.number.isRequired,
        height: PropTypes.number.isRequired,
    }).isRequired,
    icon: PropTypes.string.isRequired,
    onClick: PropTypes.func.isRequired,
};

export default PanelDecoratorButton;
