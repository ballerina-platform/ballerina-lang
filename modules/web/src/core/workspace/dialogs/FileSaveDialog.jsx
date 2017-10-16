import React from 'react';
import _ from 'lodash';
import { getPathSeperator } from 'api-client/api-client';
import PropTypes from 'prop-types';
import { Scrollbars } from 'react-custom-scrollbars';
import { Button, Form, FormGroup, FormControl, ControlLabel, Col } from 'react-bootstrap';
import Dialog from './../../view/Dialog';
import FileTree from './../../view/tree-view/FileTree';
import { createOrUpdate, exists as checkFileExists } from './../fs-util';
import { DIALOGS } from './../constants';
import { COMMANDS as LAYOUT_COMMANDS } from './../../layout/constants';

const FILE_TYPE = 'file';

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
                    title="Save File"
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
                                    placeholder="eg: /home/user/ballerina-services"
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
                                    placeholder="eg: routing.bal"
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

FileSaveDialog.propTypes = {
    file: PropTypes.objectOf(Object).isRequired,
    onSaveSuccess: PropTypes.func,
    onSaveFail: PropTypes.func,
    workspaceManager: PropTypes.objectOf(Object).isRequired,
};

FileSaveDialog.defaultProps = {
    onSaveSuccess: () => {},
    onSaveFail: () => {},
};

export default FileSaveDialog;
