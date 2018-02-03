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
import { Button, Form, FormGroup, FormControl, InputGroup } from 'react-bootstrap';
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
            configArguments: this.props.configArguments || [],
        };
        this.onDialogHide = this.onDialogHide.bind(this);
        this.onAddArgument = this.onAddArgument.bind(this);
        this.onChangeArgument = this.onChangeArgument.bind(this);
        this.saveConfigs = this.saveConfigs.bind(this);
        this.onDeleteArgument = this.onDeleteArgument.bind(this);
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
    onAddArgument() {
        this.setState({
            configArguments: this.state.configArguments,
        });
    }
    /**
     * on change argument text field
     * @param {int} idx - Array Index
     * @param {Object} evt - Text field change event
     * @memberof LauncherConfigDialog
     */
    onChangeArgument(idx, evt) {
        const newConfigArguments = [...this.state.configArguments];
        newConfigArguments[idx] = evt.target.value;
        this.setState({ configArguments: newConfigArguments });
    }

    /**
     * Save launcher configs
     * @memberof LauncherConfigDialog
     */
    saveConfigs() {
        this.props.onSaveConfigs(this.state.configArguments);
        this.setState({
            showDialog: false,
        });
    }
    /**
     * Remove command argument text field
     * @param {int} deleteIndex - Array index
     * @memberof LauncherConfigDialog
     */
    onDeleteArgument(deleteIndex) {
        const newConfigArguments = this.state.configArguments.filter((config, index) => {
            return index !== deleteIndex;
        });
        this.setState({
            configArguments: newConfigArguments,
        });
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <Dialog
                show={this.state.showDialog}
                title="Configure Application Arguments"
                actions={
                    <Button
                        bsStyle="primary"
                        onClick={this.saveConfigs}
                    >
                        Save
                    </Button>
                }
                closeAction
                onHide={this.onDialogHide}
                error={this.state.error}
            >
                <Form horizontal>

                    {this.state.configArguments.map((config, idx) => {
                        return (
                            <FormGroup key={idx}>
                                <InputGroup>
                                    <FormControl
                                        autoFocus
                                        value={config}
                                        onChange={event => this.onChangeArgument(idx, event)}
                                        type="text"
                                    />
                                    <InputGroup.Button>
                                        <Button
                                            bsStyle="primary"
                                            onClick={() => this.onDeleteArgument(idx)}
                                            className="launcher-config-delete"
                                        >
                                            <i className="fw fw-delete" />
                                        </Button>
                                    </InputGroup.Button>
                                </InputGroup>
                            </FormGroup>
                        );
                    })}

                    <FormGroup>
                        <Button
                            bsStyle="primary"
                            onClick={this.onAddArgument}
                        >
                            <i className="fw fw-add" />
                            &nbsp; Add argument
                        </Button>


                    </FormGroup>
                </Form>
            </Dialog>
        );
    }
}

LauncherConfigDialog.propTypes = {

};

export default LauncherConfigDialog;
