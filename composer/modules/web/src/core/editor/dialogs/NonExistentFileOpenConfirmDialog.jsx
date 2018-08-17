/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { Button } from 'semantic-ui-react';
import Dialog from './../../view/Dialog';

/**
 * Confirm Dialog when opening non existent files
 * @extends React.Component
 */
class NonExistentFileOpenConfirmDialog extends React.Component {

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
                title='File Not Found'
                titleIcon='warning circle'
                actions={[
                    <Button
                        key='non-existent-file-open-confirm-dialog-save'
                        primary
                        onClick={(evt) => {
                            this.onDialogHide();
                            this.props.onSave();
                            evt.stopPropagation();
                            evt.preventDefault();
                        }}
                    >
                        Save
                    </Button>,
                    <Button
                        key='non-existent-file-open-confirm-dialog-close-n-delete'
                        secondary
                        onClick={(evt) => {
                            this.onDialogHide();
                            this.props.onConfirm();
                            evt.stopPropagation();
                            evt.preventDefault();
                        }}
                    >
                        Remove
                    </Button>,
                ]}
                closeAction
                onHide={this.onDialogHide}
                onAfterHide={this.props.onAfterHide}
                error={this.state.error}
            >
                <h4>
                    Opened file cannot be found in the disk. Do you want to save the changes you made to
                    {' "' + this.props.file.name + '.' + this.props.file.extension + '" '}?
                </h4>
                <p>
                    Your changes will be lost if you don&#39;t save them.
                </p>

            </Dialog>
        );
    }
}

NonExistentFileOpenConfirmDialog.propTypes = {
    file: PropTypes.objectOf(Object).isRequired,
    onConfirm: PropTypes.func.isRequired,
    onAfterHide: PropTypes.func,
    onSave: PropTypes.func.isRequired,
};

NonExistentFileOpenConfirmDialog.defaultProps = {
    onAfterHide: () => { },
};

export default NonExistentFileOpenConfirmDialog;
