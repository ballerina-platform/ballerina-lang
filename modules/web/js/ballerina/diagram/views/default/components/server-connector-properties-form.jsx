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
import ASTFactory from '../../../../ast/ast-factory';
import AnnotationHelper from '../../../../env/helpers/annotation-helper';
import './properties-form.css';
import PropertiesWindow from './property-window';

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
            if (data[key]) {
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

    /**
     * Get config values that are already added to the service def
     * @returns {Array}
     */
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

    /**
     * Get the config keys/attributes that is supported by the service def
     * @returns {Array}
     */
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

    /**
     * Get the type of the configuration attribute
     * @param value
     * @returns {*}
     */
    getBTypeOfConfigurationAttribute(value) {
        const annotationAttributeDef = AnnotationHelper.getAttributeDefinition(
            this.props.environment, value, this.getProtocolPkgPath(this.props.model.getProtocolPkgName()),
            'configuration');
        return annotationAttributeDef.getBType();
    }

    /**
     * Check if the attribute is of type array
     * @param value
     * @returns {*}
     */
    isArrayTypeConfigurationAttribute(value) {
        const annotationAttributeDef = AnnotationHelper.getAttributeDefinition(
            this.props.environment, value, this.getProtocolPkgPath(this.props.model.getProtocolPkgName()),
            'configuration');
        return annotationAttributeDef.isArrayType();
    }
    /**
     * Renders the view for a server connector properties window
     *
     * @returns {ReactElement} The view.
     * @memberof server connector properties window
     */
    render() {
        const positionX = (this.props.bBox.x) + 'px';
        const positionY = (this.props.bBox.y) + 'px';
        const formH = '@' + this.props.model.getProtocolPkgName() + ': configuration';
        const styles = {
            popover: {
                top: this.props.bBox.y + 10 + 'px',
                left: positionX,
                height: '320px',
            },
            arrowStyle: {
                top: positionY,
                left: this.props.bBox.x + 10 + 'px',
            },
        };
        const supportedKeys = this.getSupportedKeys();
        if (!supportedKeys.length) {
            return null;
        }
        return (
            <PropertiesWindow
                model={this.props.model}
                formHeading={formH}
                key={`servicedefProp/${this.props.model.id}`}
                styles={styles}
                supportedProps={supportedKeys}
                editor={this.props.editor}
                addedValues={this.getDataFromPropertyForm}
            />);
    }
}

export default ServerConnectorPropertiesForm;

ServerConnectorPropertiesForm.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
};
