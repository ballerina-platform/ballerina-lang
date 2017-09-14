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
import ConnectorHelper from '../../../../env/helpers/connector-helper';
import './properties-form.css';
import TagInput from './tag-input';
import ASTFactory from '../../../../ast/ast-factory';
/**
 * React component for a service definition.
 *
 * @class ServiceDefinition
 * @extends {React.Component}
 */
class ConnectorPropertiesForm extends React.Component {

    constructor(props) {
        super(props);
        const data = {};
        this.getSupportedProps().map((params) => {
            /* if (params.fields) {
                params.fields.map((structField) => {
                    key = structField.getName();
                    data[key] = structField.getDefaultValue();
                });
            } else {*/
            const key = params.identifier;
            data[key] = params.defaultValue;
            // }
        });

        this.state = { data };
        this.onChange = this.onChange.bind(this);
        this.onTagsAdded = this.onTagsAdded.bind(this);
        this.removeTagsAdded = this.removeTagsAdded.bind(this);
        this.getSupportedProps = this.getSupportedProps.bind(this);
        this.getDataAddedToConnectorInit = this.getDataAddedToConnectorInit.bind(this);
        this.setDataToConnectorInitArgs = this.setDataToConnectorInitArgs.bind(this);
        this.handleDismiss = this.handleDismiss.bind(this);
        this.handleOutsideClick = this.handleOutsideClick.bind(this);
        this.renderNumericInputs = this.renderNumericInputs.bind(this);
        this.renderTextInputs = this.renderTextInputs.bind(this);
        this.renderBooleanInputs = this.renderBooleanInputs.bind(this);
        this.renderTagInputs = this.renderTagInputs.bind(this);
        this.renderStructs = this.renderStructs.bind(this);
        this.connectorInstanceString = this.connectorInstanceString.bind(this);
        this.getAddedValueOfProp = this.getAddedValueOfProp.bind(this);
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

    getAddedValueOfProp(node) {
        let value = '';
        if (ASTFactory.isBasicLiteralExpression(node)) {
            value = node.getBasicLiteralValue();
        } else if (ASTFactory.isSimpleVariableReferenceExpression(node)) {
            value = node.getVariableName();
        }
        return value;
    }
    getSupportedProps() {
        const pkgName = this.props.model.getDeclarationStatement().getRightExpression()
            .getConnectorName().getPackageName();
        const connectorProps = ConnectorHelper.getConnectorParameters(this.props.environment, pkgName);
        const addedValues = this.getDataAddedToConnectorInit();
        connectorProps.map((property, index) => {
            if (addedValues.length > 0) {
                property.defaultValue = this.getAddedValueOfProp(addedValues[index]);
            }
        });
        return connectorProps;
    }

    connectorInstanceString(connectorInit, data) {
        let spacesBeforeNameRef = '';
        let spacesNameRefToArgStart = '';

        if (connectorInit.getIsFilterConnectorInitExpr()) {
            spacesBeforeNameRef = connectorInit.whiteSpace.useDefault ? ' ' : connectorInit.getWSRegion(0);
            spacesNameRefToArgStart = connectorInit.whiteSpace.useDefault ? ' ' : connectorInit.getWSRegion(1);
        } else {
            spacesBeforeNameRef = connectorInit.whiteSpace.useDefault ? ' ' : connectorInit.getWSRegion(1);
            spacesNameRefToArgStart = connectorInit.whiteSpace.useDefault ? ' ' : connectorInit.getWSRegion(2);
        }

        let connectorInstanceString = spacesBeforeNameRef +
            connectorInit.getConnectorName().toString().trim()
            + spacesNameRefToArgStart
            + '(';

        Object.keys(data).forEach((key, index) => {
            if (index !== 0) {
                connectorInstanceString += ', ';
            }
            this.getSupportedProps().map((property) => {
                if (key === property.identifier) {
                    switch (property.bType) {
                        case 'string':
                            connectorInstanceString += JSON.stringify(data[key]);
                            break;
                        default:
                            connectorInstanceString += data[key];
                    }
                }
            });
        });

        return connectorInstanceString + ')';
    }

    setDataToConnectorInitArgs(data) {
        const connectorInit = this.props.model.getDeclarationStatement().getRightExpression();
        const expr = 'create' + this.connectorInstanceString(connectorInit, data);
        connectorInit.setExpressionFromString(expr);
        console.log(connectorInit.getExpressionString());
    }

    getDataAddedToConnectorInit() {
        return this.props.model.getDeclarationStatement().getRightExpression().getArgs();
    }

    handleDismiss() {
        this.setDataToConnectorInitArgs(this.state.data);
        this.props.model.getViewState().showPropertyForm = !this.props.model.getViewState().showPropertyForm;
        this.props.editor.update();
    }

    onChange(event, index) {
        const value = event.target.type === 'checkbox' ? event.target.checked : event.target.value;
        this.setState({ data: _.extend(this.state.data, { [index]: value }) });
    }

    onTagsAdded(event, index) {
        if (event.keyCode === 13) {
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

    removeTagsAdded(identifier, index) {
        this.setState({ data: _.extend(this.state.data, { [identifier]:
            this.state.data[identifier].filter((item, i) => i !== index),
        }) });
    }

    handleOutsideClick(e) {
        if (this.node) {
            if (!this.node.contains(e.target)) {
                this.handleDismiss();
            }
        }
    }

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
                        required
                    />
                </div>
            </div>);
    }

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
                        required
                    />
                </div>
            </div>);
    }

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
                    required
                />
            </div>
        </div>);
    }

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
     * Renders the view for a service definition.
     *
     * @returns {ReactElement} The view.
     * @memberof ServiceDefinition
     */
    render() {
        const supportedProps = this.getSupportedProps();
        const positionX = (this.props.bBox.x) + 'px';
        const positionY = (this.props.bBox.y) + 'px';
        let visibility = 'none';
        if (this.props.visibility === true && this.getSupportedProps().length > 0) {
            visibility = 'block';
        }
        const styles = {
            display: visibility,
            top: positionY,
            left: positionX,
           // maxHeight: this.props.model.getViewState().components.statementContainer.h + 30,
            overflowY: 'scroll',
            overflowX: 'hidden',
        };
        return (
            <div
                id="connector-property-modal"
                style={styles}
                ref={(node) => { this.node = node; }}
                key={`connectorProp/${this.props.model.id}`}
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
                    <h4 className="form-title file-dialog-title">
                        Connector Properties</h4>
                    <hr className="style1" />
                </div>
                <div className="form-body">
                    <div className="container-fluid">
                        <form className='form-horizontal propertyForm'>
                            {supportedProps.map((key) => {
                                if (key.bType === 'int') {
                                    return this.renderNumericInputs(key);
                                } else if (key.bType === 'string' || key.bType === 'map') {
                                    return this.renderTextInputs(key);
                                } else if (key.bType === 'boolean') {
                                    let booleanValue = false;
                                    if (this.state.data[key.identifier]) {
                                        booleanValue = JSON.parse(this.state.data[key.identifier]);
                                    }
                                    return this.renderBooleanInputs(key, booleanValue);
                                } else if (key.bType === 'array') {
                                    return this.renderTagInputs(key);
                                } else if (key.bType === 'ConnectionProperties') {
                                   // return this.renderStructs(key);
                                    return this.renderTextInputs(key);
                                }
                            })}
                        </form>
                    </div>
                </div>
            </div>);
    }
}

export default ConnectorPropertiesForm;

ConnectorPropertiesForm.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
};
