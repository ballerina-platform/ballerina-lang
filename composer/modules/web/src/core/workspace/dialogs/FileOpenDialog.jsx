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
import { Form, Button } from 'semantic-ui-react';
import { getUserHome } from 'api-client/api-client';
import ScrollBarsWithContextAPI from './../../view/scroll-bars/ScrollBarsWithContextAPI';
import Dialog from './../../view/Dialog';
import FileTree from './../../view/tree-view/FileTree';
import { exists as checkPathExists } from './../fs-util';

const FILE_TYPE = 'file';
const HISTORY_LAST_ACTIVE_PATH = 'composer.history.workspace.file-open-dialog.last-active-path';

/**
 * File Open Wizard Dialog
 * @extends React.Component
 */
class FileOpenDialog extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        const { history } = props.workspaceManager.appContext.pref;
        const filePath = history.get(HISTORY_LAST_ACTIVE_PATH) || '';
        this.state = {
            error: '',
            selectedNode: undefined,
            filePath,
            showDialog: true,
        };
        this.onFileOpen = this.onFileOpen.bind(this);
        this.onDialogHide = this.onDialogHide.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        if (!this.state.filePath) {
            getUserHome()
                .then((userHome) => {
                    this.setState({
                        filePath: userHome,
                    });
                });
        }
    }

    /**
     * Called when user clicks open
     */
    onFileOpen() {
        const { filePath } = this.state;
        checkPathExists(filePath)
            .then((response) => {
                if (response.exists) {
                    if (response.type === FILE_TYPE) {
                        this.props.workspaceManager.openFile(filePath)
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
                            error: `${filePath} is not a file`,
                        });
                    }
                } else {
                    this.setState({
                        error: `File ${filePath} does not exist.`,
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
    updateState({ error, selectedNode, filePath }) {
        const { history } = this.props.workspaceManager.appContext.pref;
        history.put(HISTORY_LAST_ACTIVE_PATH, filePath);
        this.setState({
            error,
            filePath,
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
                title='Open File'
                actions={
                    <Button
                        primary
                        onClick={this.onFileOpen}
                        disabled={this.state.filePath === ''}
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
                        this.onFileOpen();
                    }}
                >
                    <Form.Group controlId='filePath'>
                        <Form.Input
                            fluid
                            label='File Path'
                            placeholder='eg: /home/user/ballerina-services/routing.bal'
                            value={this.state.filePath}
                            onChange={(evt) => {
                                this.updateState({
                                    error: '',
                                    filePath: evt.target.value,
                                    selectedNode: undefined,
                                });
                            }}
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
                        activeKey={this.state.filePath}
                        onSelect={
                            (node) => {
                                this.updateState({
                                    error: '',
                                    selectedNode: node,
                                    filePath: node.id,
                                });
                            }
                        }
                        onOpen={
                            (node) => {
                                this.updateState({
                                    error: '',
                                    selectedNode: node,
                                    filePath: node.id,
                                });
                                this.onFileOpen();
                            }
                        }
                    />
                </ScrollBarsWithContextAPI>
            </Dialog>
        );
    }
}

FileOpenDialog.propTypes = {
    workspaceManager: PropTypes.objectOf(Object).isRequired,
};

export default FileOpenDialog;
