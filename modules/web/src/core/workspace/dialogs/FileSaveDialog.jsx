import React from 'react';
import PropTypes from 'prop-types';
import { Scrollbars } from 'react-custom-scrollbars';
import { Button, Form, FormGroup, FormControl, ControlLabel, Col } from 'react-bootstrap';
import Dialog from './../../view/Dialog';
import FileTree from './../../view/tree-view/FileTree';

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
            selectedNode: undefined,
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
        const { selectedNode: { type, id } } = this.state;
        if (type !== FILE_TYPE) {
            this.setState({
                error: `${id} is not a file`,
            });
        } else {
            this.props.workspaceManager.openFile(id)
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
        }
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
                                this.setState({
                                    error: '',
                                    selectedNode: node,
                                    filePath: node.id,
                                });
                            }
                        }
                        onOpen={
                            (node) => {
                                this.setState({
                                    error: '',
                                    selectedNode: node,
                                    filePath: node.id,
                                });
                                this.onFileSave();
                            }
                        }
                    />
                </Scrollbars>
            </Dialog>
        );
    }
}

FileSaveDialog.propTypes = {
    workspaceManager: PropTypes.objectOf(Object).isRequired,
};

export default FileSaveDialog;
