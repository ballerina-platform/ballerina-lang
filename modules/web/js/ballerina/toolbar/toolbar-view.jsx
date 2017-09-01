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
import _ from 'lodash';
import ReactDOM from 'react-dom';
import './toolbar.css';
import DiagramRenderView from './diagram-render-view';
import FileHandlingView from './file-handling-view';
import UndoRedoView from './undo-redo-view';
import LaunchDebuggerView from './launcher-debugger-view';

class ToolbarView extends React.Component {

    constructor() {
        super();
        this.state = {
            filePath: '',
            file: '',
        };
    }

    /**
     * hook for componentDidMount
     */
    componentDidMount() {
        const app = this.props.app;
        const self = this;
        const tabController = app.tabController;
        tabController.on('active-tab-changed', (evt) => {
            const activeTab = evt.newActiveTab;
            if (_.isFunction(activeTab.getFile)) {
                self.setState({
                    filePath: activeTab.getFile().getPath(),
                    file: activeTab.getFile().getName(),
                });
            } else {
                self.setState({
                    filePath: 'Ballerina Composer',
                    file: activeTab.getTitle(),
                });
            }
        });
        tabController.on('last-tab-removed', () => {
            self.setState({
                filePath: '',
                file: '',
            });
        });
    }

    /**
     * Set path of the file
     * @param path
     * @returns {Array}
     */
    setPath(path) {
        path = _.replace(path, /\\/gi, '/');
        const pathArr = _.split(path, '/');
        return pathArr;
    }

    /**
     * Render tool view.
     *
     * @returns {ReactElement} render tool view.
     *
     * @memberof Tool
     */
    render() {
        const app = this.props.app;
        const pathArr = this.setPath(this.state.filePath);
        let copyPathArr = pathArr;
        let viewFullPath = '';
        if (copyPathArr.length > 3) {
            copyPathArr = copyPathArr.splice(copyPathArr.length - 3);
            viewFullPath = (<li title={[this.state.filePath, this.state.file].join('/')}> <a>...</a></li>);
        }
        return (
            <div className="row">
                <div id="toolbar-palette">
                    <FileHandlingView
                        app={app}
                    />
                    <UndoRedoView
                        app={app}
                    />
                    <LaunchDebuggerView
                        app={app}
                    />
                    <DiagramRenderView
                        app={app}
                    />
                </div>
                <div id="breadcrumb-container">
                    <ul className={app.config.breadcrumbs.cssClass.list}>
                        {viewFullPath}
                        {copyPathArr.map((value) => {
                            return (<li
                                key={value}
                                className={app.config.breadcrumbs.cssClass.item}
                            >
                                <a>{value}</a></li>);
                        })}
                        <li
                            key="fileName"
                            className={[app.config.breadcrumbs.cssClass.item,
                                app.config.breadcrumbs.cssClass.active].join(' ')}
                        >
                            <a>{this.state.file}</a></li>
                    </ul>
                </div>
            </div>
        );
    }
}


export default function renderToolView(app) {
    ReactDOM.render(<ToolbarView
        app={app}
    />, document.getElementById('toolbar-container'));
}
