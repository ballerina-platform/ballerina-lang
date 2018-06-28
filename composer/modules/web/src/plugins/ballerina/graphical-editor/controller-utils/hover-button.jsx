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
import { Button, Menu, Popup, Transition } from 'semantic-ui-react';
import ImageUtils from 'plugins/ballerina/diagram/image-util';

class HoverButton extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            isMenuActive: false,
        };
    }

    setMenuVisibility(active = false) {
        this.setState({
            isMenuActive: active,
        });
    }
    handleRef(ref) {
        this.triggerRef = ref;
    }
    render() {
        const { children, size } = this.props;
        const buttonSize = 30;
        const style = {
            position: 'absolute',
            ...this.props.style,
            left: this.props.style.left - (buttonSize / 2),
        };
        return (
            <div style={style} ref={ref => this.handleRef(ref)}>
                <Button
                    size={size}
                    compact
                    onClick={() => {
                        this.setMenuVisibility(true);
                    }}
                    
                ><i className='fw'>{ImageUtils.getCodePoint('add')}</i>
                </Button>
                <Transition visible={this.state.isMenuActive} animation='scale' duration={200}>
                    <Popup
                        open={this.state.isMenuActive}
                        on='click'
                        context={this.triggerRef}
                        hoverable
                        basic
                        hideOnScroll
                        position='top left'
                        horizontalOffset={-15}
                        verticalOffset={-25}
                        onClose={() => {
                            this.setMenuVisibility(false);
                        }}
                        style={{
                            padding: 0,
                        }}
                    >
                        <Menu vertical>
                            {children}
                        </Menu>
                    </Popup>
                </Transition>
            </div>
        );
    }
}

HoverButton.defaultProps = {
    style: {},
};

export default HoverButton;
