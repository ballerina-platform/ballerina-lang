import React from 'react';
import PropTypes from 'prop-types';
import { Button } from 'react-bootstrap';
import Dialog from './../../view/Dialog';

/**
 * Confirm Dialog when replacing existing files
 * @extends React.Component
 */
class FileReplaceConfirmDialog extends React.Component {

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
                title="File Already Exists"
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
                        Overwrite
                    </Button>,
                ]}
                closeAction
                onHide={this.onDialogHide}
                error={this.state.error}
            >
                <h4>
                    File {` ${this.props.filePath} `} alredy exists.
                </h4>
                <p>
                   Do you want to overwrite the file?
                </p>
            </Dialog>
        );
    }
}

FileReplaceConfirmDialog.propTypes = {
    filePath: PropTypes.string.isRequired,
    onConfirm: PropTypes.func.isRequired,
    onCancel: PropTypes.func.isRequired,
};

export default FileReplaceConfirmDialog;
