import React from 'react';
import PropTypes from 'prop-types';
import { Scrollbars } from 'react-custom-scrollbars';
import { Button, Form, FormGroup, FormControl, ControlLabel, Col } from 'react-bootstrap';
import Dialog from './../../view/Dialog';
import FileTree from './../../view/tree-view/FileTree';

const FILE_TYPE = 'file';

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
        this.state = {
            error: '',
            selectedNode: undefined,
            filePath: '',
            showDialog: true,
        };
        this.onFileOpen = this.onFileOpen.bind(this);
        this.onDialogHide = this.onDialogHide.bind(this);
    }

    /**
     * Called when user clicks open
     */
    onFileOpen() {
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
                title="Open File"
                actions={
                    <Button
                        bsStyle="primary"
                        onClick={this.onFileOpen}
                        disabled={this.state.filePath === ''}
                    >
                        Open
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
                                placeholder="eg: /home/user/ballerina-services/routing.bal"
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
                                this.onFileOpen();
                            }
                        }
                    />
                </Scrollbars>
            </Dialog>
        );
    }
}

FileOpenDialog.propTypes = {
    workspaceManager: PropTypes.objectOf(Object).isRequired,
};

export default FileOpenDialog;
