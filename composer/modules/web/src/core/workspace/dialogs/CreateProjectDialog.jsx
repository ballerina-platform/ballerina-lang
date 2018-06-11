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
import { getPathSeperator, getUserHome } from 'api-client/api-client';
import PropTypes from 'prop-types';
import { Button, Form } from 'semantic-ui-react';
import ScrollBarsWithContextAPI from './../../view/scroll-bars/ScrollBarsWithContextAPI';
import Dialog from './../../view/Dialog';
import FileTree from './../../view/tree-view/FileTree';
import { create, exists as checkFileExists, createProject } from './../fs-util';
import { isOnElectron } from './../../utils/client-info';

const HISTORY_LAST_ACTIVE_PATH = 'composer.history.workspace.create-project-dialog.last-active-path';

// TODO: move this to a global level config
const BALLERINA_INIT_FILE_NAME = 'hello_service.bal';

/**
 * Create Project Wizard Dialog
 * @extends React.Component
 */
class CreateProjectDialog extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        const { history } = props.workspaceManager.appContext.pref;
        const dirPath = history.get(HISTORY_LAST_ACTIVE_PATH) || '';
        this.state = {
            error: '',
            dirPath,
            projectName: '',
            showDialog: true,
        };
        this.onFileSave = this.onFileSave.bind(this);
        this.onDialogHide = this.onDialogHide.bind(this);
    }

    /**
    * @inheritdoc
    */
    componentDidMount() {
        if (!this.state.dirPath) {
            getUserHome()
                .then((userHome) => {
                    this.setState({
                        dirPath: userHome,
                    });
                });
        }
    }

    /**
     * Called when user clicks open
     */
    onFileSave() {
        const { dirPath, projectName } = this.state;
        if (projectName === '') {
            this.setState({
                error: 'Project name cannot be empty',
            });
            return;
        }
        if (dirPath === '') {
            this.setState({
                error: 'Project path cannot be empty',
            });
            return;
        }
        const derivedDirPath = !_.endsWith(dirPath, getPathSeperator())
            ? dirPath + getPathSeperator() : dirPath;
        const newDirPath = derivedDirPath + projectName;
        const createProjectIfNotExist = () => {
            create(newDirPath, 'folder')
                .then((success) => {
                    createProject(newDirPath).then(() => {
                        this.setState({
                            error: '',
                            showDialog: false,
                        });

                        this.props.workspaceManager.openFolder(newDirPath)
                            .then(() => {
                                this.setState({
                                    error: '',
                                    showDialog: false,
                                });
                                this.props.onSaveSuccess();
                            })
                            .catch((error) => {
                                this.setState({
                                    error,
                                });
                            });
                        this.props.workspaceManager.openFile(newDirPath +
                            getPathSeperator() + BALLERINA_INIT_FILE_NAME);
                    });

                })
                .catch((error) => {
                    this.setState({
                        error: error.message,
                    });
                    this.props.onSaveFail();
                });
        };

        checkFileExists(newDirPath)
            .then(({ exists }) => {
                if (!exists) {
                    createProjectIfNotExist();
                } else {
                    this.setState({
                        error: 'Directory already exists',
                    });
                }
            }).catch((error) => {
                this.setState({
                    error: error.message,
                });
                this.props.onSaveFail();
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
    updateState({ error, dirPath = this.state.dirPath, projectName = this.state.projectName }) {
        const { history } = this.props.workspaceManager.appContext.pref;
        history.put(HISTORY_LAST_ACTIVE_PATH, dirPath);
        this.setState({
            error,
            dirPath,
            projectName,
        });
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div>
                <Dialog
                    show={this.state.showDialog}
                    title='Create Project'
                    actions={
                        <Button
                            primary
                            onClick={this.onFileSave}
                            disabled={this.state.dirPath === '' || this.state.projectName === ''}
                        >
                            Create Project
                        </Button>
                    }
                    closeDialog
                    onHide={this.onDialogHide}
                    error={this.state.error}
                >
                    <Form
                        widths='equal'
                        onSubmit={(e) => {
                            this.onFileSave();
                        }}
                    >
                        <Form.Group controlId='projectName'>
                            <Form.Input
                                fluid
                                label='Project Name'
                                className='inverted'
                                placeholder='eg: hello-world'
                                value={this.state.projectName}
                                onChange={(evt) => {
                                    this.updateState({
                                        error: '',
                                        projectName: evt.target.value,
                                    });
                                }}
                            />
                        </Form.Group>

                        <Form.Group controlId='dirPath'>
                            <Form.Input
                                fluid
                                label='Project Path'
                                className='inverted'
                                placeholder='eg: /home/user/ballerina-services'
                                value={this.state.dirPath}
                                onChange={(evt) => {
                                    this.updateState({
                                        error: '',
                                        dirPath: evt.target.value,
                                    });
                                }}
                                action={isOnElectron() &&
                                    <Button
                                        icon='folder open'
                                        content='Select'
                                        onClick={(evt) => {
                                            const { ipcRenderer } = require('electron');
                                            ipcRenderer.send('show-folder-open-dialog', 'Select Project Path',
                                                'select parent folder for the project');
                                            ipcRenderer.once('folder-open-wizard-closed', (e, dirPath) => {
                                                if (dirPath) {
                                                    this.updateState({
                                                        error: '',
                                                        dirPath,
                                                    });
                                                }
                                            });
                                            evt.preventDefault();
                                        }}
                                    />
                                }
                            />
                        </Form.Group>

                    </Form>
                    {!isOnElectron() &&
                        <ScrollBarsWithContextAPI
                            style={{
                                height: 300,
                            }}
                            autoHide
                        >
                            <FileTree
                                activeKey={this.state.dirPath}
                                onSelect={
                                    (node) => {
                                        const dirPath = node.id;
                                        const projectName = node.fileName;
                                        this.updateState({
                                            error: '',
                                            dirPath,
                                            projectName,
                                        });
                                    }
                                }
                            />
                        </ScrollBarsWithContextAPI>
                    }
                </Dialog>
            </div>
        );
    }
}

CreateProjectDialog.contextTypes = {
    workspace: PropTypes.shape({
        isFilePathOpenedInExplorer: PropTypes.func,
        refreshPathInExplorer: PropTypes.func,
        goToFileInExplorer: PropTypes.func,
    }).isRequired,
};


CreateProjectDialog.propTypes = {
    file: PropTypes.objectOf(Object).isRequired,
    onSaveSuccess: PropTypes.func,
    onSaveFail: PropTypes.func,
    workspaceManager: PropTypes.objectOf(Object).isRequired,
    mode: PropTypes.string,
};

CreateProjectDialog.defaultProps = {
    onSaveSuccess: () => { },
    onSaveFail: () => { },
    mode: '',
};

export default CreateProjectDialog;
