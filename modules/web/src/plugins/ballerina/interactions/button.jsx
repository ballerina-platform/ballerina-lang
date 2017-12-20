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
import './button.scss';

class Button extends React.Component {
    /**
     * draw add typed button
     * @return {object} button rendering object
     */
    drawAddButton() {
        const menuItems = ['If', 'While', 'Send', 'Receive'];
        const width = this.props.width;
        const height = this.props.height;
        const x = this.props.x;
        const y = this.props.y;
        const buttonX = this.props.buttonX;
        const buttonY = this.props.buttonY;
        let buttonClass = 'button';
        if (this.props.showAlways) {
            buttonClass = 'button-show-always';
        }

        return (
            <div
                className='button-area'
                style={{ height, width, left: x, top: y }}
            >
                <div className='interaction-menu-area' style={{ left: buttonX, top: buttonY }}>
                    <div className='button-panel'>
                      <span className={buttonClass + ' fw-stack fw-lg'} onClick={this.onAddButtonClick}>
                          <i className='fw fw-circle fw-stack-2x' />
                          <i className='fw fw-add fw-stack-1x fw-inverse' />
                      </span>
                    </div>
                    <nav className='interaction-menu'>
                        {
                          menuItems.map((item) => {
                              return (<a className='interaction-menu-item'>{item}</a>);
                          })
                        }
                    </nav>
                </div>
            </div>
        );
    }

    /**
     * render hover area and button
     * @return {object} button rendering object
     */
    render() {
        return this.drawAddButton();
    }
}

Button.propTypes = {
    x: PropTypes.number.isRequired,
    y: PropTypes.number.isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
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
