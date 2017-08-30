import React from 'react';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
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
                    {this.props.children}
                </Modal.Body>

                <Modal.Footer>
                    {this.props.actions}
                </Modal.Footer>

            </Modal.Dialog>
        );
    }
}

Dialog.propTypes = {
    title: PropTypes.node.isRequired,
    children: PropTypes.node.isRequired,
    actions: PropTypes.node.isRequired,
};

export default Dialog;
