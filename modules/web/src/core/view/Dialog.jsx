import React from 'react';
import PropTypes from 'prop-types';
import { Modal, Button } from 'react-bootstrap';

/**
 * Base class for popup dialogs
 * @extends React.Component
 */
class Dialog extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.close = this.close.bind(this);
    }

    /**
     * Close dialog
     */
    close() {
        this.props.onHide();
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <Modal show={this.props.show} onHide={this.close}>
                <Modal.Header closeButton>
                    <Modal.Title>{this.props.title}</Modal.Title>
                    <hr className="modal-body-seperator" />
                </Modal.Header>
                <Modal.Body>
                    {this.props.children}
                </Modal.Body>
                <Modal.Footer>
                    {this.props.actions}
                    {this.props.closeAction &&
                        <Button onClick={this.close}>Close</Button>
                    }
                </Modal.Footer>
            </Modal>
        );
    }
}

Dialog.propTypes = {
    show: PropTypes.bool,
    closeAction: PropTypes.bool,
    onHide: PropTypes.func,
    title: PropTypes.node.isRequired,
    children: PropTypes.node.isRequired,
    actions: PropTypes.node.isRequired,
};

Dialog.defaultProps = {
    show: true,
    closeAction: false,
    onHide: () => {},
};

export default Dialog;
