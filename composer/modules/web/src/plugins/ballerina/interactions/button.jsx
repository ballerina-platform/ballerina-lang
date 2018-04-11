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
     * Initialize functions and default states
     * @param {*} props component props for default states
     */
    constructor(props) {
        super();
        this.state = { mouseClicked: false, showAlways: props.showAlways };
        this.buttonClick = this.buttonClick.bind(this);
        this.setWrapperRef = this.setWrapperRef.bind(this);
        this.handleClickOutside = this.handleClickOutside.bind(this);
    }

    /**
     * Include menu close call back to the context
     * @return {object} context object
     */
    getChildContext() {
        return ({ menuCloseCallback:
            () => { this.setState({ mouseClicked: false, showAlways: this.props.showAlways }); } });
    }

    /**
     * Bind mouse down event to handle out side click events
     */
    componentDidMount() {
        if (this.props.enableMouseClick) {
            document.addEventListener('mousedown', this.handleClickOutside);
        }
    }
    /**
     * remove mouse down event bound  when mounting component
     */
    componentWillUnmount() {
        if (this.props.enableMouseClick) {
            document.removeEventListener('mousedown', this.handleClickOutside);
        }
    }

    /**
     * Set specified node as wrapper reference for check outside clicks
     * @param {*} node node click event needs to be detected
     */
    setWrapperRef(node) {
        this.wrapperRef = node;
    }

    /**
     * Handle outside click event and close menu accordingly
     * @param {*} event triggerred event
     */
    handleClickOutside(event) {
        if (this.wrapperRef && !this.wrapperRef.contains(event.target)) {
            this.setState({ mouseClicked: false, showAlways: this.props.showAlways });
        } else {
            this.buttonClick();
        }
    }

    /**
     * Handles button click event
     */
    buttonClick() {
        if (this.props.enableMouseClick) {
            this.setState({ mouseClicked: true, showAlways: true });
        } else {
            this.props.onClick();
        }
    }

    /**
     * render hover button
     * @return {object} button rendering object
     */
    render() {
        const btnX = this.props.buttonX;
        const btnY = this.props.buttonY;
        const btnRadius = this.props.buttonRadius;
        const btnIconColor = this.props.buttonIconColor;
        let IconOpacity = 1;
        const buttonArea = btnRadius + 2 + (btnRadius / 4);
        const topPadding = (-3 * btnRadius) + (btnRadius / 2);
        let menuCss = 'interaction-menu-area';
        const menuStyle = {
            left: buttonArea,
            top: topPadding,
            display: 'block',
            position: 'relative',
            float: 'left' };

        let buttonBackgroundClass = 'button-background';

        if (this.props.type === 'secondary') {
            buttonBackgroundClass = 'button-background-secondary right-arrow';
        } if (this.props.type === 'default') {
            buttonBackgroundClass = 'button-background-default';
            IconOpacity = 0;
        }

        if (this.props.menuOverButton) {
            menuStyle.zIndex = 22;
            menuStyle.top = (-3 * btnRadius) + 10;
            menuStyle.left = 0;
        }

        if (this.props.enableMouseClick) {
            if (this.state.mouseClicked) {
                menuCss = 'interaction-menu-area-clicked';
            } else {
                menuCss = 'interaction-menu-area-hidden';
            }
        }

        return (
            <div
                ref={this.setWrapperRef}
                className={menuCss}
                style={{ position: 'absolute', left: btnX, top: btnY, '--button-size': buttonArea + 'px' }}
            >
                <div className='button-panel'>
                    <span
                        onClick={this.props.onClick}
                        className={(this.state.showAlways ? 'button-show-always' : 'button') + ' fw-lg ' + buttonBackgroundClass}
                        title={this.props.tooltip}
                    >
                        <i className={`fw fw-${this.props.icon} fw-lg button-icon`} />
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
    type: PropTypes.string,
    children: PropTypes.node,
    icon: PropTypes.string,
    buttonX: PropTypes.number,
    buttonY: PropTypes.number,
    buttonRadius: PropTypes.number,
    buttonIconColor: PropTypes.string,
    showAlways: PropTypes.bool,
    onClick: PropTypes.func,
    tooltip: PropTypes.string,
    menuOverButton: PropTypes.bool,
    enableMouseClick: PropTypes.bool,
};

Button.defaultProps = {
    type: 'primary',
    icon: 'add',
    buttonX: 20,
    buttonY: 20,
    buttonRadius: 10,
    hideIconBackground: false,
    showAlways: false,
    onClick: () => {},
    tooltip: '',
    menuOverButton: false,
    enableMouseClick: true,
    children: null,
};

Button.childContextTypes = {
    menuCloseCallback: PropTypes.func,
};

export default Button;
