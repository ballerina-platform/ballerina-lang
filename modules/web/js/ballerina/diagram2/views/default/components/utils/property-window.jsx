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
import Pagination from './pagination';

/**
 * React component for a service definition.
 *
 * @class ServiceDefinition
 * @extends {React.Component}
 */
class PropertiesWindow extends React.Component {

    constructor(props) {
        super(props);
        const data = {};
        this.props.supportedProps.map((addedAnnotation) => {
            let key = addedAnnotation.identifier;
            data[key] = addedAnnotation.value;
            if (addedAnnotation.fields) {
                addedAnnotation.fields.map((field) => {
                    key = addedAnnotation.identifier + ':' + (field.getName());
                    data[key] = field.getDefaultValue();
                });
            }
        });

        this.state = { data,
            pageOfItems: [],
            expandProperties: false,
        };
        this.onChange = this.onChange.bind(this);
        this.onTagsAdded = this.onTagsAdded.bind(this);
        this.removeTagsAdded = this.removeTagsAdded.bind(this);
        this.handleDismiss = this.handleDismiss.bind(this);
        this.handleOutsideClick = this.handleOutsideClick.bind(this);
        this.renderNumericInputs = this.renderNumericInputs.bind(this);
        this.renderTextInputs = this.renderTextInputs.bind(this);
        this.renderBooleanInputs = this.renderBooleanInputs.bind(this);
        this.renderTagInputs = this.renderTagInputs.bind(this);
        this.onChangePage = this.onChangePage.bind(this);
        this.toggleStructProperties = this.toggleStructProperties.bind(this);
    }

    componentDidMount() {
        if (this.props.model.viewState.showOverlayContainer) {
            document.addEventListener('mouseup', this.handleOutsideClick, false);
        } else {
            document.removeEventListener('mouseup', this.handleOutsideClick, false);
        }
    }

    componentDidUpdate() {
        if (this.props.model.viewState.showOverlayContainer) {
            document.addEventListener('mouseup', this.handleOutsideClick, false);
        } else {
            document.removeEventListener('mouseup', this.handleOutsideClick, false);
        }
    }

    componentWillUnmount() {
        document.removeEventListener('mouseup', this.handleOutsideClick, false);
    }

    /**
     * On page change event
     * @param pageOfItems
     */
    onChangePage(pageOfItems) {
        // update state with new page of items
        this.setState({ pageOfItems });
    }

    /**
     * On change event for form inputs
     * @param event
     * @param index
     */
    onChange(event, index) {
        const value = event.target.type === 'checkbox' ? event.target.checked : event.target.value;
        this.setState({ data: _.extend(this.state.data, { [index]: value }) });
    }

    /**
     * Handle when tags are added to tag inputs
     * @param event
     * @param index
     */
    onTagsAdded(event, index) {
        if (event.keyCode === 13) {
            event.preventDefault();
            const { value } = event.target;
            if (this.state.data[index] === undefined) {
                this.setState({ data: _.extend(this.state.data,
                    { [index]: [] }) });
            }
            const obj = { [index]: [...this.state.data[index], value] };
            this.setState({ data: _.extend(this.state.data, obj) });
        }

        if ([this.state.data[index]].length && event.keyCode === 8) {
            this.removeTagsAdded(index, this.state.data[index].length - 1);
        }
    }

    /**
     * Hanldes the dismiss/cancel event of the prop window
     */
    handleDismiss() {
        this.props.addedValues(this.state.data);
        this.props.model.viewState.showOverlayContainer = false;
        this.props.model.viewState.overlayContainer = {};
    }

    /**
     * Handles the tags that are removed from the tag input
     * @param identifier
     * @param index
     */
    removeTagsAdded(identifier, index) {
        this.setState({ data: _.extend(this.state.data, { [identifier]:
            this.state.data[identifier].filter((item, i) => i !== index),
        }) });
    }

    /**
     * Handles the outside click of the prop window
     * @param e
     */
    handleOutsideClick(e) {
        if (this.node) {
            if (!this.node.contains(e.target)) {
                this.handleDismiss();
            }
        }
    }

    /**
     *
     */
    toggleStructProperties() {
        if (this.state.expandProperties) {
            this.setState({
                expandProperties: false,
            });
        } else {
            this.setState({
                expandProperties: true,
            });
        }
    }
    /**
     * Renders text input for form
     * @param key
     * @returns {XML}
     */
    renderTextInputs(key, identifier) {
        return (
            <div key={key.identifier} className="form-group">
                <label
                    htmlFor={key.identifier}
                    className='col-sm-4 property-dialog-label'
                >
                    {key.identifier}</label>
                <div className='col-sm-7'>
                    <input
                        className='property-dialog-form-control'
                        id={key.identifier}
                        name={key.identifier}
                        type='text'
                        placeholder={key.identifier}
                        value={this.state.data[identifier] || this.state.data[key.identifier]}
                        onChange={event => this.onChange(event, identifier || key.identifier)}
                    />
                </div>
            </div>);
    }

