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
import ServiceNodeHelper from './../../../../../env/helpers/service-node-helper';
import './properties-form.css';
import PropertiesWindow from './property-window';
import NodeFactory from './../../../../../model/node-factory';

/**
 * React component for a server connector properties form
 *
 * @class ServiceDefinition
 * @extends {React.Component}
 */
class ServerConnectorPropertiesForm extends React.Component {

    constructor(props) {
        super(props);
        this.getDataFromPropertyForm = this.getDataFromPropertyForm.bind(this);
        this.getSupportedKeys = this.getSupportedKeys.bind(this);
        this.getBTypeOfConfigurationAttribute = this.getBTypeOfConfigurationAttribute.bind(this);
        this.isArrayTypeConfigurationAttribute = this.isArrayTypeConfigurationAttribute.bind(this);
        this.getProtocolPkgPath = this.getProtocolPkgPath.bind(this);
    }

    /**
     * Get the protocol package path for a service def
     * @param protocolPkgName
     * @returns {*}
     */
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

    /**
     * Get service config annotations and values added for a service using the prop form
     * @param data
     */
    getDataFromPropertyForm(data) {
        const model = this.props.model.props.model;

        // Check if there is a 'configuration' annotation attachment already
        if (model.getAnnotationAttachments().length > 0) {
            model.getAnnotationAttachments().map((annotationAttachment) => {
                if (!annotationAttachment.getAnnotationName() === 'configuration') {
                    // Create an annotation attachment 'configuration' node
                    model.addAnnotationAttachments(NodeFactory.createAnnotationAttachment(
                        { packageAlias: model.getProtocolPackageIdentifier().value,
                            annotationName: 'configuration' }), model.getAnnotationAttachments().length - 1);
                }
            });
        } else {
            // If there's no annotation attachment node create a 'configuration'
            model.addAnnotationAttachments(NodeFactory.createAnnotationAttachment(
                { packageAlias: model.getProtocolPackageIdentifier().value,
                    annotationName: 'configuration' }), 0);
        }

        // Get the configuration annotation attachment node of the service node
        const annotationAttachments = model.filterAnnotationAttachments((attachment) => {
            return attachment.getAnnotationName().value === 'configuration';
        })[0];

        // Iterate over the data entered from the property form
        Object.keys(data).forEach((key) => {
            // If there are values entered for the attribute
            if (data[key]) {
                let exists = false;
                // For values
                if (!this.isArrayTypeConfigurationAttribute(key)) {  // /* if no value array*/
                    // For attributes with values
                    // Object.keys(annotationAttachments.getAttributes()).map((annotation) => {
                    annotationAttachments.getAttributes().map((annotation) => {
                        // If the attribute is already added change the value with the new value
                        if (annotation.name === key) {
                            exists = true;
                            annotation.value.getValue().setValue(data[key]);
                        }
                    });
                    // If the value doesnot exists create a new value
                    if (!exists) {
                        // Add a new annotation attachment attribute node
                        const annotationAttachmentAttr = NodeFactory.createAnnotationAttachmentAttribute({ name: key });

                        // Add a new annotation attachment attribute value
                        const annotationAttachmentAttrValue = NodeFactory
                            .createAnnotationAttachmentAttributeValue({ value: data[key] });

                        // Add the annotation attachment attribute value to the annotation attribute
                        annotationAttachmentAttr.setValue(annotationAttachmentAttrValue);

                        // Add the annotation attachment attribute to the annotation attachment node
                        // Calculate the index to be added
                        const index = annotationAttachments.getAttributes().length - 1;
                        annotationAttachments.addAttributes(annotationAttachmentAttr, index + 1);
                    }
                    // For the attributes with value arrays
                } else {
                    Object.keys(annotationAttachments.getAttributes()).forEach((annotation) => {
                        if (annotation.name === key) {
                            exists = true;
                            // Get the values from the value array
                            const childValues = annotation.value.getValueArray();
                            // Check if there are any values to be entered to the array
                            if (data[key].length > 0) {
                                data[key].map((value) => {
                                    childValues.map((existingValue) => {
                                        // Remove if there are differences in the values entered newly
                                        // and the exisiting values
                                        if (!(data[key].some(item => existingValue.value.getValue().value === item))) {
                                            // Remove the value from the value array
                                            annotation.value.removeValueArray(existingValue);
                                        }
                                    });
                                    // Add the newly added values to the array
                                    if (!(childValues.some(item => value === item.value.getValue().value))) {
                                        const annotationAttachmentAttrValue = NodeFactory
                                            .createAnnotationAttachmentAttributeValue({ value });
                                        // Add new value to value array
                                        const index = annotation.value.getValueArray.length - 1;
                                        annotation.value.addValueArray(annotationAttachmentAttrValue, index + 1);
                                    }
                                });
                            } else {
                                // If no values are in the value array remove the annotation attachment attribute
                                annotation.parent.removeAttributes(annotation);
                            }
                        }
                    });
                    // If the value doesnot exists create a new value array
                    if (!exists) {
                        // Add a new annotation attachment attribute node
                        const annotationAttachmentAttr = NodeFactory.createAnnotationAttachmentAttribute({ name: key });
                        const attributes = data[key];

                        attributes.map((identifier) => {
                            // Add a new annotation attachment attribute value
                            const annotationAttachmentAttrValue = NodeFactory
                                .createAnnotationAttachmentAttributeValue({ value: identifier });
                            // Add the new value to the value array
                            const index = annotationAttachmentAttr.value.getValueArray.length - 1;
                            annotationAttachmentAttr.addValueArray(annotationAttachmentAttrValue, index + 1);
                        });

                        // Add the annotation attachment attribute to the annotation attachment node
                        // Calculate the index to be added
                        const index = annotationAttachments.getAttributes().length - 1;
                        annotationAttachments.addAttributes(annotationAttachmentAttr, index + 1);
                    }
                }
            }
        });
    }

