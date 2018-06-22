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
import { Button } from 'semantic-ui-react';
import ImageUtils from './../diagram/image-util';

class Tools extends React.Component {
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

    render() {
        const { children, size } = this.props;
        return (
            <div style={{
                position: 'relative',
                ...this.props.style,
            }}
            >
                <Button
                    size={size}
                    compact
                    onClick={() => {
                        this.setMenuVisibility(true);
                        this.setButtonVisibility(false);
                    }}
                ><i className='fw'>{ImageUtils.getCodePoint('add')}</i>

                </Button>
                {this.state.isMenuActive ? children : ''}
            </div>
        );
    }
}

Tools.defaultProps = {
    showButtonAlways: false,
    style: {},
};

export default Tools;
