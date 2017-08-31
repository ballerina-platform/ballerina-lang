import React from 'react';
import PropTypes from 'prop-types';
import { Button, Form, FormGroup, FormControl, ControlLabel, Col } from 'react-bootstrap';
import Dialog from './../../view/Dialog';
import FileTree from './../../view/FileTree';

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
        this.setState({
            showDialog: false,
        });
    }

    /**
     * Called when user hides the dialog
     */
    onDialogHide() {
        this.setState({
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
            >
                <Form horizontal>
                    <FormGroup controlId="folderPath">
                        <Col componentClass={ControlLabel} sm={2}>
                            Location
                        </Col>
                        <Col sm={10}>
                            <FormControl
                                value={this.state.folderPath}
                                onChange={(evt) => {
                                    this.setState({
                                        folderPath: evt.target.value,
                                    });
                                }}
                                type="text"
                                placeholder="eg: /home/user/ballerina-services/routing"
                            />
                        </Col>
                    </FormGroup>
                </Form>
                <FileTree
                    onSelect={
                        (node) => {
                            this.setState({
                                selectedNode: node,
                                folderPath: node.id,
                            });
                        }
                    }
                />
            </Dialog>
        );
    }
}

export default FileOpenDialog;
