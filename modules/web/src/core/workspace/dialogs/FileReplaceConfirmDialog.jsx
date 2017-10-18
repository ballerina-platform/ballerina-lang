import React from 'react';
import PropTypes from 'prop-types';
import { Button, Row, Grid, Col } from 'react-bootstrap';
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
                title="Replace An Existing File"
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
                <Grid fluid>
                    <Row>
                        <Col md={2}>
                            <i className="fw fw-4x fw-warning danger" />
                        </Col>
                        <Col md={10}>
                            <h4 style={{ marginTop: 0 }}>
                                {`A file named "${this.props.target}" already exists. Do you want to replace it?`}
                            </h4>
                            <p>
                                {`The file already exists in "${this.props.parent}". 
                                Replacing it will overwrite its contents.`}
                            </p>
                        </Col>
                    </Row>
                </Grid>
            </Dialog>
        );
    }
}

FileReplaceConfirmDialog.propTypes = {
    target: PropTypes.string.isRequired,
    parent: PropTypes.string.isRequired,
    onConfirm: PropTypes.func.isRequired,
    onCancel: PropTypes.func.isRequired,
};

export default FileReplaceConfirmDialog;
