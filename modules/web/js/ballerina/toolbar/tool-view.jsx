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
import ToolMenuItem from './tool-menu-item';
import './toolbar.css';
import * as callbackFunction from './toolbar-callback-functions';

/**
 * Tool Component which render a tool in toolbar.
 *
 * @class Tool
 * @extends {React.Component}
 */
class ToolView extends React.Component {

    constructor() {
        super();
        this.state = {
            childVisible: 'none',
        };
        this.onClickIcon = this.onClickIcon.bind(this);
    }
    onClickIcon(app, tool, callback) {
        callbackFunction.resetMenu();
        const callbackResponse = callbackFunction[callback](app, tool, this.state.childVisible);
        if (callbackResponse !== undefined) {
            this.setState({ childVisible: callbackResponse.state });
        }
    }
    /**
     * Render tool view.
     *
     * @returns {ReactElement} render tool view.
     *
     * @memberof Tool
     */
    render() {
        const tool = this.props.tool;
        const toolTip = tool.title;
        const children = tool.children;
        const cssClasses = `toolbar-icons ${tool.cssClassOnDiv} `;
        const callBackFunctionForTool = this.props.callBack;
        const app = this.props.app;
        return (
            <div
                className={cssClasses}
                id={`${tool.id}-tool`}
                title={toolTip}
                onClick={() => { this.onClickIcon(app, tool, callBackFunctionForTool); }}
            >
                { tool.cssClass &&
                <i
                    id={`${tool.id}Icon`}
                    className={tool.cssClass}
                />
                }
                { tool.iconImage &&
                <img src={tool.iconImage}
                    id={`${tool.id}Icon`}
                    style={{ width:'13px'}}
                />
                }                
                <i className={tool.getChildrenIcon} />
                <div
                    id={`${tool.id}Menu`}
                    className="dropdown-content"
                    style={{ display: (this.state.childVisible) }}
                    ref={tool.id}
                >
                    <ul className="list">
                        {Object.keys(children).map((element) => {
                            return (<ToolMenuItem
                                key={`${children[element].id}-menuItem`}
                                menuItem={children[element]}
                                callBack={`${children[element].id}Function`}
                                app={app}
                            />);
                        })}
                    </ul>
                </div>
            </div>

        );
    }
}

export default ToolView;
