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
import AnnotationHelper from 'plugins/ballerina/env/helpers/annotation-helper';
import NodeFactory from 'plugins/ballerina/model/node-factory';
import AnnotationAttachmentAttributeTreeNode from 'plugins/ballerina/model/tree/annotation-attachment-attribute-node';
import AnnotationAttachmentTreeNode from 'plugins/ballerina/model/tree/annotation-attachment-node';
import EnvAnnotationDefinition from 'plugins/ballerina/env/annotation-definition';
import BallerinaEnvironment from 'plugins/ballerina/env/environment';
import AutoSuggestHtml from '../decorators/autosuggest-html';

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
        let editState = this.props.editState;
        if (props.attributeModel.getName().getValue() === undefined ||
                                                                    props.attributeModel.getName().getValue() === '') {
            editState = 1;
        }
        this.state = {
            editState,
        };

        this.onKeyEdit = this.onKeyEdit.bind(this);
        this.onKeyBlur = this.onKeyBlur.bind(this);
        this.onKeySelected = this.onKeySelected.bind(this);
    }

    /**
     * Event when key is blurred.
     * @memberof AnnotationAttributeKey
     */
    onKeyBlur(event) {
        if (event.target.value.trim() !== '') {
            this.setState({
                editState: 0,
            });
        } else if (this.props.removeEmptyAttribute) {
            this.props.removeEmptyAttribute();
        }
    }

    /**
     * Event when key to be edited.
     * @memberof AnnotationAttributeKey
     */
    onKeyEdit() {
        this.setState({
            editState: 1,
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
            editState: 0,
        });

        if (this.props.attributeModel.getName().getValue() !== suggestionValue) {
            const keyIdentifier = NodeFactory.createIdentifier({
                value: suggestionValue,
            });
            this.props.attributeModel.setName(keyIdentifier, true);

            const attributeDefinition = this.props.annotationDefinitionModel;
            const annotationAttributeDef = AnnotationHelper.getAttributeDefinition(
                                                this.context.environment, suggestionValue,
                                                attributeDefinition.getPackagePath(), attributeDefinition.getName());
            annotationAttributeDef.setPackagePath(attributeDefinition.getPackagePath());
            let annotationAttributeValue = NodeFactory.createAnnotationAttachmentAttributeValue();
            if (annotationAttributeDef.isArrayType()) {
                // Adding an init array item
                annotationAttributeValue = NodeFactory.createAnnotationAttachmentAttributeValue({
                    value: undefined,
                });
                if (BallerinaEnvironment.getTypes().includes(annotationAttributeDef.getBType().replace('[]', ''))) {
                    const literalInArray = NodeFactory.createLiteral({
                        value: AnnotationHelper.generateDefaultValue(annotationAttributeDef
                                                                                    .getBType().replace('[]', '')),
                    });
                    const arrayAnnotationAttributeValue = NodeFactory.createAnnotationAttachmentAttributeValue();
                    arrayAnnotationAttributeValue.setValue(literalInArray);

                    if (this.props.attributeModel.parent === undefined && this.props.parent) {
                        this.props.parent.viewState.addingEmptyAttribute = false;
                        this.props.parent.addAttributes(this.props.attributeModel, -1, true);
                    }
                    annotationAttributeValue.addValueArray(arrayAnnotationAttributeValue);
                } else {
                    const annotationAttachmentInArray = NodeFactory.createAnnotationAttachment({
                        packageAlias: NodeFactory.createLiteral({
                            value: annotationAttributeDef.getPackagePath().split('.').pop(),
                        }),
                        annotationName: NodeFactory.createLiteral({
                            value: annotationAttributeDef.getBType().split(':').pop().replace('[]', ''),
                        }),
                    });
                    const arrayAnnotationAttributeValue = NodeFactory.createAnnotationAttachmentAttributeValue();
                    arrayAnnotationAttributeValue.setValue(annotationAttachmentInArray);

                    if (this.props.attributeModel.parent === undefined && this.props.parent) {
                        this.props.parent.viewState.addingEmptyAttribute = false;
                        this.props.parent.addAttributes(this.props.attributeModel, -1, true);
                    }
                    annotationAttributeValue.addValueArray(arrayAnnotationAttributeValue);
                }
            } else if (BallerinaEnvironment.getTypes().includes(annotationAttributeDef.getBType())) {
                const bValue = NodeFactory.createLiteral({
                    value: AnnotationHelper.generateDefaultValue(annotationAttributeDef
                        .getBType().replace('[]', '')),
                });
                // bValue.setBType(annotationAttributeDef.getBType());
                annotationAttributeValue.setValue(bValue);

                if (this.props.attributeModel.parent === undefined && this.props.parent) {
                    this.props.parent.viewState.addingEmptyAttribute = false;
                    this.props.parent.addAttributes(this.props.attributeModel, -1, true);
                }
            } else {
                const annotationAttachment = NodeFactory.createAnnotationAttachment();
                annotationAttachment.setPackageAlias(NodeFactory.createLiteral({
                    value: annotationAttributeDef.getPackagePath().split('.').pop(),
                }));
                annotationAttachment.setAnnotationName(NodeFactory.createLiteral({
                    value: annotationAttributeDef.getBType().split(':').pop(),
                }));

                if (this.props.attributeModel.parent === undefined && this.props.parent) {
                    this.props.parent.viewState.addingEmptyAttribute = false;
                    this.props.parent.addAttributes(this.props.attributeModel, -1, true);
                }

                annotationAttributeValue.setValue(annotationAttachment);
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
            const supportedAttributes = AnnotationHelper.getAttributes(
                    this.context.environment, annotationDefinition.getPackagePath(), annotationDefinition.getName());
            return supportedAttributes.map((attributeDefinition) => {
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
        if (this.state.editState < 0) {
            // Empty state
            const supportedKeys = this.getSupportedKeys();
            return (<AutoSuggestHtml
                items={supportedKeys}
                placeholder={'Key'}
                initialValue=''
                onSuggestionSelected={(event, { suggestionValue }) => { this.onKeySelected(suggestionValue); }}
                onBlur={e => this.onKeyBlur(e)}
                minWidth={130}
                maxWidth={1000}
                showAllAtStart
            />);
        } else if (this.state.editState === 0) {
            // not editing state
            return (<span onClick={this.onKeyEdit} className='annotation-attribute-key'>
                {this.props.attributeModel.getName().getValue()}:
            </span>);
        } else {
            // editing state
            const supportedKeys = this.getSupportedKeys();
            let initialValue = '';
            if (this.props.attributeModel !== undefined &&
                this.props.attributeModel.getName().getValue() !== undefined &&
                                                        this.props.attributeModel.getName().getValue().trim() !== '') {
                initialValue = this.props.attributeModel.getName().getValue();
            }

            return (<AutoSuggestHtml
                items={supportedKeys}
                placeholder={'Key'}
                initialValue={initialValue}
                onSuggestionSelected={(event, { suggestionValue }) => { this.onKeySelected(suggestionValue); }}
                onBlur={e => this.onKeyBlur(e)}
                minWidth={130}
                maxWidth={1000}
                showAllAtStart
            />);
        }
    }
}

AnnotationAttributeKey.propTypes = {
    attributeModel: PropTypes.instanceOf(AnnotationAttachmentAttributeTreeNode).isRequired,
    annotationDefinitionModel: PropTypes.instanceOf(EnvAnnotationDefinition),
    editState: PropTypes.number,
    parent: PropTypes.instanceOf(AnnotationAttachmentTreeNode),
    removeEmptyAttribute: PropTypes.func,
};

AnnotationAttributeKey.defaultProps = {
    annotationDefinitionModel: undefined,
    editState: 0,
    parent: undefined,
    removeEmptyAttribute: undefined,
};

AnnotationAttributeKey.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
};

export default AnnotationAttributeKey;
