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
import ASTFactory from '../../../../ast/ast-factory';
import AnnotationHelper from '../../../../env/helpers/annotation-helper';
import './properties-form.css';
import TagInput from './tag-input';

/**
 * React component for a service definition.
 *
 * @class ServiceDefinition
 * @extends {React.Component}
 */
class ServerConnectorPropertiesForm extends React.Component {

    constructor(props) {
        super(props);
        const data = {};
        this.getSupportedKeys().map((addedAnnotation) => {
            const key = addedAnnotation.identifier;
            data[key] = addedAnnotation.value;
        });

        this.state = { data };
        this.onChange = this.onChange.bind(this);
        this.onTagsAdded = this.onTagsAdded.bind(this);
        this.removeTagsAdded = this.removeTagsAdded.bind(this);
        this.getDataFromPropertyForm = this.getDataFromPropertyForm.bind(this);
        this.getSupportedKeys = this.getSupportedKeys.bind(this);
        this.handleDismiss = this.handleDismiss.bind(this);
        this.getBTypeOfConfigurationAttribute = this.getBTypeOfConfigurationAttribute.bind(this);
        this.isArrayTypeConfigurationAttribute = this.isArrayTypeConfigurationAttribute.bind(this);
        this.getProtocolPkgPath = this.getProtocolPkgPath.bind(this);
        this.handleOutsideClick = this.handleOutsideClick.bind(this);
        this.renderNumericInputs = this.renderNumericInputs.bind(this);
        this.renderTextInputs = this.renderTextInputs.bind(this);
        this.renderBooleanInputs = this.renderBooleanInputs.bind(this);
        this.renderTagInputs = this.renderTagInputs.bind(this);
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

    isArrayTypeConfigurationAttribute(value) {
        const annotationAttributeDef = AnnotationHelper.getAttributeDefinition(
            this.props.environment, value, this.getProtocolPkgPath(this.props.model.getProtocolPkgName()),
            'configuration');
        return annotationAttributeDef.isArrayType();
    }

    getProtocolPkgPath(protocolPkgName) {
        let protocolPkgPath;
        switch (protocolPkgName) {
            case 'http':
                protocolPkgPath = 'ballerina.net.http';
                break;
            case 'ws':
                protocolPkgPath = 'ballerina.net.ws';
                break;
            case 'jms':
                protocolPkgPath = 'ballerina.net.jms';
                break;
            case 'file':
                protocolPkgPath = 'ballerina.net.ftp';
                break;
        }
        return protocolPkgPath;
    }

    getDataFromPropertyForm(data) {
        if (!ASTFactory.isAnnotationAttachment(this.props.model.getChildren()[0])) {
            const serviceConfigAnnotation = ASTFactory.createAnnotationAttachment({
                fullPackageName: this.props.model.getProtocolPkgPath(),
                packageName: this.props.model.getProtocolPkgName(),
                name: 'configuration',
            });
            this.props.model.addChild(serviceConfigAnnotation, 0);
        }
        const annotationContainer = this.props.model.getChildren()[0];
        let methodAttribute,
            methodsValue,
            getValue;
        Object.keys(data).forEach((key) => {
            if (data[key] || (typeof (data[key]) === 'boolean')) {
                let exists = false;
                // For Arrays
                if (this.isArrayTypeConfigurationAttribute(key)) {
                    Object.keys(annotationContainer.children).forEach((annotationKey) => {
                        if (annotationContainer.children[annotationKey].getKey() === key) {
                            exists = true;
                            const childValues = annotationContainer.children[annotationKey].getChildren()[0]
                                .getChildren();
                            if (data[key].length > 0) {
                                data[key].map((value) => {
                                    childValues.map((existingValue) => {
                                        if (!(data[key].some(item => existingValue.getChildren()[0]
                                                .getStringValue() === item))) {
                                            existingValue.getParent().removeChild(existingValue);
                                        }
                                    });

                                    if (!(childValues.some(item => value === item.getChildren()[0]
                                            .getStringValue()))) {
                                        methodsValue = ASTFactory.createAnnotationAttributeValue();
                                        getValue = ASTFactory.createBValue({
                                            stringValue: value,
                                        });
                                        methodsValue.addChild(getValue);
                                        annotationContainer.children[annotationKey].getChildren()[0]
                                            .addChild(methodsValue);
                                    }
                                });
                            } else {
                                const node = annotationContainer.children[annotationKey];
                                node.getParent().removeChild(node);
                            }
                        }
                    });

                    if (!exists) {
                        methodAttribute = ASTFactory.createAnnotationAttribute({
                            key,
                        });
                        methodsValue = ASTFactory.createAnnotationAttributeValue();
                        const attributes = data[key];
                        attributes.map((name) => {
                            const methodsArray = ASTFactory.createAnnotationAttributeValue();
                            getValue = ASTFactory.createBValue({
                                stringValue: name,
                            });
                            methodsArray.addChild(getValue);
                            methodsValue.addChild(methodsArray);
                        });
                        methodAttribute.addChild(methodsValue);
                        annotationContainer.addChild(methodAttribute);
                    }
                } else {
                    // Others
                    Object.keys(annotationContainer.children).forEach((annotationKey) => {
                        if (annotationContainer.children[annotationKey].getKey() === key) {
                            exists = true;
                            annotationContainer.children[annotationKey].getValue()
                                .getChildren()[0].setStringValue(data[key]);
                        }
                    });
                    if (!exists) {
                        methodAttribute = ASTFactory.createAnnotationAttribute({
                            key,
                        });
                        methodsValue = ASTFactory.createAnnotationAttributeValue();
                        getValue = ASTFactory.createBValue({
                            type: this.getBTypeOfConfigurationAttribute(key),
                            stringValue: data[key],
                        });
                        methodsValue.addChild(getValue);
                        methodAttribute.addChild(methodsValue);
                        annotationContainer.addChild(methodAttribute);
                    }
                }
            }
        });
    }

    getAddedValues() {
        const addedValues = [];
        if (ASTFactory.isAnnotationAttachment(this.props.model.getChildren()[0])) {
            const annotationContainer = this.props.model.getChildren()[0];
            const children = annotationContainer.getChildren();
            children.map((annotation) => {
                let value;
                // If an empty annotation attribute value array exists without any attribute values
                // then remove it from the parent
                if (annotation.getChildren()[0].getChildren().length === 0) {
                    annotation.getParent().removeChild(annotation);
                }
                // For array types
                else if ((annotation.getChildren()[0].getChildren()[0].getChildren()).length > 0) {
                    // Of array type
                    value = [];
                    annotation.getChildren()[0].getChildren().map((child) => {
                        value.push(child.getChildren()[0].getStringValue());
                    });
                } else {
                    value = annotation.getChildren()[0].getChildren()[0].getStringValue().toString();
                }
                const keyValuePair = { identifier: annotation.getKey(), value };

                addedValues.push(keyValuePair);
            });
        }
        return addedValues;
    }

    getSupportedKeys() {
        const supportedKeysArray = [];
        const addedValues = this.getAddedValues();
        const protocolPkgPath = this.getProtocolPkgPath(this.props.model.getProtocolPkgName());
        this.supportedAttributes = AnnotationHelper.getAttributes(
            this.props.environment, protocolPkgPath, 'configuration');

        this.annotationAttachments = AnnotationHelper.getAnnotationAttachments(
            this.props.environment, protocolPkgPath, 'configuration');

        this.supportedAttributes.map((attributeDefinition) => {
            let value = '';
            let annotationDesc = '';
            addedValues.map((addedAnnotation) => {
                if (addedAnnotation.identifier === attributeDefinition.getIdentifier()) {
                    value = addedAnnotation.value;
                }
            });

            this.annotationAttachments.map((annotationAttachment) => {
                if (annotationAttachment.getKey().bValue === attributeDefinition.getIdentifier()) {
                    annotationDesc = annotationAttachment.getValue().bValue;
                }
            });

            const keyValuePair = {
                identifier: attributeDefinition.getIdentifier(),
                bType: (attributeDefinition.isArrayType()) ? 'array' : attributeDefinition.getBType(),
                value,
                desc: annotationDesc,
            };
            supportedKeysArray.push(keyValuePair);
        });
        return supportedKeysArray;
    }

    handleDismiss() {
        this.getDataFromPropertyForm(this.state.data);
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

    getBTypeOfConfigurationAttribute(value) {
        const annotationAttributeDef = AnnotationHelper.getAttributeDefinition(
            this.props.environment, value, this.getProtocolPkgPath(this.props.model.getProtocolPkgName()),
            'configuration');
        return annotationAttributeDef.getBType();
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
                        placeholder={key.desc}
                        value={this.state.data[key.identifier]}
                        onChange={event => this.onChange(event, key.identifier)}
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
                        placeholder={key.desc}
                        value={this.state.data[key.identifier]}
                        onChange={event => this.onChange(event, key.identifier)}
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
                    placeholder={key.desc}
                    ref={(node) => { this.node = node; }}
                />
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
        const supportedProps = this.getSupportedKeys();
        const positionX = (this.props.bBox.x) + 'px';
        const positionY = (this.props.bBox.y) + 'px';
        let visibility = 'none';
        if (this.props.visibility === true) {
            visibility = 'block';
        }
        const styles = {
            display: visibility,
            top: positionY,
            left: positionX,
            maxHeight: this.props.model.getViewState().components.body.h - 15,
            overflowY: 'scroll',
            overflowX: 'hidden',
        };
        return (
            <div
                id="servicedef-property-modal"
                style={styles}
                ref={(node) => { this.node = node; }}
                key={`servicedefProp/${this.props.model.id}`}
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
                            Server Connector Properties</h4>
                    <hr className="style1" />
                </div>
                <div className="form-body">
                    <div className="container-fluid">
                        <form className='form-horizontal propertyForm'>

                            {supportedProps.map((key) => {
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
            </div>);
    }
}

export default ServerConnectorPropertiesForm;

ServerConnectorPropertiesForm.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
};
