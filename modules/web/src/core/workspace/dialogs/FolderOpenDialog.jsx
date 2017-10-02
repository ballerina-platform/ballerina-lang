import React from 'react';
import PropTypes from 'prop-types';
import { Scrollbars } from 'react-custom-scrollbars';
import { Button, Form, FormGroup, FormControl, ControlLabel, Col } from 'react-bootstrap';
import Dialog from './../../view/Dialog';
import FileTree from './../../view/tree-view/FileTree';
import { exists as checkPathExists } from './../fs-util';


const FOLDER_TYPE = 'folder';

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
        this.state = {
            error: '',
            selectedNode: undefined,
            showDialog: true,
            folderPath: '',
        };
        this.onFolderOpen = this.onFolderOpen.bind(this);
        this.onDialogHide = this.onDialogHide.bind(this);
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
     * @inheritdoc
     */
    render() {
        return (
            <Dialog
                show={this.state.showDialog}
                title="Open Program Directory"
                actions={
                    <Button
                        bsStyle="primary"
                        onClick={this.onFolderOpen}
                        disabled={this.state.folderPath === ''}
                    >
                        Open
                    </Button>
                }
                closeAction
                onHide={this.onDialogHide}
                error={this.state.error}
            >
                <Form
                    horizontal
                    onSubmit={(e) => {
                        e.preventDefault();
                    }}
                >
                    <FormGroup controlId="folderPath">
                        <Col componentClass={ControlLabel} sm={2}>
                            Location
                        </Col>
                        <Col sm={10}>
                            <FormControl
                                value={this.state.folderPath}
                                onKeyDown={(e) => {
                                    if (e.key === 'Enter') {
                                        this.onFolderOpen();
                                    } else if (e.key === 'Escape') {
                                        this.onDialogHide();
                                    }
                                }}
                                onChange={(evt) => {
                                    this.setState({
                                        error: '',
                                        folderPath: evt.target.value,
                                        selectedNode: undefined,
                                    });
                                }}
                                type="text"
                                placeholder="eg: /home/user/ballerina-services/routing"
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
                                    folderPath: node.id,
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
                                this.onFolderOpen();
                            }
                        }
                    />
                </Scrollbars>
            </Dialog>
        );
    }
}

FolderOpenDialog.propTypes = {
    workspaceManager: PropTypes.objectOf(Object).isRequired,
};

export default FolderOpenDialog;
