/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Interaction IconButton component
 */
class IconButton extends React.Component {

    /**
     * Initialize functions and default states
     * @param {*} props component props for default states
     */
    constructor(props) {
        super();
        this.state = { mouseClicked: false, showAlways: props.showAlways };
        this.IconButtonClick = this.IconButtonClick.bind(this);
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
            this.IconButtonClick();
        }
    }

    /**
     * Handles IconButton click event
     */
    IconButtonClick() {
        if (this.props.enableMouseClick) {
            this.setState({ mouseClicked: true, showAlways: true });
        } else {
            this.props.onClick();
        }
    }

    /**
     * render hover IconButton
     * @return {object} IconButton rendering object
     */
    render() {
        const btnX = this.props.IconButtonX;
        const btnY = this.props.IconButtonY;
        const btnRadius = 0;
        const btnIconColor = this.props.IconButtonIconColor;
        const IconOpacity = 1;
        const IconButtonArea = btnRadius + 2 + (btnRadius / 4);
        const topPadding = (-3 * btnRadius) + (btnRadius / 2);
        let menuCss = 'interaction-menu-area-clicked';
        const menuStyle = {
            left: IconButtonArea,
            top: topPadding,
            display: 'block',
            position: 'relative',
            float: 'left' };

        if (this.props.menuOverIconButton) {
            menuStyle.zIndex = 22;
            menuStyle.top = 0;
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
                style={{ position: 'absolute', left: 0, top: 0 }}
            >
                <div>
                    <span
                        onClick={this.props.onClick}
                        title={this.props.tooltip}
                        className={'menu-wrapper'}
                    >
                        <i className={`fw fw-${this.props.icon} fw-lg`} />
                    </span>
                </div>
                <div style={menuStyle}>
                    {this.props.children}
                </div>
            </div>
        );
    }
}

IconButton.propTypes = {
    type: PropTypes.string,
    children: PropTypes.node,
    icon: PropTypes.string,
    IconButtonX: PropTypes.number,
    IconButtonY: PropTypes.number,
    IconButtonRadius: PropTypes.number,
    IconButtonIconColor: PropTypes.string,
    showAlways: PropTypes.bool,
    onClick: PropTypes.func,
    tooltip: PropTypes.string,
    menuOverIconButton: PropTypes.bool,
    enableMouseClick: PropTypes.bool,
};

IconButton.defaultProps = {
    type: 'primary',
    icon: 'add',
    IconButtonX: 20,
    IconButtonY: 20,
    IconButtonRadius: 10,
    hideIconBackground: false,
    showAlways: false,
    onClick: () => {},
    tooltip: '',
    menuOverIconButton: false,
    enableMouseClick: true,
    children: null,
};

IconButton.childContextTypes = {
    menuCloseCallback: PropTypes.func,
};

export default IconButton;
