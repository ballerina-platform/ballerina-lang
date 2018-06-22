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
import ToolsOverlay from './tools-overlay';

class HoverGroup extends React.Component {
    constructor() {
        super();
        this.state = {
            content: null,
        };
    }
    componentDidMount() {
        this.props.model.on('render-menu', ({ content }) => {
            this.setState({
                content,
            });
        });
    }
    componentWillUnmount() {
        this.props.model.off('render-menu');
    }
    render() {
        const { model, children } = this.props;
        return (
            <g
                className='hover-group'
                onMouseEnter={() => {
                    this.props.model.viewState.hovered = true;
                    this.props.model.trigger('mouse-enter', {
                        origin: this.props.model,
                    });
                }}
                onMouseLeave={(e) => {
                    this.props.model.viewState.hovered = false;
                    this.props.model.trigger('mouse-leave', {
                        origin: this.props.model,
                    });
                }}
            >
                {children}
                <ToolsOverlay
                    bBox={this.props.model.viewState.bBox}
                >
                    {this.state.content}
                </ToolsOverlay>
            </g>
        );
    }
}

HoverGroup.defaultProps = {

};

export default HoverGroup;
