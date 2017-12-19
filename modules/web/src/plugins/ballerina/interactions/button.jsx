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
import './button.scss';

class Button extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            actionTriggered: false,
        };
        this.onAddButtonClick = this.onAddButtonClick.bind(this);
        this.handleOutsideClick = this.handleOutsideClick.bind(this);
    }

    /**
     * invoke on component will unmount.
     * */
    componentWillUnmount() {
        document.removeEventListener('mouseup', this.handleOutsideClick, false);
    }

    /**
     * add button handler
     */
    onAddButtonClick() {
        this.setState({ actionTriggered: true });
    }

    /**
     * Handles the outside click event of the drop down
     * @param {Object} e - event object.
     */
    handleOutsideClick(e) {
        alert('test');
    }

    /**
     * draw add typed button
     * @return {object} button rendering object
     */
    drawAddButton() {
        const menuItems = ['If', 'While', 'Send', 'Receive'];
        return (
            <div className='button-area'>
                <nav>
                    <div>
                        <span className='button fw-stack fw-lg' onClick={this.onAddButtonClick}>
                            <i className='fw fw-circle fw-stack-2x' />
                            <i className='fw fw-add fw-stack-1x fw-inverse' />
                        </span>
                    </div>
                    {
                      menuItems.map((item) => {
                          return (<a>{item}</a>);
                      })
                    }
                </nav>
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

export default Button;
