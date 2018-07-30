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
 */

import React from 'react';
import PropTypes from 'prop-types';
import { Button, Form, Input, Checkbox, Accordion, Icon } from 'semantic-ui-react';
import Dialog from 'core/view/Dialog';
import MonacoEditor from 'react-monaco-editor';

/**
 * File Open Wizard Dialog
 * @extends React.Component
 */
class ImportRecordDialog extends React.Component {

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
            activeIndex: 1,
        };
        this.onImportJson = this.onImportJson.bind(this);
        this.onDialogHide = this.onDialogHide.bind(this);
        this.textChange = this.textChange.bind(this);
        this.onValidate = this.onValidate.bind(this);
        this.handleAccordion = this.handleAccordion.bind(this);
    }

    /**
     * Setting the default file path on the dialog if not set in history.
     * @memberof ImportRecordDialog
     */
    componentDidMount() {
    }

    /**
     * Called when user clicks 'Import Recrod' menu item.
     */
    onImportJson() {
        if (this.props.onImport(this.state.json, this.state.recordName, this.state.removeDefaults)) {
            this.setState({
                error: '',
                json: '',
                isJSONError: false,
                isGenerationError: false,
                showDialog: false,
            });
        } else {
            this.setState({ isGenerationError: true });
        }
    }

    /**
     * Called when user hides the dialog
     */
    onDialogHide() {
        this.setState({
            error: '',
            json: '',
            isJSONError: false,
            isGenerationError: false,
            showDialog: false,
        });
    }

    /**
    *  Set JSON Error state upon validating JSON by Editor
     * @param  {array} errors List of validation errors on the editor
     */
    onValidate(errors) {
        this.setState({ isJSONError: errors.length > 0 });
    }

    /**
     * Called when user types on the editor.
     * @param  {string} newValue changed text value
     */
    textChange(newValue) {
        this.setState({ json: newValue, isJSONError: false, isGenerationError: false });
    }

    /**
     * Handler function for accordion
     * @param {object} e 
     * @param {object} titleProps 
     */
    handleAccordion(e, titleProps) {
        const { index } = titleProps;
        const { activeIndex } = this.state;
        const newIndex = activeIndex === index ? -1 : index;
    
        this.setState({ activeIndex: newIndex });
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
                title='Create Record'
                titleIcon='fw fw-import'
                closeDialog
                actions={[
                    <Button
                        primary
                        onClick={this.onImportJson}
                    >
                        Import
                    </Button>
                ]}
                onHide={this.onDialogHide}
                error={this.state.error}
            >
                <Form
                    widths='equal'
                    onSubmit={(e) => {
                        this.onImportJson();
                    }}
                >
                    <Form.Group controlId='recordName' inline className='inverted'>
                        <Form.Field width={3} htmlFor='recordName'>
                            <label>Record Name</label>
                        </Form.Field>
                        <Form.Field width={12} className='inverted'>
                            <Input
                                type='text'
                                value={this.state.recordName}
                                onChange={(evt) => {
                                    this.setState({
                                        error: '',
                                        recordName: evt.target.value,
                                    });
                                }}
                            />
                        </Form.Field>
                    </Form.Group>
                    <Accordion as={Form.Field} className='inverted'>
                        <Accordion.Title active={this.state.activeIndex === 0} index={0} onClick={this.handleAccordion}>
                            <Icon name='dropdown' />
                            Create record using JSON.
                        </Accordion.Title>
                        <Accordion.Content active={this.state.activeIndex === 0}>
                            <Form.Field>
                                <p>Please enter a valid sample JSON to generate a record.</p>
                                <MonacoEditor
                                    width='auto'
                                    height='300'
                                    language='json'
                                    theme='vs-dark'
                                    value={this.state.json}
                                    onChange={this.textChange}
                                    name='json'
                                    options={{
                                        autoIndent: true,
                                        fontSize: 14,
                                        contextmenu: true,
                                        renderIndentGuides: true,
                                        autoClosingBrackets: true,
                                        matchBrackets: true,
                                        automaticLayout: true,
                                        glyphMargin: true,
                                        folding: true,
                                        lineNumbersMinChars: 2,
                                    }}
                                />
                            </Form.Field>
                            <Form.Group controlId='removeDefaults' inline className='inverted'>
                                <Form.Field width={15} className='inverted'>
                                    <Checkbox
                                        label='Import JSON without defaults values.'
                                        value={this.state.removeDefaults}
                                        onChange={(evt, data) => {
                                            this.setState({
                                                error: '',
                                                removeDefaults: data.checked,
                                            });
                                        }}
                                    />
                                </Form.Field>
                            </Form.Group>
                        </Accordion.Content>
                    </Accordion>
                </Form>
                {(this.state.isGenerationError && this.state.isJSONError) &&
                    <div className='alert alert-danger'>
                        <p style={errorStyle}>Invalid JSON</p>
                    </div>
                }
                {(this.state.isGenerationError && !this.state.isJSONError) &&
                    <div className='alert alert-danger'>
                        <p style={errorStyle}>Please make sure JSON attribute
                          names are align with ballerina variable naming conventions </p>
                    </div>
                }
            </Dialog>
        );
    }
}

ImportRecordDialog.propTypes = {
    onImport: PropTypes.arrayOf(PropTypes.string).isRequired,
};

export default ImportRecordDialog;
