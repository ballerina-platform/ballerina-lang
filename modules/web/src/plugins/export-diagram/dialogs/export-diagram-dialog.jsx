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
import { getPathSeperator } from 'api-client/api-client';
import PropTypes from 'prop-types';
import { Scrollbars } from 'react-custom-scrollbars';
import { Button, Form, FormGroup, FormControl, ControlLabel, Col } from 'react-bootstrap';
import Dialog from 'core/workspace/view/Dialog';
import FileTree from 'core/workspace/view/tree-view/FileTree';
import { createOrUpdate, exists as checkFileExists } from 'core/workspace/fs-util';
import { DIALOGS } from 'core/workspace/constants';
import { COMMANDS as LAYOUT_COMMANDS } from 'core/layout/constants';

const FILE_TYPE = 'file';

/**
 * File Save Wizard Dialog
 * @extends React.Component
 */
class ExportDiagramDialog extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            error: '',
            filePath: '',
            fileName: '',
            showDialog: true,
        };
        this.onFileSave = this.onFileSave.bind(this);
        this.onDialogHide = this.onDialogHide.bind(this);
    }

    /**
     * Called when user clicks open
     */
    onFileSave() {
        const { filePath, fileName } = this.state;
        if (fileName.trim() === '') {
            this.setState({
                error: 'File name cannot be empty',
            });
            return;
        }
        if (filePath.trim() === '') {
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
                    file.name = fileName;
                    file.path = derivedFilePath;
                    file.extension = 'bal';
                    file.fullPath = derivedFilePath + derivedFileName;
                    file.isPersisted = true;
                    file.isDirty = false;
                    this.props.onSaveSuccess();
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
                    this.props.command.dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, {
                        id: DIALOGS.REPLACE_FILE_CONFIRM,
                        additionalProps: {
                            filePath: derivedFilePath + derivedFileName,
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
     * @inheritdoc
     */
    render() {
        return (
            <div>
                <Dialog
                    show={this.state.showDialog}
                    title="Save Diagram"
                    actions={
                        <Button
                            bsStyle="primary"
                            onClick={this.onFileSave}
                            disabled={this.state.filePath === '' && this.state.fileName === ''}
                        >
                            Save
                        </Button>
                    }
                    closeAction
                    onHide={this.onDialogHide}
                    error={this.state.error}
                >
                    <Form horizontal>
                        <FormGroup controlId="filePath">
                            <Col componentClass={ControlLabel} sm={2}>
                                File Path
                            </Col>
                            <Col sm={10}>
                                <FormControl
                                    onKeyDown={(e) => {
                                        if (e.key === 'Enter') {
                                            this.onFileSave();
                                        } else if (e.key === 'Escape') {
                                            this.onDialogHide();
                                        }
                                    }}
                                    value={this.state.filePath}
                                    onChange={(evt) => {
                                        this.setState({
                                            error: '',
                                            filePath: evt.target.value,
                                        });
                                    }}
                                    type="text"
                                    placeholder="eg: /home/user/diagrams"
                                />
                            </Col>
                        </FormGroup>
                        <FormGroup controlId="fileName">
                            <Col componentClass={ControlLabel} sm={2}>
                                File Name
                            </Col>
                            <Col sm={10}>
                                <FormControl
                                    onKeyDown={(e) => {
                                        if (e.key === 'Enter') {
                                            this.onFileSave();
                                        } else if (e.key === 'Escape') {
                                            this.onDialogHide();
                                        }
                                    }}
                                    value={this.state.fileName}
                                    onChange={(evt) => {
                                        this.setState({
                                            error: '',
                                            fileName: evt.target.value,
                                        });
                                    }}
                                    type="text"
                                    placeholder="eg: routing.png"
                                />
                            </Col>
                        </FormGroup>
                    </Form>
                    <Scrollbars
                        style={{
                            width: 608,
                            height: 500,
                        }}
                        autoHide
                    >
                        <FileTree
                            onSelect={
                                (node) => {
                                    let filePath = node.id;
                                    let fileName = this.state.fileName;
                                    if (node.type === FILE_TYPE) {
                                        filePath = node.filePath;
                                        fileName = node.fileName + '.' + node.extension;
                                    }
                                    this.setState({
                                        error: '',
                                        filePath,
                                        fileName,
                                    });
                                }
                            }
                        />
                    </Scrollbars>
                </Dialog>
            </div>
        );
    }
}

ExportDiagramDialog.propTypes = {
    file: PropTypes.objectOf(Object).isRequired,
    onSaveSuccess: PropTypes.func,
    onSaveFail: PropTypes.func,
    command: PropTypes.objectOf(Object).isRequired,
};

ExportDiagramDialog.defaultProps = {
    onSaveSuccess: () => { },
    onSaveFail: () => { },
};

export default ExportDiagramDialog;
