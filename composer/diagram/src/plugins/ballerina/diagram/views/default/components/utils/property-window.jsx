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
import _ from 'lodash';
import './properties-form.css';
import TagInput from './tag-input';
import FragmentUtils from './../../../../../utils/fragment-utils';
import TreeBuilder from './../../../../../model/tree-builder';
import TreeUtils from './../../../../../model/tree-util';
import { Button, Checkbox, Form, Grid } from 'semantic-ui-react';

/**
 * React component for a service definition.
 *
 * @class ServiceDefinition
 * @extends {React.Component}
 */
class PropertyWindow extends React.Component {

    constructor(props) {
        super(props);
        this.previousItems = [];
        this.breadCrumbs = ['Properties'];
        this.supportedKeys = props.supportedProps;
        this.isVarDefEnabled = this.props.varDefInit;
        this.varDefInitRef = this.props.varDefInitRef;
        this.state = {
            properties: this.supportedKeys,
            error: false,
            isVarDefEnabled: this.props.varDefInit,
            varDefInitRef: this.props.varDefInitRef,
        };
        this.onChange = this.onChange.bind(this);
        this.clickVarDefCheck = this.clickVarDefCheck.bind(this);
        this.handleDismiss = this.handleDismiss.bind(this);
        this.renderNumericInputs = this.renderNumericInputs.bind(this);
        this.renderTextInputs = this.renderTextInputs.bind(this);
        this.renderBooleanInputs = this.renderBooleanInputs.bind(this);
        this.toggleStructProperties = this.toggleStructProperties.bind(this);
        this.goToPreviousView = this.goToPreviousView.bind(this);
        this.onTagsAdded = this.onTagsAdded.bind(this);
        this.removeTagsAdded = this.removeTagsAdded.bind(this);
        this.renderTagInputs = this.renderTagInputs.bind(this);
        this.toggleStructView = this.toggleStructView.bind(this);
        this.closePropertyWindow = this.closePropertyWindow.bind(this);
        this.handleStructs = this.handleStructs.bind(this);
    }

    /**
     * On change event for form inputs
     * @param event
     * @param index
     */
    onChange(event, key) {
        const value = event.target.type === 'checkbox' ? event.target.checked : event.target.value;
        key.value = value;
        this.forceUpdate();
    }

    clickVarDefCheck(event) {
        this.isVarDefEnabled = event.target.checked;
        this.setState({
            isVarDefEnabled: event.target.checked,
        });
    }

    /**
     * Hanldes the dismiss/cancel event of the prop window
     */
    handleDismiss() {
        this.handleStructs(this.state.properties); // update the properties in the specific page
        this.handleStructs(this.supportedKeys); // update all the properties
        this.handleConnectors(this.state.properties); // update the properties in the specific page
        this.handleConnectors(this.supportedKeys); // update all the properties
        if (!this.state.error) {
            if (this.isVarDefEnabled) {
                this.props.addedValues(this.varDefInitRef, true);
            } else {
                this.props.addedValues(this.supportedKeys, false);
            }
            this.props.model.viewState.showOverlayContainer = false;
            this.props.model.viewState.shouldShowConnectorPropertyWindow = false;
            this.props.model.viewState.overlayContainer = {};
            this.props.model.trigger('tree-modified', {
                origin: this.props.model,
                type: 'Property Submitted',
                title: 'Property Changed',
                data: {
                    node: this.props.model,
                },
            });
        }
    }

    /**
     * Close the property window without saving any changes
     */
    closePropertyWindow() {
        this.props.model.viewState.showOverlayContainer = false;
        this.props.model.viewState.shouldShowConnectorPropertyWindow = false;
        this.props.model.viewState.overlayContainer = {};
        this.context.editor.update();
    }

    /**
     * Toggles the struct properties
     */
    toggleStructProperties(identifier, fields) {
        this.previousItems.push(this.state.properties);
        this.breadCrumbs.push(identifier);
        this.handleStructs(this.supportedKeys);
        this.setState({
            properties: fields,
        });
    }


