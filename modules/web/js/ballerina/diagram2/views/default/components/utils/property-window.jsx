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
        this.state = {
            properties: this.supportedKeys,
        };
        this.onChange = this.onChange.bind(this);
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

    /**
     * Hanldes the dismiss/cancel event of the prop window
     */
    handleDismiss() {
        this.props.addedValues(this.supportedKeys);
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
            <div key={key.identifier} className="form-group">
                <label
                    htmlFor={key.identifier}
                    className='col-sm-4 property-dialog-label'
                >
                    {_.startCase(key.identifier)}</label>
                <div className='col-sm-7'>
                    <input
                        className='property-dialog-form-control'
                        id={key.identifier}
                        name={key.identifier}
                        type='text'
                        placeholder={key.identifier}
                        value={value}
                        onChange={event => this.onChange(event, key)}
                    />
                </div>
            </div>);
    }

    /**
     * Renders numeric input for form
     * @param key
     * @returns {XML}
     */
    renderNumericInputs(key) {
        return (
            <div key={key.identifier} className="form-group">
                <label
                    htmlFor={key.identifier}
                    className='col-sm-4 property-dialog-label'
                >
                    {_.startCase(key.identifier)}</label>
                <div className='col-sm-7'>
                    <input
                        className='property-dialog-form-control'
                        id={key.identifier}
                        name={key.identifier}
                        type='number'
                        placeholder={key.identifier}
                        value={key.value}
                        onChange={event => this.onChange(event, key)}
                    />
                </div>
            </div>);
    }

    /**
     * Renders boolean input for form
     * @param key
     * @param booleanValue
     * @returns {XML}
     */
    renderBooleanInputs(key, booleanValue) {
        return (
            <div key={key.identifier} className="form-group">
                <label
                    htmlFor={key.identifier}
                    className='col-sm-4 property-dialog-label'
                >
                    {_.startCase(key.identifier)}</label>
                <div className='col-sm-7 properties-checkbox'>
                    <input
                        className="toggle"
                        type="checkbox"
                        id={key.identifier}
                        checked={booleanValue}
                        onChange={event => this.onChange(event, key)}
                    />
                </div>
            </div>);
    }
    /**
     * Renders structs
     * @param key
     * @returns {XML}
     */
    renderStructs(key) {
        return (<div className="propWindowStruct">
            <div id='optionGroup' key={key.identifier} className="form-group">
                <label
                    htmlFor={key.identifier}
                    className='col-sm-4 property-dialog-label'
                >
                    {_.startCase(key.identifier)}</label>
                <div className='col-sm-7'>
                    <input
                        className='property-dialog-form-control'
                        id={key.identifier}
                        name={key.identifier}
                        type='text'
                        placeholder='Defined option object or a method'
                        value={key.value}
                        onChange={event => this.onChange(event, key)}
                    />
                    <input
                        id='viewOptionParams'
                        type='button'
                        value='+'
                        onClick={() => { this.toggleStructProperties(key.identifier, key.fields); }}
                    />
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
        return (<div key={key.identifier} className="form-group">
            <label
                className="col-sm-4 property-dialog-label"
                htmlFor="tags"
            >{_.startCase(key.identifier)}</label>
            <div className='col-sm-7 properties-tags'>
                <TagInput
                    id={key.identifier}
                    taggedElements={key.value}
                    onTagsAdded={event =>
                        this.onTagsAdded(event, key)}
                    removeTagsAdded={this.removeTagsAdded}
                    placeholder={key.identifier}
                    ref={(node) => { this.node = node; }}
                />
            </div>
        </div>);
    }

    /**
     * Handle when tags are added to tag inputs
     * @param event
     * @param index
     */
    onTagsAdded(event, key) {
        if (event.keyCode === 13) {
            event.preventDefault();
            const { value } = event.target;
            if (!key.value) {
                key.value = [];
            }
            key.value.push(value);
        }

        if (key.value.length && event.keyCode === 8) {
            this.removeTagsAdded(key.value, key.value.length - 1);
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
                    <li><a className="currentBreadcrumbItem"> {key}</a></li>
                );
            } else {
                return (
                    <li><a
                        className="previousBreadcrumbItem"
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
                    id="popover-arrow"
                    style={this.props.styles.arrowStyle}
                />
                <div
                    id="property-modal"
                    ref={(node) => { this.node = node; }}
                    key={`propertiesForm/${this.props.model.id}`}
                    style={this.props.styles.popover}
                >
                    <div className="formHeader">
                        <button
                            id='dismissBtn'
                            type="button"
                            className="close"
                            aria-label="Close"
                            onClick={this.closePropertyWindow}
                        >
                            <span aria-hidden="true">&times;</span>
                        </button>
                        <h5 className="form-title file-dialog-title">
                            {this.props.formHeading}</h5>
                        {this.props.propertiesExist &&
                        <ul id="propWindowBreadcrumb">
                            {breadCrumbContainer}
                        </ul>
                        }
                    </div>
                    {!this.props.propertiesExist &&
                    <div className="form-body noPropertyPrompt">
                        <span>
                            <h5 className="alertMsgForNoProps"> No properties to be configured</h5>
                        </span>
                    </div>
                    }
                    {this.props.propertiesExist &&
                    <div className="form-body formContainer">
                        <div className="container-fluid">
                            <form className='form-horizontal propertyForm'>
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
                                    } else {
                                        return this.renderStructs(key);
                                    }
                                })}
                            </form>
                        </div>
                    </div>
                    }
                    <div className="formFooter">
                        {!_.isEmpty(this.previousItems) &&
                            <button
                                className="btn btn-primary propWindowBackBtn"
                                type='button'
                                onClick={this.goToPreviousView}
                            > <i className="fw fw-left propWindowBackIcon" /> Back </button>
                        }
                        <button
                            type="button"
                            className="btn btn-primary propWindowCancelBtn"
                            onClick={this.closePropertyWindow}
                        >Cancel</button>
                        {this.props.propertiesExist &&
                        <button
                            type="button"
                            className="propWindowApplyBtn btn"
                            onClick={this.handleDismiss}
                        >Apply</button>
                        }
                    </div>
                </div>
            </div>);
    }
}

PropertyWindow.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};
export default PropertyWindow;
