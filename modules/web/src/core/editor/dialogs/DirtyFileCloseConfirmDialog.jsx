import React from 'react';
import PropTypes from 'prop-types';
import { Button } from 'react-bootstrap';
import Dialog from './../../view/Dialog';

/**
 * Confirm Dialog when closing dirty files
 * @extends React.Component
 */
class DirtyFileCloseConfirmDialog extends React.Component {

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
                title="File Contains Unsaved Content"
                actions={
                [
                    <Button
                        onClick={() => {
                            this.onDialogHide();
                            this.props.onConfirm();
                        }}
                    >
                        Don't Save
                    </Button>,
                    <Button
                        bsStyle="primary"
                        onClick={() => {
                            this.onDialogHide();
                            this.props.onSave();
                        }}
                    >
                        Save
                    </Button>,
                ]}
                closeAction
                onHide={this.onDialogHide}
                error={this.state.error}
            >
                <h4>
                    Do you want to save the changes you made to 
                    {' ' + this.props.file.name + '.' + this.props.file.extension}?
                </h4>
                <p>
                    Your changes will be lost if you don't save them.
                </p>
            </Dialog>
        );
    }
}

DirtyFileCloseConfirmDialog.propTypes = {
    file: PropTypes.objectOf(Object).isRequired,
    onConfirm: PropTypes.func.isRequired,
    onSave: PropTypes.func.isRequired,
    editorPlugin: PropTypes.objectOf(Object).isRequired,
};

export default DirtyFileCloseConfirmDialog;
