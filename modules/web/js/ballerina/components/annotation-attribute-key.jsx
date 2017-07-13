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
import AnnotationHelper from '../env/helpers/annotation-helper';
import AutoSuggestHtml from './utils/autosuggest-html';
import ASTFactory from './../ast/ballerina-ast-factory';
import AnnotationAttributeAST from './../ast/annotations/annotation-attribute';
import EnvAnnotationDefinition from './../env/annotation-definition';
import BallerinaEnvironment from '../env/environment';

/**
 * React component for the key of an annotation attribute.
 *
 * @class AnnotationAttributeKey
 * @extends {React.Component}
 */
class AnnotationAttributeKey extends React.Component {

    /**
     * Creates an instance of AnnotationAttributeKey.
     * @param {Object} props React properties.
     * @memberof AnnotationAttributeKey
     */
    constructor(props) {
        super(props);
        if (props.attributeModel.getKey() === undefined || props.attributeModel.getKey() === '') {
            this.state = {
                isInEdit: true,
            };
        } else {
            this.state = {
                isInEdit: false,
            };
        }
        this.supportedAttributes = [];

        this.onKeyEdit = this.onKeyEdit.bind(this);
        this.onKeyBlur = this.onKeyBlur.bind(this);
        this.onKeySelected = this.onKeySelected.bind(this);
    }

    /**
     * Event when key is blurred.
     * @memberof AnnotationAttributeKey
     */
    onKeyBlur() {
        this.setState({
            isInEdit: false,
        });
    }

    /**
     * Event when key to be edited.
     * @memberof AnnotationAttributeKey
     */
    onKeyEdit() {
        this.setState({
            isInEdit: true,
        });
    }

    /**
     * The event when a key is selected. This will update the value as well.
     *
     * @param {string} suggestionValue The selected value
     * @memberof AnnotationAttributeKey
     */
    onKeySelected(suggestionValue) {
        this.setState({
            isInEdit: false,
        });

        if (this.props.attributeModel.getKey() !== suggestionValue) {
            this.props.attributeModel.setKey(suggestionValue, { doSilently: true });

            const attributeDefinition = this.props.annotationDefinitionModel;
            const annotationAttributeDef = AnnotationHelper.getAttributeDefinition(
                                                this.context.environment, suggestionValue,
                                                attributeDefinition.getPackagePath(), attributeDefinition.getName());
            const annotationAttributeValue = ASTFactory.createAnnotationAttributeValue();
            if (annotationAttributeDef.isArrayType()) {
                // Adding an init array item
                const arrayAnnotationAttributeValue = ASTFactory.createAnnotationAttributeValue();
                if (BallerinaEnvironment.getTypes().includes(annotationAttributeDef.getBType())) {
                    const bValueInArray = ASTFactory.createBValue();
                    bValueInArray.setBType(annotationAttributeDef.getBType());
                    arrayAnnotationAttributeValue.addChild(bValueInArray);
                } else {
                    const annotationAttachmentInArray = ASTFactory.createAnnotationAttachment();
                    annotationAttachmentInArray.setFullPackageName(annotationAttributeDef.getPackagePath());
                    annotationAttachmentInArray.setPackageName(
                                                            annotationAttributeDef.getPackagePath().split('.').pop());
                    annotationAttachmentInArray.setName(annotationAttributeDef.getBType());
                    arrayAnnotationAttributeValue.addChild(annotationAttachmentInArray);
                }

                annotationAttributeValue.addChild(arrayAnnotationAttributeValue);
            } else if (BallerinaEnvironment.getTypes().includes(annotationAttributeDef.getBType())) {
                const bValue = this.props.attributeModel.getFactory().createBValue();
                bValue.setBType(annotationAttributeDef.getBType());
                annotationAttributeValue.addChild(bValue);
            } else {
                const annotationAttachment = ASTFactory.createAnnotationAttachment();
                annotationAttachment.setFullPackageName(annotationAttributeDef.getPackagePath());
                annotationAttachment.setPackageName(annotationAttributeDef.getPackagePath().split('.').pop());
                annotationAttachment.setName(annotationAttributeDef.getBType());
                annotationAttributeValue.addChild(annotationAttachment);
            }
            this.props.attributeModel.setValue(annotationAttributeValue);
        }
    }

    /**
     * Gets the supported keys.
     *
     * @returns {string[]} A list keys.
     * @memberof AnnotationAttributeKey
     */
    getSupportedKeys() {
        const annotationDefinition = this.props.annotationDefinitionModel;

        if (annotationDefinition) {
            this.supportedAttributes = AnnotationHelper.getAttributes(
                    this.context.environment, annotationDefinition.getPackagePath(), annotationDefinition.getName());
            return this.supportedAttributes.map((attributeDefinition) => {
                return attributeDefinition.getIdentifier();
            });
        }

        return [];
    }

    /**
     * Renders the view for the key of an annotatino attribute.
     *
     * @returns {ReactElement} JSX of the annotation component.
     * @memberof AnnotationAttributeKey
     */
    render() {
        if (this.state.isInEdit) {
            const supportedKeys = this.getSupportedKeys();
            let initialValue = '';
            if (this.props.attributeModel !== undefined &&
                        this.props.attributeModel.getKey() !== undefined && this.props.attributeModel.getKey() !== '') {
                initialValue = this.props.attributeModel.getKey();
            }

            return (<AutoSuggestHtml
                items={supportedKeys}
                placeholder={'Key'}
                initialValue={initialValue}
                onSuggestionSelected={(event, { suggestionValue }) => { this.onKeySelected(suggestionValue); }}
                onBlur={() => this.onKeyBlur()}
                minWidth={130}
                maxWidth={1000}
                showAllAtStart
            />);
        }

        return (<span onClick={this.onKeyEdit}>
            {this.props.attributeModel.getKey()}:
            </span>);
    }
}

AnnotationAttributeKey.propTypes = {
    attributeModel: PropTypes.instanceOf(AnnotationAttributeAST),
    annotationDefinitionModel: PropTypes.instanceOf(EnvAnnotationDefinition).isRequired,
};

AnnotationAttributeKey.defaultProps = {
    attributeModel: undefined,
};

AnnotationAttributeKey.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
};

export default AnnotationAttributeKey;
