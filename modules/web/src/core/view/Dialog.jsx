import React from 'react';
import { Modal, Button } from 'react-bootstrap';
import View from './view';

/**
 * Base class for popup dialogs
 * @extends React.Component
 */
class Dialog extends View {

    /**
     * Method to get the unique ID of the dialog.
     *
     * @returns {String} A unique ID for the Dialog
     */
    getID() {
        return 'composer.dialog.generic';
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <Modal.Dialog>
                <Modal.Header>
                    <Modal.Title>{this.props.title}</Modal.Title>
                </Modal.Header>

                <Modal.Body>
                    {this.props.message}
                </Modal.Body>

                <Modal.Footer>
                    {this.props.actions}
                </Modal.Footer>

            </Modal.Dialog>
        );
    }
}

export default Dialog;
