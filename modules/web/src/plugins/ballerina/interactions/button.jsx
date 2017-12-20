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
import './interaction.scss';

/**
 * Interaction button component
 */
class Button extends React.Component {
    /**
     * render hover area and button
     * @return {object} button rendering object
     */
    render() {
        const bBox = this.props.bBox;
        const buttonX = this.props.buttonX;
        const buttonY = this.props.buttonY;
        return (
            <div
                className='button-area'
                style={{ height: bBox.h, width: bBox.w, left: bBox.x, top: bBox.y }}
            >
                <div className='interaction-menu-area' style={{ left: buttonX, top: buttonY }}>
                    <div className='button-panel'>
                        <span className={(this.props.showAlways ? 'button-show-always' : 'button') + ' fw-stack fw-lg'}>
                            <i className='fw fw-circle fw-stack-2x' />
                            <i className='fw fw-add fw-stack-1x fw-inverse' />
                        </span>
                    </div>
                    {this.props.children}
                </div>
            </div>
        );
    }
}

Button.propTypes = {
    children: PropTypes.isRequired,
    bBox: PropTypes.valueOf(PropTypes.object).isRequired,
    buttonX: PropTypes.number,
    buttonY: PropTypes.number,
    showAlways: PropTypes.bool,
};

Button.defaultProps = {
    buttonX: 20,
    buttonY: 20,
    showAlways: false,
};

export default Button;
