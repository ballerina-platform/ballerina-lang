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
import { Modal, Header, Message, Segment, Transition, Button } from 'semantic-ui-react';

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
            <Transition animation='fade down' duration={500} transitionOnMount unmountOnHide>
                <Modal
                    open={this.props.show}
                    onClose={this.close}
                    closeIcon
                    size={this.props.size}
                    className={`inverted ${this.props.className}`}
                >
                    <Header icon={this.props.titleIcon} content={this.props.title} />
                    <Modal.Content>
                        <Segment inverted>
                            {
                                this.props.error &&
                                <Message negative>
                                    <p>{this.props.error}</p>
                                </Message>
                            }
                            {this.props.children}
                        </Segment>
                    </Modal.Content>
                    <Modal.Actions>
                        {this.props.closeDialog &&
                            <Button
                                secondary
                                onClick={this.close}
                            >
                                Cancel
                            </Button>
                        }
                        {this.props.actions}
                    </Modal.Actions>
                </Modal>
            </Transition>
        );
    }
}

Dialog.propTypes = {
    show: PropTypes.bool,
    onHide: PropTypes.func,
    onAfterHide: PropTypes.func.isRequired,
    title: PropTypes.node.isRequired,
    children: PropTypes.node.isRequired,
    actions: PropTypes.node,
    error: PropTypes.node,
    size: PropTypes.string,
    titleIcon: PropTypes.string,
    className: PropTypes.string,
    closeDialog: PropTypes.bool,
};

Dialog.defaultProps = {
    show: true,
    onHide: () => { },
    onAfterHide: () => { },
    error: '',
    actions: '',
    className: '',
    size: 'tiny',
    titleIcon: null,
    closeDialog: false,
};

export default Dialog;