    /**
     * Toggles the connector param properties
     */
    toggleConnectorParamProperties(identifier, fields) {
        this.previousItems.push(this.state.properties);
        this.breadCrumbs.push(identifier);
        this.forceUpdate();
        this.setState({
            properties: fields,
        });
    }

    /**
     * Handles rendering the previous view with the props
     */
    goToPreviousView() {
        const poppedData = this.previousItems.pop();
        this.breadCrumbs.pop();
        this.handleStructs(this.supportedKeys);
        this.setState({
            properties: poppedData,
        });
    }

    /**
     * Toggle the struct view on breadcrumb element click
     */
    toggleStructView(index) {
        // Get the elements related to the index
        const elements = this.previousItems[index];
        this.previousItems.splice(index);
        this.breadCrumbs.splice(index + 1);
        this.handleStructs(this.supportedKeys);
        this.setState({
            properties: elements,
        });
    }

    /**
     * Renders text input for form
     * @param key
     * @returns {XML}
     */
    renderTextInputs(key) {
        let value = key.value;
        if (value !== null) {
            if (value.startsWith('"') && value.endsWith('"')) {
                value = value.substring(1, value.length - 1);
            }
        }

        return (
            <Grid key={key.identifier}>
                <Grid.Row>
                    <Grid.Column width={6}>
                        <label
                            htmlFor={key.identifier}
                        >
                            {_.startCase(key.identifier)}
                        </label>
                    </Grid.Column>
                    <Grid.Column width={10}>
                        <input
                            id={key.identifier}
                            name={key.identifier}
                            type='text'
                            placeholder={_.startCase(key.identifier)}
                            value={value}
                            onChange={event => this.onChange(event, key)}
                        />
                    </Grid.Column>
                </Grid.Row>
            </Grid>);
    }

    /**
     * Renders a select box
     * @param key
     * @returns {select box}
     */
    renderSelectBox(key) {
        return (
            <Grid key={key.identifier}>
                <Grid.Row>
                    <Grid.Column width={6}>
                        <label
                            htmlFor={key.identifier}
                        >
                            {_.startCase(key.identifier)}
                        </label>
                    </Grid.Column>
                    <Grid.Column width={10}>
                        <select
                            value={key.value}
                            onChange={event => this.onChange(event, key)}
                        >
                            <option value='null'>Select {key.identifier}</option>
                            {key.fields.map((option) => {
                                return <option value={option} key={option}>{option}</option>;
                            })}
                        </select>
                    </Grid.Column>
                </Grid.Row>
            </Grid>);
    }

    /**
     * Renders numeric input for form
     * @param key
     * @returns {XML}
     */
    renderNumericInputs(key) {
        return (
            <Grid key={key.identifier}>
                <Grid.Row>
                    <Grid.Column width={6}>
                        <label
                            htmlFor={key.identifier}
                        >
                            {_.startCase(key.identifier)}
                        </label>
                    </Grid.Column>
                    <Grid.Column width={10}>
                        <input
                            id={key.identifier}
                            name={key.identifier}
                            type='number'
                            placeholder={_.startCase(key.identifier)}
                            value={Math.abs(key.value)}
                            onChange={event => this.onChange(event, key)}
                        />
                    </Grid.Column>
                </Grid.Row>
            </Grid>);
    }

