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
import NodeFactory from './../../../../../model/node-factory';
import PropertyWindow from './property-window';

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
        this.createIdentifierNode = this.createIdentifierNode.bind(this);
        this.addQuotationForStringValues = this.addQuotationForStringValues.bind(this);
    }

    /**
     * Get service config annotations and values added for a service using the prop form
     * @param data
     */
    getDataFromPropertyForm(data) {
        const model = this.props.model.props.model;

        // Check if there is a 'configuration' annotation attachment already
        if (model.getAnnotationAttachments().length > 0) {
            model.getAnnotationAttachments().forEach((annotationAttachment) => {
                if (!annotationAttachment.getAnnotationName() === 'configuration') {
                    model.addAnnotationAttachments(NodeFactory.createAnnotationAttachment(
                        { packageAlias: this.createIdentifierNode(model.getProtocolPackageIdentifier().value),
                            annotationName: this.createIdentifierNode('configuration') }),
                        model.getAnnotationAttachments().length - 1, true);
                }
            });
        } else {
            model.addAnnotationAttachments(NodeFactory.createAnnotationAttachment(
                { packageAlias: this.createIdentifierNode(model.getProtocolPackageIdentifier().value),
                    annotationName: this.createIdentifierNode('configuration') }),
                model.getAnnotationAttachments().length - 1, true);
        }

        // Get the configuration annotation attachment node of the service node
        const annotationAttachments = model.filterAnnotationAttachments((attachment) => {
            return attachment.getAnnotationName().value === 'configuration';
        })[0];

        // Iterate over the data entered from the property form
        data.forEach((key) => {
            // If there are values entered for the attribute
            if (key.value) {
                let exists = false;
                // For values
                if (!this.isArrayTypeConfigurationAttribute(key.identifier)) {
                    const bType = this.getBTypeOfConfigurationAttribute(key.identifier);
                    let value = key.value;
                    // For attributes with values
                    annotationAttachments.getAttributes().forEach((annotation) => {
                        // If the attribute is already added change the value with the new value
                        if (annotation.getName().value === key.identifier) {
                            exists = true;
                            if (bType === 'string') {
                                value = this.addQuotationForStringValues(value);
                            }
                            annotation.value.getValue().setValue(value);
                        }
                    });
                    // If the value doesnot exists create a new value
                    if (!exists) {
                        // Add a new annotation attachment attribute node
                        const annotationAttachmentAttr = NodeFactory.createAnnotationAttachmentAttribute(
                            { name: this.createIdentifierNode(key.identifier) });

                        // Create a literal node to add the value
                        if (bType === 'string') {
                            value = this.addQuotationForStringValues(value);
                        }
                        const valueNode = NodeFactory.createLiteral({ value });
                        // Add a new annotation attachment attribute value
                        const annotationAttachmentAttrValue = annotationAttachmentAttr.getValue();
                        annotationAttachmentAttrValue.setValue(valueNode);
                        // Delete the value array attribute
                        delete annotationAttachmentAttrValue.getValueArray();

                        // Add the annotation attachment attribute value to the annotation attribute
                        annotationAttachmentAttr.setValue(annotationAttachmentAttrValue);

                        // Add the annotation attachment attribute to the annotation attachment node
                        // Calculate the index to be added
                        const index = annotationAttachments.getAttributes().length - 1;
                        annotationAttachments.addAttributes(annotationAttachmentAttr, index + 1, true);
                    }
                    // For the attributes with value arrays
                } else {
                    annotationAttachments.getAttributes().forEach((annotation) => {
                        if (annotation.getName().value === key.identifier) {
                            exists = true;
                            // Get the values from the value array
                            annotation.value.setValueArray([], true);
                            // Check if there are any values to be entered to the array
                            if (key.value.length > 0) {
                                annotation = this.addValuesToValueArray(annotation, key.value);
                            } else {
                                // Remove if there are values
                                annotationAttachments.removeAttributes(annotation, true);
                            }
                        }
                    });
                    // If the value doesnot exists create a new value array
                    if (!exists) {
                        // Add a new annotation attachment attribute node
                        let annotationAttachmentAttr = NodeFactory.createAnnotationAttachmentAttribute(
                            { name: this.createIdentifierNode(key.identifier) });
                        const attributes = key.value;
                        annotationAttachmentAttr = this.addValuesToValueArray(annotationAttachmentAttr, attributes);
                        delete annotationAttachmentAttr.getValue().value;

                        // Add the annotation attachment attribute to the annotation attachment node
                        // Calculate the index to be added
                        const index = annotationAttachments.getAttributes().length - 1;
                        annotationAttachments.addAttributes(annotationAttachmentAttr, index + 1, true);
                    }
                }
            } else {
                annotationAttachments.getAttributes().forEach((annotation) => {
                    if (annotation.getName().value === key.identifier) {
                        annotationAttachments.removeAttributes(annotation, true);
                    }
                });
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
                attributes.forEach((annotation) => {
                    let value;                    // For array types
                    if ((annotation.value.getValueArray()).length > 0) {
                        value = [];
                        annotation.value.getValueArray().forEach((element) => {
                            value.push(element.getValue().value);
                        });
                    } else {
                        value = annotation.value.getValue().value;
                    }
                    const keyValuePair = { identifier: annotation.getName().value, value };

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
            this.context.environment, props.model.getProtocolPackageIdentifier().value, 'configuration');
        const addedValues = this.getAddedValues();
        supportedAttributes.forEach((attributeDefinition) => {
            let value = '';
            addedValues.forEach((addedAnnotation) => {
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
        const props = this.props.model.props;
        const annotationAttributeDef = ServiceNodeHelper.getAttributeDefinition(
            this.context.environment, value, props.model.getProtocolPackageIdentifier().value, 'configuration');
        return annotationAttributeDef.getBType();
    }

  /**
   * Create an Annotation Name Node
   */

    createIdentifierNode(value) {
        return NodeFactory.createIdentifier({ value });
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
   * Fill the value array in AnnotationAttachmentAttributeValue
   */
    addValuesToValueArray(annotationAttachmentAttr, attributes) {
        attributes.forEach((identifier) => {
      // Add a new annotation attachment attribute value
      // Create a literal node to add the value
            identifier = this.addQuotationForStringValues(identifier);
            const valueNode = NodeFactory.createLiteral({ value: identifier });

            const annotationAttachmentAttrValue = NodeFactory
        .createAnnotationAttachmentAttributeValue({ value: valueNode });
      // Add the new value to the value array
            let index = annotationAttachmentAttr.value.getValueArray().length - 1;
            if (index === -1) {
                index = 0;
            }
            annotationAttachmentAttr.getValue().addValueArray(annotationAttachmentAttrValue, index + 1, true);
        });
        return annotationAttachmentAttr;
    }
    /**
     * Check if the attribute is of type array
     * @param value
     * @returns {*}
     */
    isArrayTypeConfigurationAttribute(value) {
        const props = this.props.model.props;
        const annotationAttributeDef = ServiceNodeHelper.getAttributeDefinition(
            this.context.environment, value, props.model.getProtocolPackageIdentifier().value, 'configuration');
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
                minWidth: '500px',
            },
            arrowStyle: {
                top: positionY,
                left: props.bBox.x + 10 + 'px',
            },
        };
        const supportedKeys = this.getSupportedKeys();
        let propertiesExist = true;
        if (!supportedKeys.length) {
            propertiesExist = false;
        }
        return (
            <PropertyWindow
                model={props.model}
                formHeading={formH}
                key={`servicedefProp/${props.model.id}`}
                styles={styles}
                supportedProps={supportedKeys}
                addedValues={this.getDataFromPropertyForm}
                propertiesExist={propertiesExist}
            />);
    }
}

export default ServerConnectorPropertiesForm;

ServerConnectorPropertiesForm.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
};
