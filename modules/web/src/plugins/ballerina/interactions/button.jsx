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
        const btnX = this.props.buttonX;
        const btnY = this.props.buttonY;
        const btnRadius = this.props.buttonRadius;
        const btnColor = this.props.buttonColor;
        const btnIconColor = this.props.buttonIconColor;
        const IconOpacity = this.props.hideIconBackground ? 0 : 1;
        return (
            <div
                className='button-area'
                style={{ height: bBox.h, width: bBox.w, left: bBox.x, top: bBox.y }}
            >
                <div
                    className='interaction-menu-area'
                    style={{ left: btnX, top: btnY, '--button-size': (btnRadius + 2 + (btnRadius / 4)) + 'px' }}
                >
                    <div style={{ fontSize: btnRadius }} className='button-panel'>
                        <span
                            className={(this.props.showAlways ? 'button-show-always' : 'button') + ' fw-stack fw-lg'}
                            onClick={() => {
                                if (this.props.command) {
                                    this.context.command.dispatch(this.props.command, this.props.commandArgs);
                                }
                            }}
                        >
                            <i
                                style={{ color: btnColor, opacity: IconOpacity }}
                                className='fw button-background fw-circle fw-stack-2x'
                            />
                            <i style={{ color: btnIconColor }} className={`fw fw-${this.props.icon} fw-stack-1x`} />
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
    icon: PropTypes.string,
    bBox: PropTypes.valueOf(PropTypes.object).isRequired,
    buttonX: PropTypes.number,
    buttonY: PropTypes.number,
    buttonRadius: PropTypes.number,
    buttonColor: PropTypes.string,
    buttonIconColor: PropTypes.string,
    hideIconBackground: PropTypes.bool,
    showAlways: PropTypes.bool,
    command: PropTypes.string,
    commandArgs: PropTypes.objectOf(Object),
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
    command: undefined,
    commandArgs: {},
};

Button.contextTypes = {
    command: PropTypes.shape({
        dispatch: PropTypes.func,
    }).isRequired,
};

export default Button;
