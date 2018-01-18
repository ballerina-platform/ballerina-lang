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
     * render hover button
     * @return {object} button rendering object
     */
    render() {
        const btnX = this.props.buttonX;
        const btnY = this.props.buttonY;
        const btnRadius = this.props.buttonRadius;
        const btnColor = this.props.buttonColor;
        const btnIconColor = this.props.buttonIconColor;
        const IconOpacity = this.props.hideIconBackground ? 0 : 1;
        const buttonArea = btnRadius + 2 + (btnRadius / 4);
        const topPadding = (-3 * btnRadius) + (btnRadius / 2);
        const menuStyle = {
            left: buttonArea,
            top: topPadding,
            display: 'block',
            position: 'relative',
            float: 'left' };

        if (this.props.menuOverButton) {
            menuStyle.zIndex = 52;
            menuStyle.top = (-3 * btnRadius) + 10;
            menuStyle.left = 0;
        }

        return (
            <div
                className='interaction-menu-area'
                style={{ left: btnX, top: btnY, '--button-size': buttonArea + 'px' }}
            >
                <div style={{ fontSize: btnRadius }} className='button-panel'>
                    <span
                        className={(this.props.showAlways ? 'button-show-always' : 'button') + ' fw-stack fw-lg'}
                        onClick={this.props.onClick}
                        title={this.props.tooltip}
                    >
                        <i
                            style={{ color: btnColor, opacity: IconOpacity }}
                            className='fw button-background fw-circle fw-stack-2x'
                        />
                        <i style={{ color: btnIconColor }} className={`fw fw-${this.props.icon} fw-stack-1x`} />
                    </span>
                </div>
                <div style={menuStyle}>
                    {this.props.children}
                </div>
            </div>
        );
    }
}

Button.propTypes = {
    children: PropTypes.isRequired,
    icon: PropTypes.string,
    buttonX: PropTypes.number,
    buttonY: PropTypes.number,
    buttonRadius: PropTypes.number,
    buttonColor: PropTypes.string,
    buttonIconColor: PropTypes.string,
    hideIconBackground: PropTypes.bool,
    showAlways: PropTypes.bool,
    onClick: PropTypes.func,
    tooltip: PropTypes.string,
    menuOverButton: PropTypes.bool,
};

Button.defaultProps = {
    icon: 'add',
    buttonX: 20,
    buttonY: 20,
    buttonRadius: 10,
    buttonColor: '#f1772a',
    buttonIconColor: '#ffffff',
    hideIconBackground: false,
    showAlways: false,
    onClick: () => {},
    tooltip: '',
    menuOverButton: false,
};

export default Button;
