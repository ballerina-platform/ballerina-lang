import React from 'react';
import PropTypes from 'prop-types';
import { Button } from 'react-bootstrap';
import Dialog from './../../view/Dialog';

/**
 * Confirm Dialog when deleting files from disk
 * @extends React.Component
 */
class FileDeleteConfirmDialog extends React.Component {

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
        this.props.onCancel();
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <Dialog
                show={this.state.showDialog}
                title={`Delete ${this.props.isFolder ? 'Folder' : 'File'} From Disk`}
                actions={
                [
                    <Button
                        onClick={() => {
                            this.setState({
                                showDialog: false,
                            });
                            this.props.onConfirm();
                        }}
                    >
                        Delete
                    </Button>,
                ]}
                closeAction
                onHide={this.onDialogHide}
                error={this.state.error}
            >
                <h4>
                    {`Are you sure you want to delete "${this.props.target}"
                        ${this.props.isFolder ? ' and its contents' : ''} ?`}
                </h4>
                <p>
                    {`${this.props.isFolder ? 'Folder' : 'File'} will be deleted from file system.`}
                </p>
            </Dialog>
        );
    }
}

FileDeleteConfirmDialog.propTypes = {
    isFolder: PropTypes.bool.isRequired,
    target: PropTypes.string.isRequired,
    onConfirm: PropTypes.func.isRequired,
    onCancel: PropTypes.func.isRequired,
};

export default FileDeleteConfirmDialog;