    renderVariableReferenceSection(key) {
        const label = 'Variable Reference';
        return (
            <Grid key='vardef'>
                <Grid.Row>
                    <Grid.Column width={6}>
                        <label
                            htmlFor='varRefEnable'
                        >
                            <input
                                type='checkbox'
                                name='varRefEnable'
                                id='varRefEnable'
                                value='variable ref'
                                onClick={event => this.clickVarDefCheck(event)}
                                checked={this.isVarDefEnabled}
                            />
                            <span>&nbsp;</span>
                            {label}
                        </label>
                    </Grid.Column>
                    <Grid.Column
                        width={(this.isVarDefEnabled ? '' : ' content-disabled' + ' ten')}
                    >
                        <input
                            id='vardef'
                            name='vardef'
                            type='text'
                            placeholder='var1'
                            value={this.isVarDefEnabled ? this.varDefInitRef.value : ''}
                            onChange={event => this.onChange(event, key)}
                        />
                    </Grid.Column>
                </Grid.Row>
            </Grid>
        );
    }

    /**
     * Renders boolean input for form
     * @param key
     * @param booleanValue
     * @returns {XML}
     */
    renderBooleanInputs(key, booleanValue) {
        return (
            <Grid key={key.identifier}>
                <Grid.Row>
                    <Grid.Column width={6}>
                        <label
                            htmlFor={key.identifier}
                        >
                            {_.startCase(key.identifier)}
                        </label>
                    </Grid.Column>
                    <Grid.Column width={10}>
                        <Checkbox
                            toggle
                            id={key.identifier}
                            // checked={booleanValue}
                            // onChange={event => this.onChange(event, key)}
                        />
                    </Grid.Column>
                </Grid.Row>
            </Grid>);
    }

    /**
     * Add quotation for strings
     */
    addQuotationForStringValues(value) {
        if (!value.startsWith('"')) {
            value = '"' + value + '"';
        }
        return value;
    }

    /**
     * Get the stringified map
     * @param properties
     * @returns {string} stringified map
     */
    getStringifiedMap(properties) {
        const valueArr = [];
        properties.forEach((field) => {
            // Remove quotation marks of the identifier
            if (field.identifier.startsWith('"')) {
                field.identifier = JSON.parse(field.identifier);
            }
            if ((!field.value && field.bType !== 'struct') ||
                (field.value === field.defaultValue && field.bType !== 'struct')) {
                return;
            }
            // If the btype is string, add quotation marks to the value
            if (field.bType === 'string') {
                field.value = this.addQuotationForStringValues(field.value);
            }
            if (field.bType !== 'struct') {
                valueArr.push(field.identifier + ':' + field.value);
            } else if (field.bType === 'struct') {
                if (!field.value || (field.value.startsWith('{') && field.value.endsWith('}'))) {
                    let localMap = this.getStringifiedMap(field.fields);
                    if (localMap !== '{}') {
                        localMap = field.identifier + ': ' + localMap;
                        valueArr.push(localMap);
                    }
                } else {
                    valueArr.push(field.identifier + ':' + field.value);
                }
            }
        });
        const map = '{' + valueArr.join(',') + '}';
        return map;
    }

    /**
     * Clears the struct field values
     */
    clearStructFieldValues(key, fields) {
        if (fields) {
            fields.forEach((field) => {
                if (field.bType === 'struct') {
                    this.clearStructFieldValues(field, field.fields);
                }
                field.value = field.defaultValue;
            });
        }
        key.value = '{}';
        this.setState({
            error: false,
        });
    }

