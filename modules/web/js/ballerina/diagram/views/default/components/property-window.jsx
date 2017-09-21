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
            const key = addedAnnotation.identifier;
            data[key] = addedAnnotation.value;
        });

        this.state = { data,
            pageOfItems: [],
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
    }

    componentDidMount() {
        if (this.props.model.getViewState().showPropertyForm) {
            document.addEventListener('mouseup', this.handleOutsideClick, false);
        } else {
            document.removeEventListener('mouseup', this.handleOutsideClick, false);
        }
    }

    componentDidUpdate() {
        if (this.props.model.getViewState().showPropertyForm) {
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
        this.props.model.getViewState().showPropertyForm = !this.props.model.getViewState().showPropertyForm;
        this.props.editor.update();
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
     * Renders text input for form
     * @param key
     * @returns {XML}
     */
    renderTextInputs(key) {
        return (
            <div key={key.identifier} className="form-group">
                <label
                    htmlFor={key.identifier}
                    className='col-sm-3 property-dialog-label'
                >
                    {key.identifier}</label>
                <div className='col-sm-8'>
                    <input
                        className='property-dialog-form-control'
                        id={key.identifier}
                        name={key.identifier}
                        type='text'
                        placeholder={key.identifier}
                        value={this.state.data[key.identifier]}
                        onChange={event => this.onChange(event, key.identifier)}
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
                    className='col-sm-3 property-dialog-label'
                >
                    {key.identifier}</label>
                <div className='col-sm-8'>
                    <input
                        className='property-dialog-form-control'
                        id={key.identifier}
                        name={key.identifier}
                        type='number'
                        placeholder={key.identifier}
                        value={this.state.data[key.identifier]}
                        onChange={event => this.onChange(event, key.identifier)}
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
                    className='col-sm-3 property-dialog-label'
                >
                    {key.identifier}</label>
                <div className='col-sm-8 properties-checkbox'>
                    <input
                        className="toggle"
                        type="checkbox"
                        id={key.identifier}
                        checked={booleanValue}
                        onChange={event => this.onChange(event, key.identifier)}
                    />
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
                className="col-sm-3 property-dialog-label"
                htmlFor="tags"
            >{key.identifier}</label>
            <div className='col-sm-8 properties-tags'>
                <TagInput
                    id={key.identifier}
                    taggedElements={this.state.data[key.identifier]}
                    onTagsAdded={event =>
                        this.onTagsAdded(event, key.identifier)}
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
        const dataTarget = '#' + key.identifier;
        return (<div className="structsContainer" id="accordion">
            <div className="panel panel-default" id="panel">
                <div className="collapsiblePanelHeaderDiv panel-heading">
                    <h4 className="collapsibleTitle">
                        <a
                            data-toggle="collapse"
                            data-target={dataTarget}
                            href="#"
                        >
                            {key.bType}
                        </a>
                    </h4>
                </div>
                <div id={key.identifier} className="panel-collapse collapse">
                    <div className="collapsiblePanelBody panel-body">
                        { key.fields.map((field) => {
                            const fieldName = { identifier: field.getName() };
                            switch (field.getType()) {
                                case 'string':
                                    return this.renderTextInputs(fieldName);
                                    break;
                                case 'int':
                                    return this.renderNumericInputs(fieldName);
                                    break;
                                case 'boolean':
                                    let booleanValue = false;
                                    if (this.state.data[fieldName.identifier]) {
                                        booleanValue = JSON.parse(this.state.data[fieldName.identifier]);
                                    }
                                    return this.renderBooleanInputs(fieldName, booleanValue);
                                    break;
                                case 'array':
                                    return this.renderTagInputs(fieldName);
                                    break;
                            }
                        })}
                    </div>
                </div>
            </div>
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

export default PropertiesWindow;