    /**
     * Get config values that are already added to the service def
     * @returns {Array}
     */
    getAddedValues() {
        const model = this.props.model.props.model;
        const addedValues = [];
        const annotationAttachment = model.filterAnnotationAttachments((attachment) => {
            return attachment.getAnnotationName().value === 'configuration';
        });
        if (annotationAttachment[0]) {
            if (annotationAttachment[0].attributes) {
                const attributes = annotationAttachment[0].attributes;
                attributes.map((annotation) => {
                    let value;
                    // For array types
                    if ((annotation.value.getValueArray()).length > 0) {
                        value = [];
                        annotation.value.getValueArray().map((element) => {
                            value.push(element.getValue().value);
                        });
                    } else {
                        value = annotation.value.getValue().value;
                    }
                    const keyValuePair = { identifier: annotation.name, value };

                    addedValues.push(keyValuePair);
                });
            }
        }
        return addedValues;
    }

    /**
     * Get the config keys/attributes that is supported by the service def
     * @returns {Array}
     */
    getSupportedKeys() {
        const props = this.props.model.props;
        const supportedKeysArray = [];
        const supportedAttributes = ServiceNodeHelper.getAttributes(
            props.environment, props.model.getProtocolPackageIdentifier().value, 'configuration');
        const addedValues = this.getAddedValues();
        supportedAttributes.map((attributeDefinition) => {
            let value = '';
            addedValues.map((addedAnnotation) => {
                if (addedAnnotation.identifier === attributeDefinition.getIdentifier()) {
                    value = addedAnnotation.value;
                }
            });
            const keyValuePair = {
                identifier: attributeDefinition.getIdentifier(),
                bType: (attributeDefinition.getBType().endsWith('[]')) ? 'array' : attributeDefinition.getBType(),
                value,
            };
            supportedKeysArray.push(keyValuePair);
        });
        return supportedKeysArray;
    }

    /**
     * Get the type of the configuration attribute
     * @param value
     * @returns {*}
     */
    getBTypeOfConfigurationAttribute(value) {
        /* const annotationAttributeDef = AnnotationHelper.getAttributeDefinition(
            this.props.environment, value, this.getProtocolPkgPath(this.props.model.getProtocolPkgName()),
            'configuration');
        return annotationAttributeDef.getBType();*/
    }

    /**
     * Check if the attribute is of type array
     * @param value
     * @returns {*}
     */
    isArrayTypeConfigurationAttribute(value) {
        const props = this.props.model.props;
        const annotationAttributeDef = ServiceNodeHelper.getAttributeDefinition(
             props.environment, value, props.model.getProtocolPackageIdentifier().value, 'configuration');
        // annotationAttributeDef.isArrayType(); --> Now this always returns false
        return annotationAttributeDef.getBType().endsWith('[]');
    }
    /**
     * Renders the view for a server connector properties window
     *
     * @returns {ReactElement} The view.
     * @memberof server connector properties window
     */
    render() {
        const props = this.props.model.props;
        const positionX = (props.bBox.x) + 'px';
        const positionY = (props.bBox.y) + 'px';
        const formH = '@' + props.model.getProtocolPackageIdentifier().value + ': configuration';
        const styles = {
            popover: {
                top: props.bBox.y + 10 + 'px',
                left: positionX,
                height: '340px',
                minWidth: '500px',
            },
            arrowStyle: {
                top: positionY,
                left: props.bBox.x + 10 + 'px',
            },
        };
        const supportedKeys = this.getSupportedKeys();
        if (!supportedKeys.length) {
            return null;
        }
        return (
            <PropertiesWindow
                model={props.model}
                formHeading={formH}
                key={`servicedefProp/${props.model.id}`}
                styles={styles}
                supportedProps={supportedKeys}
                editor={props.editor}
                addedValues={this.getDataFromPropertyForm}
            />);
    }
}

export default ServerConnectorPropertiesForm;

ServerConnectorPropertiesForm.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
};
