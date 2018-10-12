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
import PropTypes from 'prop-types';
import { Button, Form } from 'semantic-ui-react';
import { getUserHome } from 'api-client/api-client';
import ScrollBarsWithContextAPI from './../../view/scroll-bars/ScrollBarsWithContextAPI';
import Dialog from './../../view/Dialog';
import FileTree from './../../view/tree-view/FileTree';
import { exists as checkPathExists } from './../fs-util';


const FOLDER_TYPE = 'folder';
const HISTORY_LAST_ACTIVE_PATH = 'composer.history.workspace.folder-open-dialog.last-active-path';

/**
 * Folder Open Wizard Dialog
 * @extends React.Component
 */
class FolderOpenDialog extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        const { history } = props.workspaceManager.appContext.pref;
        const folderPath = history.get(HISTORY_LAST_ACTIVE_PATH) || '';
        this.state = {
            error: '',
            selectedNode: undefined,
            showDialog: true,
            folderPath,
        };
        this.onFolderOpen = this.onFolderOpen.bind(this);
        this.onDialogHide = this.onDialogHide.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        if (!this.state.folderPath) {
            getUserHome()
                .then((userHome) => {
                    this.setState({
                        folderPath: userHome,
                    });
                });
        }
    }

    /**
     * Called when user clicks open
     */
    onFolderOpen() {
        const { folderPath } = this.state;
        checkPathExists(folderPath)
            .then((response) => {
                if (response.exists) {
                    if (response.type === FOLDER_TYPE) {
                        this.props.workspaceManager.openFolder(folderPath)
                            .then(() => {
                                this.setState({
                                    error: '',
                                    showDialog: false,
                                });
                            })
                            .catch((error) => {
                                this.setState({
                                    error,
                                });
                            });
                    } else {
                        this.setState({
                            error: `${folderPath} is not a folder`,
                        });
                    }
                } else {
                    this.setState({
                        error: `Folder ${folderPath} does not exist.`,
                    });
                }
            });
    }

    /**
     * Called when user hides the dialog
     */
    onDialogHide() {
        this.setState({
            error: '',
            showDialog: false,
        });
    }

    /**
     * Update state and history
     *
     * @param {Object} state
     */
    updateState({ error, selectedNode, folderPath }) {
        const { history } = this.props.workspaceManager.appContext.pref;
        history.put(HISTORY_LAST_ACTIVE_PATH, folderPath);
        this.setState({
            error,
            folderPath,
            selectedNode,
        });
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <Dialog
                show={this.state.showDialog}
                title='Open Project Directory'
                actions={
                    <Button
                        primary
                        onClick={this.onFolderOpen}
                        disabled={this.state.folderPath === ''}
                    >
                        Open
                    </Button>
                }
                closeDialog
                onHide={this.onDialogHide}
                error={this.state.error}
            >
                <Form
                    widths='equal'
                    onSubmit={(e) => {
                        this.onFolderOpen();
                    }}
                >
                    <Form.Group controlId='folderPath'>
                        <Form.Input
                            fluid
                            value={this.state.folderPath}
                            label='Location'
                            onChange={(evt) => {
                                this.updateState({
                                    error: '',
                                    folderPath: evt.target.value,
                                    selectedNode: undefined,
                                });
                            }}
                            placeholder='eg: /home/user/ballerina-services/routing'
                        />
                    </Form.Group>
                </Form>
                <ScrollBarsWithContextAPI
                    style={{
                        height: 300,
                    }}
                    autoHide
                >
                    <FileTree
                        activeKey={this.state.folderPath}
                        onSelect={
                            (node) => {
                                this.updateState({
                                    error: '',
                                    selectedNode: node,
                                    folderPath: node.id,
                                });
                            }
                        }
                        onOpen={
                            (node) => {
                                this.updateState({
                                    error: '',
                                    selectedNode: node,
                                    folderPath: node.id,
                                });
                                this.onFolderOpen();
                            }
                        }
                    />
                </ScrollBarsWithContextAPI>
            </Dialog>
        );
    }
}

FolderOpenDialog.propTypes = {
    workspaceManager: PropTypes.objectOf(Object).isRequired,
};

export default FolderOpenDialog;
