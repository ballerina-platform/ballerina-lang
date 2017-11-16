/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

import React from 'react';
import PropTypes from 'prop-types';
import { Modal, Button, Alert } from 'react-bootstrap';

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
        this.onExited = this.onExited.bind(this);
    }

    /**
     * Dialog exited
     */
    onExited() {
        this.props.onAfterHide();
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
            <Modal
                show={this.props.show}
                onHide={this.close}
                dialogClassName={'composer-dialog ' + this.props.className}
                onExited={this.onExited}
            >
                <Modal.Header closeButton>
                    <Modal.Title>{this.props.title}</Modal.Title>
                    {this.props.error !== '' &&
                        <Alert bsStyle="danger">
                            {this.props.error}
                        </Alert>
                    }
                </Modal.Header>
                <Modal.Body>
                        {this.props.children}
                </Modal.Body>
                <Modal.Footer>
                    {this.props.actions}
                    {this.props.closeAction &&
                        <Button onClick={this.close}>Cancel</Button>
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
    onAfterHide: PropTypes.func.isRequired,
    title: PropTypes.node.isRequired,
    children: PropTypes.node.isRequired,
    actions: PropTypes.node,
    error: PropTypes.node,
    className: PropTypes.string,
};

Dialog.defaultProps = {
    show: true,
    closeAction: false,
    onHide: () => {},
    onAfterHide: () => {},
    error: '',
    actions: '',
    className: '',
};

export default Dialog;