    /**
     * Handle the connector property
     * @param properties
     */
    handleConnectors(properties) {
        properties.forEach((key) => {
            let connectorParams = '(';
            if (key.isConnector) {
                key.connectorParams.forEach((param, index) => {
                    const value = param.bType === 'string' ? this.addQuotationForStringValues(param.value)
                        : param.value;
                    if (index !== key.connectorParams.length - 1) {
                        connectorParams += `${value},`;
                    } else {
                        connectorParams += `${value}`;
                    }
                });
                connectorParams += ')';
                key.value = key.value.replace(/ *\([^)]*\) */g, connectorParams);
            }
        });
    }

    /**
     * Update values of structs when navigating across the prop window
     * @param properties
     */
    handleStructs(properties) {
        properties.forEach((key) => {
            if (key.bType === 'struct') {
                if (!key.value) {
                    key.fields.forEach((field) => {
                        if (key.bType === 'struct') {
                            this.clearStructFieldValues(key, key.fields);
                        } else {
                            field.value = field.defaultValue;
                        }
                    });
                    key.value = '{}';
                } else if (key.value.startsWith('{') && key.value.endsWith('}')) {
                    // If the user edits the configuration properties, then the user
                    // is not on the first page i.e. the user is inside a struct
                    // TODO no way to figure out whether the user is providing a direct
                    // value to the struct or whether the properties of the struct are
                    // configured
                    key.value = this.getStringifiedMap(key.fields);
                    const fragment = FragmentUtils.createExpressionFragment(key.value);
                    const parsedJson = FragmentUtils.parseFragment(fragment);
                    if (parsedJson.error) {
                        this.setState({
                            error: true,
                        });
                    } else {
                        const varDefNode = TreeBuilder.build(parsedJson).variable.initialExpression;
                        if (TreeUtils.isSimpleVariableRef(varDefNode) ||
                            TreeUtils.isLiteral(varDefNode)) {
                            key.value = this.getAddedValueOfProp(varDefNode);
                        } else if (TreeUtils.isRecordLiteralExpr(varDefNode)) { // For structs
                            this.getValueOfStructs(varDefNode, key.fields);
                            key.value = this.getStringifiedMap(key.fields);
                        }
                    }
                }
            }
        });
        this.forceUpdate();
    }

    /**
     * Get already added values to properties
     * @param node
     * @returns {string}
     */
    getAddedValueOfProp(node) {
        let value = '';
        if (TreeUtils.isLiteral(node)) { // If its a direct value
            value = node.getValue();
        } else if (TreeUtils.isSimpleVariableRef(node)) { // If its a reference variable
            value = node.getVariableName().value;
        }
        return value;
    }

    /**
     * Get value of structs
     */
    getValueOfStructs(addedValues, fields) {
        addedValues.getKeyValuePairs().forEach((element) => {
            if (TreeUtils.isRecordLiteralKeyValue(element)) {
                const key = element.getKey().getVariableName().value ||
                    element.getKey().value;
                // If the value is a Literal Node
                if (TreeUtils.isLiteral(element.getValue())
                    || TreeUtils.isSimpleVariableRef(element.getValue())) {
                    const obj = _.find(fields, { identifier: key });
                    obj.value = (this.getAddedValueOfProp(element.getValue()));
                } else if (TreeUtils.isRecordLiteralExpr(element.getValue())) {
                    const propName = _.find(fields, { identifier: key });
                    this.getValueOfStructs(element.getValue(), propName.fields);
                    propName.value = this.getStringifiedMap(propName.fields);
                }
            }
        });
    }

    // TODO: Add the icon to the button
    renderConnectorProps(key) {
        return (

            <Grid id='optionGroup' key={key.identifier}>
                <Grid.Row>
                    <Grid.Column width={6}>
                        <label
                            htmlFor={key.identifier}
                        >
                            {_.startCase(key.identifier)}
                        </label>
                    </Grid.Column>
                    <Grid.Column width={10}>
                        <input
                            id={key.identifier}
                            name={key.identifier}
                            type='text'
                            placeholder='Defined option object or a method'
                            value={key.value}
                            onChange={event => this.onChange(event, key)}
                        />
                        <span className='input-group-btn'>
                            <Button
                                id='viewOptionParams'
                                onClick={() => {
                                    this.toggleConnectorParamProperties(key.identifier, key.connectorParams);
                                }}
                            >
                                <i
                                    className='fw fw-configurations connector-init-configure'
                                />
                            </Button>
                        </span>
                    </Grid.Column>
                </Grid.Row>
            </Grid>);
    }

    /**
     * Renders structs
     * @param key
     * @returns {XML}
     */
    renderStructs(key) {
        const disabled = (((key.value.includes('{') || key.value.includes('}')) &&
        key.value.substring(1, key.value.length - 1).length > 1) ? 'disabled' : '');
        let wrongInput = '';
        if (disabled) {
            const stringifiedValue = this.getStringifiedMap(key.fields);
            if (key.value && key.value !== stringifiedValue) {
                wrongInput = 'wrongInput';
            }
        }
        return (
            <div className='propWindowStruct'>
                <div id='optionGroup' key={key.identifier}>
                    <label
                        htmlFor={key.identifier}
                        className='col-sm-4 property-dialog-label'
                    >
                        {_.startCase(key.identifier)}</label>
                    <div className='col-sm-7'>
                        <div className='input-group'>
                            {wrongInput &&
                            <div className='errorMsgDiv'>
                                <i
                                    className='fw fw-error errorIcon'
                                    onClick={() => {
                                        this.clearStructFieldValues(key, key.fields);
                                    }}
                                />
                                <span className='errorMsg'> Configure properties </span></div>
                            }
                            <input
                                className={['property-dialog-form-control',
                                    disabled ? 'disabledInput' : '', wrongInput ? 'wrongInput' : ''].join(' ')}
                                id={key.identifier}
                                name={key.identifier}
                                type='text'
                                placeholder='Defined option object or a method'
                                value={key.value}
                                onChange={event => this.onChange(event, key)}
                                disabled={disabled}
                            />
                            <span className='input-group-btn'>
                                { (disabled && !wrongInput) &&
                                <input
                                    id='viewOptionParams'
                                    type='button'
                                    value='x'
                                    onClick={() => {
                                        this.clearStructFieldValues(key, key.fields);
                                    }}
                                /> }
                                <input
                                    id='viewOptionParams'
                                    type='button'
                                    value='+'
                                    onClick={() => {
                                        this.toggleStructProperties(key.identifier, key.fields);
                                    }}
                                />
                            </span>
                        </div>
                    </div>
                </div>
            </div>);
    }

    /**
     * Renders tag inputs for form
     * @param key
     * @returns {XML}
     */
    renderTagInputs(key) {
        return (
            <Grid key={key.identifier}>
                <Grid.Row>
                    <Grid.Column width={6}>
                        <label
                            className='col-sm-4 property-dialog-label'
                            htmlFor='tags'
                        >
                            {_.startCase(key.identifier)}
                        </label>
                    </Grid.Column>
                    <Grid.Column width={10}>
                        <TagInput
                            id={key.identifier}
                            taggedElements={key.value}
                            onTagsAdded={event =>
                                this.onTagsAdded(event, key)}
                            removeTagsAdded={this.removeTagsAdded}
                            placeholder={`${_.startCase(key.identifier)} (↵ or comma-separated)`}
                            ref={(node) => {
                                this.node = node;
                            }}
                        />
                    </Grid.Column>
                </Grid.Row>
            </Grid>);
    }

    /**
     * Handle when tags are added to tag inputs
     * @param event
     * @param index
     */
    onTagsAdded(event, key) {
        if (event.keyCode === 13 || event.keyCode === 188) {
            event.preventDefault();
            const { value } = event.target;
            if (!key.value) {
                key.value = [];
            }
            key.value.push(value);
        }

        if (key.value.length && event.keyCode === 8) {
            if (_.includes(key.value, event.target.value) || !event.target.value) {
                this.removeTagsAdded(key.value, key.value.length - 1);
            }
        }
        this.forceUpdate();
    }

    /**
     * Handles the tags that are removed from the tag input
     * @param identifier
     * @param index
     */
    removeTagsAdded(values, index) {
        values.splice(index, 1);
        this.forceUpdate();
    }

    /**
     * Renders the view for a property window
     *
     * @returns {ReactElement} The view.
     * @memberof PropertyWindow
     */
    render() {
        const breadCrumbContainer = this.breadCrumbs.map((key, index) => {
            if (index === this.breadCrumbs.length - 1) {
                return (
                    <li key={`breadCrumb-${key}`}>
                        <a className='currentBreadcrumbItem'> {key}</a>
                    </li>
                );
            } else {
                return (
                    <li key={`breadCrumb-${key}`}><a
                        className='previousBreadcrumbItem'
                        onClick={() => {
                            this.toggleStructView(index);
                        }}
                    > {key}</a></li>
                );
            }
        });
        return (
            <div
                id={`popover-${this.props.model.id}`}
                key={`popover-${this.props.model.id}`}
            >
                <div
                    id='popover-arrow'
                    style={this.props.styles.arrowStyle}
                />
                <div
                    id='property-modal'
                    ref={(node) => {
                        this.node = node;
                    }}
                    key={`propertiesForm/${this.props.model.id}`}
                    style={this.props.styles.popover}
                >
                    <div className='formHeader'>
                        <Button
                            id='dismissBtn'
                            type='button'
                            className='close'
                            aria-label='Close'
                            onClick={this.closePropertyWindow}
                        >
                            <span aria-hidden='true'>&times;</span>
                        </Button>
                        <h5 className='form-title file-dialog-title'>
                            {this.props.formHeading}</h5>
                        {this.props.propertiesExist &&
                        <ul id='propWindowBreadcrumb'>
                            {breadCrumbContainer}
                        </ul>
                        }
                    </div>
                    {!this.props.propertiesExist &&
                    <div className='form-body noPropertyPrompt'>
                        <span>
                            <h5 className='alertMsgForNoProps'> No properties to be configured</h5>
                        </span>
                    </div>
                    }
                    {this.props.propertiesExist &&
                    <Grid className='form-body formContainer'>
                        <Grid.Row>
                            <Form size='small'>
                                <Form.Group>

                                    {!_.isNil(this.isVarDefEnabled) &&
                                this.renderVariableReferenceSection(this.varDefInitRef)}
                                    <div
                                        className={this.isVarDefEnabled ? 'content-disabled' : ''}
                                    >
                                        {this.state.properties.map((key) => {
                                            if (key.bType === 'int') {
                                                return this.renderNumericInputs(key);
                                            } else if (key.bType === 'string') {
                                                return this.renderTextInputs(key);
                                            } else if (key.bType === 'boolean') {
                                                let booleanValue = false;
                                                if (key.value) {
                                                    booleanValue = JSON.parse(key.value);
                                                }
                                                return this.renderBooleanInputs(key, booleanValue);
                                            } else if (key.bType === 'array') {
                                                return this.renderTagInputs(key);
                                            } else if (key.bType === 'map') {
                                                return this.renderTextInputs(key);
                                            } else if (key.bType === 'struct') {
                                                return this.renderStructs(key);
                                            } else if (key.bType === 'enum') {
                                                return this.renderSelectBox(key);
                                            } else if (key.isConnector) {
                                                return this.renderConnectorProps(key);
                                            } else { // If not any of the types render a simple text box
                                                return this.renderTextInputs(key);
                                            }
                                        })}
                                    </div>
                                </Form.Group>
                            </Form>
                        </Grid.Row>
                    </Grid>
                    }
                    <div className='formFooter'>
                        {!_.isEmpty(this.previousItems) &&
                        <Button
                            // className='btn propWindowBackBtn'
                            type='button'
                            onClick={this.goToPreviousView}
                        > <i className='fw fw-left propWindowBackIcon' /> Back </Button>
                                }
                        {this.props.propertiesExist &&
                        <Button
                            primary
                            type='button'
                            // className='propWindowApplyBtn btn'
                            onClick={this.handleDismiss}
                        >Apply</Button>
                        }
                        <Button
                            type='button'
                            // className='btn propWindowCancelBtn'
                            onClick={this.closePropertyWindow}
                        >Cancel</Button>

                    </div>
                </div>
            </div>);
    }
}

PropertyWindow.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};
export default PropertyWindow;
