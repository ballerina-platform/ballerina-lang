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
import * as callbackFunction from './toolbar-callback-functions';

/**
 * Tool Component which render a tool in toolbar.
 *
 * @class Tool
 * @extends {React.Component}
 */
class ToolMenuItem extends React.Component {
    constructor() {
        super();
        this.onClickMenuItem = this.onClickMenuItem.bind(this);
    }

    onClickMenuItem(app, callback) {
        callbackFunction[callback](app);
    }
    /**
     * Render tool view.
     *
     * @returns {ReactElement} render tool view.
     *
     * @memberof Tool
     */
    render() {
        const item = this.props.menuItem;
        const callBackFunctionForTool = this.props.callBack;
        const app = this.props.app;
        return (
            <li id={item.id}>
                <a onClick={() => { this.onClickMenuItem(app, callBackFunctionForTool); }} className="link">
                    <span> {item.name}</span>
                </a>
            </li>

        );
    }
}

export default ToolMenuItem;
