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
import './toolbar.css';
import ToolItemView from './tool-item-view';
import ToolSetView from './tool-set';

/**
 * Tool Component which render a tool in toolbar.
 *
 * @class Tool
 * @extends {React.Component}
 */
class DiagramRenderView extends React.Component {

    constructor() {
        super();
        this.app = undefined;
        this.onToolClickHandler = this.onToolClickHandler.bind(this);
    }

    /**
     * Click function of the tool
     * @param app
     * @param tool
     */
    onToolClickHandler(id) {
        this.resetMenu();
        switch (id) {
            case 'defaultView':
                this.app.commandManager.dispatch('diagram-mode-change', 'default');
                break;
            case 'actionView':
                this.app.commandManager.dispatch('diagram-mode-change', 'action');
                break;
            case 'compactView':
                this.app.commandManager.dispatch('diagram-mode-change', 'compact');
                break;
        }
    }

    /**
     * Reset the drop down content
     */
    resetMenu() {
        const dropdowns = document.getElementsByClassName('dropdown-content');
        let i;
        for (i = 0; i < dropdowns.length; i++) {
            dropdowns[i].style.display = 'none';
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
        this.app = this.props.app;
        return (
            <ToolSetView
                id='diagramRenderTools'
                state='block'
            >
                {/* Default View */}
                <ToolItemView
                    id='defaultView'
                    className='btn btn-default toolbar-icons'
                    title='Default View'
                    onToolClick={this.onToolClickHandler}
                    disabled=''
                    iconImagesrc='images/default-view.svg'
                />
                {/* Action View */}
                <ToolItemView
                    id='actionView'
                    className='btn btn-default toolbar-icons'
                    title='Action View'
                    onToolClick={this.onToolClickHandler}
                    disabled=''
                    iconImagesrc='images/action-view.svg'
                />
                {/* Compact View */}
                <ToolItemView
                    id='compactView'
                    className='btn btn-default toolbar-icons'
                    title='Compact View'
                    onToolClick={this.onToolClickHandler}
                    disabled=''
                    iconImagesrc='images/compact-view.svg'
                />
            </ToolSetView>
        );
    }
}

export default DiagramRenderView;
