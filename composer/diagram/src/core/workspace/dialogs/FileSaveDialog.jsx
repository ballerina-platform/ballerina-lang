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
import { createOrUpdate, exists as checkFileExists } from './../fs-util';
import { DIALOGS } from './../constants';
import { COMMANDS as LAYOUT_COMMANDS } from './../../layout/constants';

const FILE_TYPE = 'file';
const HISTORY_LAST_ACTIVE_PATH = 'composer.history.workspace.file-save-dialog.last-active-path';
const HISTORY_LAST_ACTIVE_NAME = 'composer.history.workspace.file-save-dialog.last-active-name';

/**
 * File Save Wizard Dialog
 * @extends React.Component
 */
class FileSaveDialog extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        const { history } = props.workspaceManager.appContext.pref;
        const filePath = history.get(HISTORY_LAST_ACTIVE_PATH) || '';
        const fileName = history.get(HISTORY_LAST_ACTIVE_NAME) || '';
        this.state = {
            error: '',
            filePath,
            fileName,
            showDialog: true,
        };
        this.onFileSave = this.onFileSave.bind(this);
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
    onFileSave() {
        const { filePath, fileName } = this.state;
        if (fileName === '') {
            this.setState({
                error: 'File name cannot be empty',
            });
            return;
        }
        if (filePath === '') {
            this.setState({
                error: 'File path cannot be empty',
            });
            return;
        }
        const derivedFilePath = !_.endsWith(filePath, getPathSeperator())
            ? filePath + getPathSeperator() : filePath;
        const derivedFileName = !_.endsWith(fileName, '.bal')
            ? fileName + '.bal' : fileName;
        const file = this.props.file;

        const saveFile = () => {
            createOrUpdate(derivedFilePath, derivedFileName, file.content)
                .then((success) => {
                    this.setState({
                        error: '',
                        showDialog: false,
                    });
                    file.name = _.endsWith(fileName, '.bal') ? _.split(fileName, '.bal')[0] : fileName;
                    file.path = derivedFilePath;
                    file.extension = 'bal';
                    file.fullPath = derivedFilePath + derivedFileName;
                    file.isPersisted = true;
                    file.isDirty = false;
                    this.props.onSaveSuccess();
                    if (this.context.workspace.isFilePathOpenedInExplorer(derivedFilePath)) {
                        this.context.workspace.refreshPathInExplorer(derivedFilePath);
                        this.context.workspace.goToFileInExplorer(file.fullPath);
                    }
                })
                .catch((error) => {
                    this.setState({
                        error: error.message,
                    });
                    this.props.onSaveFail();
                });
        };

        checkFileExists(derivedFilePath + derivedFileName)
            .then(({ exists }) => {
                if (!exists) {
                    saveFile();
                } else {
                    const { command: { dispatch } } = this.props.workspaceManager.appContext;
                    dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, {
                        id: DIALOGS.REPLACE_FILE_CONFIRM,
                        additionalProps: {
                            target: derivedFileName,
                            parent: derivedFilePath,
                            onConfirm: () => {
                                saveFile();
                            },
                            onCancel: () => {
                                this.props.onSaveFail();
                            },
                        },
                    });
                }
            })
            .catch((error) => {
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
    updateState({ error, filePath = this.state.filePath, fileName = this.state.fileName }) {
        const { history } = this.props.workspaceManager.appContext.pref;
        history.put(HISTORY_LAST_ACTIVE_PATH, filePath);
        history.put(HISTORY_LAST_ACTIVE_NAME, fileName);
        this.setState({
            error,
            filePath,
            fileName,
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
                    title={this.props.mode === 'SAVE_FILE' ? 'Save File' : 'Save File As'}
                    actions={
                        <Button
                            primary
                            onClick={this.onFileSave}
                            disabled={this.state.filePath === '' || this.state.fileName === ''}
                        >
                            Save
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

                        <Form.Group controlId='filePath'>
                            <Form.Input
                                fluid
                                label='File Path'
                                className='inverted'
                                placeholder='eg: /home/user/ballerina-services'
                                value={this.state.filePath}
                                onChange={(evt) => {
                                    this.updateState({
                                        error: '',
                                        filePath: evt.target.value,
                                    });
                                }}
                            />
                        </Form.Group>

                        <Form.Group controlId='fileName'>
                            <Form.Input
                                fluid
                                label='File Name'
                                className='inverted'
                                placeholder='eg: routing.bal'
                                value={this.state.fileName}
                                onChange={(evt) => {
                                    this.updateState({
                                        error: '',
                                        fileName: evt.target.value,
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
                                    let filePath = node.id;
                                    let fileName = this.state.fileName;
                                    if (node.type === FILE_TYPE) {
                                        filePath = node.filePath;
                                        fileName = node.fileName + '.' + node.extension;
                                    }
                                    this.updateState({
                                        error: '',
                                        filePath,
                                        fileName,
                                    });
                                }
                            }
                        />
                    </ScrollBarsWithContextAPI>
                </Dialog>
            </div>
        );
    }
}

FileSaveDialog.contextTypes = {
    workspace: PropTypes.shape({
        isFilePathOpenedInExplorer: PropTypes.func,
        refreshPathInExplorer: PropTypes.func,
        goToFileInExplorer: PropTypes.func,
    }).isRequired,
};


FileSaveDialog.propTypes = {
    file: PropTypes.objectOf(Object).isRequired,
    onSaveSuccess: PropTypes.func,
    onSaveFail: PropTypes.func,
    workspaceManager: PropTypes.objectOf(Object).isRequired,
    mode: PropTypes.string,
};

FileSaveDialog.defaultProps = {
    onSaveSuccess: () => { },
    onSaveFail: () => { },
    mode: '',
};

export default FileSaveDialog;
