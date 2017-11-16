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
import { Button } from 'react-bootstrap';
import Dialog from 'core/view/Dialog';
import AceEditor from 'react-ace';

/**
 * File Open Wizard Dialog
 * @extends React.Component
 */
class ImportStructDialog extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            error: '',
            json: '',
            selectedNode: undefined,
            showDialog: true,
        };
        this.onImportJson = this.onImportJson.bind(this);
        this.onDialogHide = this.onDialogHide.bind(this);
        this.textChange = this.textChange.bind(this);
    }

    /**
     * Setting the default file path on the dialog if not set in history.
     * @memberof ImportStructDialog
     */
    componentDidMount() {
    }

    /**
     * Called when user clicks 'Import Struct' menu item.
     */
    onImportJson() {
        if (this.props.onImport(this.state.json)) {
            this.setState({
                error: '',
                json: '',
                isError: false,
                showDialog: false,
            });
        } else {
            this.setState({ isError: true });
        }
    }

    /**
     * Called when user hides the dialog
     */
    onDialogHide() {
        this.setState({
            error: '',
            json: '',
            isError: false,
            showDialog: false,
        });
    }
    /**
     * Called when user types on the editor.
     * @param  {string} newValue changed text value
     */
    textChange(newValue) {
        this.setState({ json: newValue, isError: false });
    }

    /**
     * @inheritdoc
     */
    render() {
        const errorStyle = {
            color: '#d9534f',
        };
        return (
            <Dialog
                show={this.state.showDialog}
                title="Import from JSON"
                actions={
                    <Button
                        bsStyle="primary"
                        onClick={this.onImportJson}
                        disabled={this.state.json === ''}
                    >
                        Import
                    </Button>
                }
                closeAction
                onHide={this.onDialogHide}
                error={this.state.error}
            >
                <p>Please enter a valid sample JSON to generate struct definition.</p>
                <AceEditor
                    mode='json'
                    theme='monokai'
                    onChange={this.textChange}
                    value={this.state.json}
                    name='json'
                    editorProps={{
                        $blockScrolling: Infinity,
                    }}
                    setOptions={{
                        showLineNumbers: false,
                    }}
                    maxLines={30}
                    minLines={10}
                    showGutter={this.state.isError}
                    width='auto'
                    showPrintMargin={false}
                />
                {(this.state.isError) &&
                    <div className='alert alert-danger'>
                        <p style={errorStyle}> Struct cannot be generated for the JSON provided </p>
                    </div>
                }
            </Dialog>
        );
    }
}

ImportStructDialog.propTypes = {
    onImport: PropTypes.arrayOf(PropTypes.string).isRequired,
};

export default ImportStructDialog;
