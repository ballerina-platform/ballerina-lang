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
        this.renderMenu = this.renderMenu.bind(this);
    }
    componentDidMount() {
        if (typeof this.props.model.on === 'function') {
            this.props.model.on('render-menu', this.renderMenu);
        }
    }
    componentWillReceiveProps(nextProps) {
        if (this.props.model !== nextProps.model) {
            if (typeof this.props.model.off === 'function') {
                this.props.model.off('render-menu', this.renderMenu);
            }

            if (typeof nextProps.model.on === 'function') {
                nextProps.model.on('render-menu', this.renderMenu);
            }
        }
    }
    componentWillUnmount() {
        if (typeof this.props.model.off === 'function') {
            this.props.model.off('render-menu');
        }
    }
    renderMenu({ content, region, origin }) {
        if (region === this.props.region) {
            this.setState({
                content,
            });
        }
    }
    render() {
        const { model, children } = this.props;
        return (
            <g
                className='hover-group'
                onMouseEnter={() => {
                    if (typeof this.props.model.trigger !== 'function') {
                        return;
                    }
                    this.props.model.viewState.hovered = true;
                    this.props.model.viewState.hoveredRegion = this.props.region;
                    this.props.model.trigger('mouse-enter', {
                        origin: this.props.model,
                        region: this.props.region,
                    });
                }}
                onMouseLeave={(e) => {
                    if (typeof this.props.model.trigger !== 'function') {
                        return;
                    }
                    const tagName = e.relatedTarget.tagName;
                    // ignore mouse leave event if it its moving to a html node
                    if (tagName !== 'DIV' && tagName !== 'A' && tagName !== 'SPAN') {
                        this.props.model.viewState.hovered = false;
                        this.props.model.viewState.hoveredRegion = undefined;
                        this.props.model.trigger('mouse-leave', {
                            origin: this.props.model,
                            region: this.props.region,
                        });
                    }
                }}
            >
                {children}
                {this.state.content ? <ToolsOverlay>
                    {this.state.content}
                </ToolsOverlay> : null}
            </g>
        );
    }
}

HoverGroup.defaultProps = {

};

export default HoverGroup;