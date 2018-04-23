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
 *
 */

import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import View from './../../view/view';
import { VIEWS, COMMANDS } from './../constants';
import ExplorerItem from './../components/ExplorerItem';

/**
 * Woprkspace Explorer
 */
class WorkspaceExplorer extends View {

    /**
     * @inheritdoc
     */
    getID() {
        return VIEWS.EXPLORER;
    }

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            goToFilePath: undefined,
        };
        this.onSelectNode = this.onSelectNode.bind(this);
        this.onClickOpenProgramDir = this.onClickOpenProgramDir.bind(this);
        this.onGoToFileInExplorer = this.onGoToFileInExplorer.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        const { command: { on } } = this.context;
        on(COMMANDS.GO_TO_FILE_IN_EXPLORER, this.onGoToFileInExplorer);
    }

    /**
     * @inheritdoc
     */
    componentWillUnmount() {
        const { command: { off } } = this.context;
        off(COMMANDS.GO_TO_FILE_IN_EXPLORER, this.onGoToFileInExplorer);
    }

    /**
     * On Go To File Dispatch
     */
    onGoToFileInExplorer({ filePath }) {
        this.setState({
            goToFilePath: filePath,
        });
    }

    /**
     * On any node in any tree is selected.
     * Enforces a single selection across multiple trees.
     *
     */
    onSelectNode(node) {
        this.props.workspaceManager.goToFilePath = undefined;
        this.props.workspaceManager.onNodeSelectedInExplorer(node);
    }

    /**
     * On click open program dir
     */
    onClickOpenProgramDir() {
        const { command: { dispatch } } = this.props.workspaceManager.appContext;
        dispatch(COMMANDS.SHOW_FOLDER_OPEN_WIZARD);
    }

    /**
     * @inheritdoc
     */
    render() {
        const trees = [];
        const { openedFolders } = this.props.workspaceManager;
        // give precedence to first root folder in explorer
        let foundGoToFileRoot = false;
        openedFolders.forEach((folder) => {
            let activeKey;
            const { goToFilePath } = this.state;
            if (goToFilePath && !foundGoToFileRoot) {
                if (goToFilePath && _.startsWith(goToFilePath, folder.fullPath)) {
                    activeKey = goToFilePath;
                    foundGoToFileRoot = true;
                }
            }
            trees.push((
                <ExplorerItem
                    activeKey={activeKey}
                    folder={folder}
                    key={folder.fullPath}
                    workspaceManager={this.props.workspaceManager}
                    onSelect={this.onSelectNode}
                    panelResizeInProgress={this.props.panelResizeInProgress}
                />
            ));
        });
        return (
            <div className='workspace-explorer'>
                {_.isEmpty(trees) &&
                    <div className='open-folder-btn-wrapper' onClick={this.onClickOpenProgramDir} >
                        <span className='open-folder-button'>
                            <i className='fw fw-folder-open' />Open Project Directory
                        </span>
                    </div>
                }
                {trees}
            </div>
        );
    }
}

WorkspaceExplorer.propTypes = {
    height: PropTypes.number.isRequired,
    workspaceManager: PropTypes.objectOf(Object).isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
};

WorkspaceExplorer.contextTypes = {
    history: PropTypes.shape({
        put: PropTypes.func,
        get: PropTypes.func,
    }).isRequired,
    command: PropTypes.shape({
        on: PropTypes.func,
        dispatch: PropTypes.func,
    }).isRequired,
};

export default WorkspaceExplorer;
