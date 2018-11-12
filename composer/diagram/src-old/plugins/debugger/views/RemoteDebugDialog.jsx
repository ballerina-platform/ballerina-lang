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
import { Button, Form, Input } from 'semantic-ui-react';
import Dialog from 'core/view/Dialog';
import './RemoteDebugDialog.scss';
import DebugManager from './../DebugManager';
/**
 *
 * @extends React.Component
 */
class RemoteDebugDialog extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            error: '',
            showDialog: true,
            url: '',
            connecting: false,
        };
        this.onDialogHide = this.onDialogHide.bind(this);
        this.onChangeUrl = this.onChangeUrl.bind(this);
        this.connectDebugger = this.connectDebugger.bind(this);
    }


    componentDidMount() {
        DebugManager.on('session-error', () => {
            if (this.state.showDialog) {
                this.setState({
                    error: 'Connection error, Please check provided URL.',
                    connecting: false,
                });
            }
        });
        DebugManager.on('connecting', () => {
            if (this.state.showDialog) {
                this.setState({
                    connecting: true,
                });
            }
        });
        DebugManager.on('debugging-started', () => {
            if (this.state.showDialog) {
                this.setState({
                    showDialog: false,
                    connecting: false,
                });
            }
        });
    }

    /**
     * Called when user hides the dialog
     */
    onDialogHide() {
        this.setState({
            showDialog: false,
        });
    }
    onChangeUrl(event) {
        this.setState({
            url: event.target.value,
            error: '',
            connecting: false,
        });
    }
    connectDebugger() {
        try {
            DebugManager.startDebugger(`ws://${this.state.url}/debug`);
        } catch (error) {
            this.setState({
                connecting: false,
                error: error.message,
            });
        }
    }
    /**
     * @inheritdoc
     */
    render() {
        const { error, url } = this.state;
        return (
            <Dialog
                show={this.state.showDialog}
                title='Remote Debug'
                actions={
                    <Button
                        primary
                        onClick={this.connectDebugger}
                        disabled={!url.length || this.state.connecting}
                    >
                        {this.state.connecting ? 'Connecting' : 'Debug'}
                    </Button>
                }
                closeAction
                onHide={this.onDialogHide}
                error={error}
                className='remote-debug-dialog'
            >
                <Form
                    inverted
                    onSubmit={(e) => {
                        this.connectDebugger();
                    }}
                >
                    <div className='help-block'>
                        <span>Add <strong>--debug PORT_NUMBER </strong>
                            to your ballerina command to enable remote debugging</span>
                        <div>Example: </div>
                        <span><code>ballerina run helloWorld.bal --debug 5006</code></span>
                    </div>
                    <Input
                        style={{ marginTop: 20 }}
                        fluid
                        label='ws://'
                        value={this.state.url}
                        onChange={this.onChangeUrl}
                        placeholder='Remote debugger url'
                    />
                </Form>
            </Dialog>
        );
    }
}

RemoteDebugDialog.propTypes = {

};

export default RemoteDebugDialog;
