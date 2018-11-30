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
import { Button } from 'semantic-ui-react';

/**
 * Tool Component which render a tool in toolbar.
 *
 * @class Tool
 * @extends {React.Component}
 */
class ToolItemView extends React.Component {

    constructor() {
        super();
        this.handleClick = this.handleClick.bind(this);
    }

    handleClick() {
        this.props.onToolClick(this.props.id);
    }

    /**
     * Render tool view.
     *
     * @returns {ReactElement} render tool view.
     *
     * @memberof Tool
     */
    render() {
        return (
            <Button
                className={this.props.className}
                id={`${this.props.id}-tool`}
                title={this.props.title}
                onClick={this.handleClick}
                disabled={this.props.disabled}
            >
                { this.props.iconClass &&
                <i
                    id={`${this.props.id}Icon`}
                    className={this.props.iconClass}
                />
                }
                { this.props.iconImagesrc &&
                <img
                    src={this.props.iconImagesrc}
                    id={`${this.props.id}Icon`}
                    style={{ width: '13px' }}
                />
                }
            </Button>

        );
    }
}

export default ToolItemView;
