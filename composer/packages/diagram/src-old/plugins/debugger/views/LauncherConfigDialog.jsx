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
 */

import React from 'react';
import PropTypes from 'prop-types';
import { Button, Form, TextArea } from 'semantic-ui-react';
import Dialog from 'core/view/Dialog';
import './LauncherConfigDialog.scss';
/**
 *
 * @extends React.Component
 */
class LauncherConfigDialog extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            error: '',
            showDialog: true,
            configArguments: this.props.configArguments.join(' '),
        };
        this.onDialogHide = this.onDialogHide.bind(this);
        this.onSave = this.onSave.bind(this);
        this.onChangeTextInput = this.onChangeTextInput.bind(this);
    }
    /**
     * Called when user hides the dialog
     */
    onDialogHide() {
        this.setState({
            showDialog: false,
        });
    }

    /**
     * Add command argument text field
     * @memberof LauncherConfigDialog
     */
    onSave() {
        const args = this.state.configArguments || '';
        const argsArr = args.split(' ');
        this.props.onSaveConfigs(argsArr);
        this.setState({
            showDialog: false,
        });
    }
    /**
     * Handle text input change event
     * @param e - Element onChange event
     */
    onChangeTextInput(e, { value }) {
        this.setState({
            configArguments: value,
        });
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <Dialog
                show={this.state.showDialog}
                title='Configure Application Arguments'
                actions={
                    <Button
                        primary
                        onClick={this.onSave}
                    >
                        Save
                    </Button>
                }
                closeAction
                onHide={this.onDialogHide}
                error={this.state.error}
            >
                <Form onSubmit={this.onSave}>
                    <Form.Field
                        label='Arguments for ballerina program command line execution'
                        placeholder='Command line arguments'
                        value={this.state.configArguments}
                        control={TextArea}
                        onChange={this.onChangeTextInput}
                    />
                </Form>
            </Dialog>
        );
    }
}

LauncherConfigDialog.propTypes = {
    onSaveConfigs: PropTypes.func.isRequired,
    configArguments: PropTypes.arrayOf(PropTypes.string),
};

LauncherConfigDialog.defaultProps = {
    configArguments: [],
};

export default LauncherConfigDialog;
