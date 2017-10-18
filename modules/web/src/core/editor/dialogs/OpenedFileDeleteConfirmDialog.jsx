import React from 'react';
import PropTypes from 'prop-types';
import { Button } from 'react-bootstrap';
import Dialog from './../../view/Dialog';

/**
 * Confirm Dialog when deleting a file from tree - which is alreay opened in dirty files
 * @extends React.Component
 */
class OpenedFileDeleteConfirmDialog extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            error: '',
            showDialog: true,
        };
        this.onDialogHide = this.onDialogHide.bind(this);
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
                title="Please Confirm"
                actions={
                [
                    <Button
                        key='opened-file-delete-confirm-dialog-close-and-delete'
                        onClick={(evt) => {
                            this.onDialogHide();
                            this.props.onConfirm();
                            evt.stopPropagation();
                            evt.preventDefault();
                        }}
                    >
                        {'Close & Delete'}
                    </Button>,
                ]}
                closeAction
                onHide={this.onDialogHide}
                error={this.state.error}
            >
                <p>
                    {`File ${this.props.file.name}.${this.props.file.extension} is opened in editor.`}
                    <br />
                    {'File will be closed & deleted from file system.'}
                </p>
            </Dialog>
        );
    }
}

OpenedFileDeleteConfirmDialog.propTypes = {
    file: PropTypes.objectOf(Object).isRequired,
    onConfirm: PropTypes.func.isRequired,
};

export default OpenedFileDeleteConfirmDialog;