    /**
     * Renders numeric input for form
     * @param key
     * @returns {XML}
     */
    renderNumericInputs(key, identifier) {
        return (
            <div key={key.identifier} className="form-group">
                <label
                    htmlFor={key.identifier}
                    className='col-sm-4 property-dialog-label'
                >
                    {key.identifier}</label>
                <div className='col-sm-7'>
                    <input
                        className='property-dialog-form-control'
                        id={key.identifier}
                        name={key.identifier}
                        type='number'
                        placeholder={key.identifier}
                        value={this.state.data[identifier] || this.state.data[key.identifier]}
                        onChange={event => this.onChange(event, identifier || key.identifier)}
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
    renderBooleanInputs(key, booleanValue, identifier) {
        return (
            <div key={key.identifier} className="form-group">
                <label
                    htmlFor={key.identifier}
                    className='col-sm-4 property-dialog-label'
                >
                    {key.identifier}</label>
                <div className='col-sm-7 properties-checkbox'>
                    <input
                        className="toggle"
                        type="checkbox"
                        id={key.identifier}
                        checked={booleanValue}
                        onChange={event => this.onChange(event, identifier || key.identifier)}
                    />
                </div>
            </div>);
    }

    /**
     * Renders tag inputs for form
     * @param key
     * @returns {XML}
     */
    renderTagInputs(key, identifier) {
        return (<div key={key.identifier} className="form-group">
            <label
                className="col-sm-4 property-dialog-label"
                htmlFor="tags"
            >{key.identifier}</label>
            <div className='col-sm-7 properties-tags'>
                <TagInput
                    id={key.identifier}
                    taggedElements={this.state.data[identifier] || this.state.data[key.identifier]}
                    onTagsAdded={event =>
                        this.onTagsAdded(event, identifier || key.identifier)}
                    removeTagsAdded={this.removeTagsAdded}
                    placeholder={key.identifier}
                    ref={(node) => { this.node = node; }}
                />
            </div>
        </div>);
    }

    /**
     * Renders structs / collapsible divs for form
     * @param key
     * @returns {XML}
     */
    renderStructs(key) {
        // Filter all struct field values and check if they have values
        /* let atleastOneFieldValuExists = true;
        Object.keys(this.state.data).forEach((key) => {
            if (key.startsWith('connectorOptions:')) {
                if (!this.state.data[key]) {
                    atleastOneFieldValuExists = false;
                }
            }
        });*/
        return (<div className="structsContainer">
            <div id='optionGroup' key={key.identifier} className="form-group">
                <label
                    htmlFor={key.identifier}
                    className='col-sm-4 property-dialog-label'
                >
                    {key.identifier}</label>
                <div className='col-sm-7'>
                    <input
                        className='property-dialog-form-control'
                        id={key.identifier}
                        name={key.identifier}
                        type='text'
                        placeholder='Specify a defined option object or a method'
                        value={this.state.data[key.identifier]}
                        onChange={event => this.onChange(event, key.identifier)}
                        // TODO disabling removed because of the inconsistency of the struct type
                        /* disabled={((this.state.expandProperties ||
                        (!this.state.data[key.identifier] && atleastOneFieldValuExists)))}*/
                    />
                </div>
                {(key.fields !== null && (!this.state.data[key.identifier])) &&
                <div className='col-sm-1'>
                    <input
                        id='viewOptionParams'
                        type='button'
                        value={this.state.expandProperties ? '-' : '+'}
                        onClick={this.toggleStructProperties}
                    />
                </div> }
            </div>
            {(key.fields !== null) &&
            <div
                id={key.identifier}
                className="optionsDiv"
                style={{ display: this.state.expandProperties ? 'block' : 'none' }}
            >
                <div className="collapsiblePanelBody" style={{ paddingLeft: '3px' }}>
                    <label> Specify the Option attributes</label>
                    { key.fields.map((field) => {
                        const fieldName = { identifier: field.getName() };
                        const identifier = (key.identifier) + ':' + fieldName.identifier;
                        switch (field.getType()) {
                            case 'string':
                                return this.renderTextInputs(fieldName, identifier);
                                break;
                            case 'int':
                                return this.renderNumericInputs(fieldName, identifier);
                                break;
                            case 'boolean':
                                let booleanValue = false;
                                if (this.state.data[identifier]) {
                                    booleanValue = JSON.parse(this.state.data[identifier]);
                                }
                                return this.renderBooleanInputs(fieldName, booleanValue, identifier);
                                break;
                            case 'array':
                                return this.renderTagInputs(fieldName, identifier);
                                break;
                        }
                    })}
                </div>
            </div> }
        </div>);
    }
    /**
     * Renders the view for a property window
     *
     * @returns {ReactElement} The view.
     * @memberof PropertyWindow
     */
    render() {
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
                    <div className="form-header">
                        <button
                            id='dismissBtn'
                            type="button"
                            className="close"
                            aria-label="Close"
                            onClick={this.handleDismiss}
                        >
                            <span aria-hidden="true">&times;</span>
                        </button>
                        <h5 className="form-title file-dialog-title">
                            {this.props.formHeading}</h5>
                    </div>
                    <div className="form-body">
                        <div className="container-fluid">
                            <form className='form-horizontal propertyForm'>
                                {this.state.pageOfItems.map((key) => {
                                    if (key.bType === 'int') {
                                        return this.renderNumericInputs(key);
                                    } else if (key.bType === 'string') {
                                        return this.renderTextInputs(key);
                                    } else if (key.bType === 'boolean') {
                                        let booleanValue = false;
                                        if (this.state.data[key.identifier]) {
                                            booleanValue = JSON.parse(this.state.data[key.identifier]);
                                        }
                                        return this.renderBooleanInputs(key, booleanValue);
                                    } else if (key.bType === 'array') {
                                        return this.renderTagInputs(key);
                                    } else {
                                        return this.renderStructs(key);
                                    }
                                })}
                            </form>
                        </div>
                    </div>
                    <Pagination items={this.props.supportedProps} onChangePage={this.onChangePage} />
                </div>
            </div>);
    }
}

PropertiesWindow.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};
export default PropertiesWindow;
