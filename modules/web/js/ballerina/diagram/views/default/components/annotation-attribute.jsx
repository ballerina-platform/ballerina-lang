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
import cn from 'classnames';
import CSSTransitionGroup from 'react-transition-group/CSSTransitionGroup';
import AnnotationAttachment from './annotation-attachment';
import AnnotationAttributeAST from './../../../../ast/annotations/annotation-attribute';
import AnnotationAttributeBValue from './annotation-attribute-b-value';
import AnnotationAttributeKey from './annotation-attribute-key';
import { addAttribute, deleteNode, getArrayValue } from './utils/annotation-button-events';
import AnnotationHelper from '../../../../env/helpers/annotation-helper';
import EnvAnnotationDefinition from './../../../../env/annotation-definition';
import PopoutButton from './popout-button';
import { util } from './../../../../visitors/sizing-utils';

/**
 * React component for an annoation attribute.
 *
 * @class AnnotationAttribute
 * @extends {React.Component}
 */
class AnnotationAttribute extends React.Component {

    /**
     * Creates an instance of AnnotationAttribute.
     * @param {Object} props React properties.
     * @memberof AnnotationAttribute
     */
    constructor(props) {
        super(props);

        let bValue = '';
        let hasError = false;
        const attributeValue = props.model.getValue();
        if (attributeValue.isBValue()) {
            const bValueAST = attributeValue.getChildren()[0];
            bValue = bValueAST.getStringValue();
            hasError = bValue === undefined || bValue.trim() === '';
        }

        if (props.model.getKey() === undefined || props.model.getKey() === '') {
            this.state = {
                hasError: true,
                isBValueEdit: this.props.model.getViewState().isInEdit || false,
                focusBValueInput: false,
                bValueText: bValue,
            };
        } else {
            this.state = {
                hasError,
                isBValueEdit: this.props.model.getViewState().isInEdit || false,
                focusBValueInput: false,
                bValueText: bValue,
            };
        }

        this.onBValueChange = this.onBValueChange.bind(this);
        this.onBValueEdit = this.onBValueEdit.bind(this);
        this.onBValueEditFinished = this.onBValueEditFinished.bind(this);
        this.onBValueKeyPress = this.onBValueKeyPress.bind(this);
        this.toggleCollapse = this.toggleCollapse.bind(this);
    }

    /**
     * @override
     */
    componentDidUpdate() {
        if (this.bValueInput && this.state.focusBValueInput) {
            this.bValueInput.focus();
        }
    }

    /**
     * Event when the value is changed.
     *
     * @param {Object} e The event.
     * @memberof AnnotationAttribute
     */
    onBValueChange(e) {
        this.setState({
            bValueText: e.target.value,
            hasError: e.target.value === '',
        });
    }

    /**
     * Event when the b-value is finished editing.
     *
     * @param {Object} event The event.
     * @memberof AnnotationAttribute
     */
    onBValueEditFinished(event) {
        this.setState({
            isBValueEdit: false,
            focusBValueInput: false,
        });
        this.props.model.getViewState().isInEdit = false;
        const attributeValue = this.props.model.getValue();
        const bValue = attributeValue.getChildren()[0];
        bValue.setStringValue(event.target.value);
    }

    /**
     * Event when the b-value is started to edit.
     *
     * @param {boolean} focusInput Whether to focus on the b-value.
     * @memberof AnnotationAttribute
     */
    onBValueEdit(focusInput) {
        this.setState({
            isBValueEdit: true,
            focusBValueInput: focusInput,
        });

        this.props.model.getViewState().isInEdit = true;
    }

    /**
     * Event when a key is pressed on a b value edit.
     *
     * @param {Object} event The event.
     * @memberof AnnotationAttribute
     */
    onBValueKeyPress(event) {
        if (event.keyCode === 13 || event.which === 13) {
            this.setState({
                isBValueEdit: false,
                focusBValueInput: false,
            });
            this.props.model.getViewState().isInEdit = false;
            const attributeValue = this.props.model.getValue();
            const bValue = attributeValue.getChildren()[0];
            bValue.setStringValue(event.target.value);
        }
    }

    /**
     * Event when the collapse icon is clicked.
     * @memberof AnnotationAttribute
     */
    toggleCollapse() {
        this.props.model.getViewState().collapsed = !this.props.model.getViewState().collapsed;
        this.context.editor.update();
    }

    /**
     * Renders the annotation attributes of an annotation attachment.
     *
     * @param {AnnotationAttachment} annotationAttachment The annotation attachment.
     * @returns {AnnotationAttribute[]}  A component list.
     * @memberof AnnotationAttribute
     */
    renderAnnotationAttributes(annotationAttachment) {
        if (this.props.annotationDefinitionModel) {
            return annotationAttachment.getChildren().map((attribute) => {
                return (<AnnotationAttribute
                    key={attribute.getID()}
                    model={attribute}
                    annotationDefinitionModel={
                        AnnotationHelper.getAnnotationDefinition(
                                                    this.context.environment, annotationAttachment.getFullPackageName(),
                                                    annotationAttachment.getName())}
                />);
            });
        }

        return annotationAttachment.getChildren().map((attribute) => {
            return (<AnnotationAttribute
                key={attribute.getID()}
                model={attribute}
            />);
        });
    }

    /**
     * Rendering the values inside an annotation-attribute-value. This is used for rendering elements inside an arrayed
     * annotation-attribute-value.
     *
     * @param {AnnotationAttributeValue} attributeValue The annotation-attribute-value.
     * @returns {AnnotationAttributeBValue|AnnotationAttachment|null} The component of the value.
     * @memberof AnnotationAttribute
     */
    renderAnnotationAttributeValue(attributeValue) {
        if (attributeValue.isBValue()) {
            const buttons = [];
            // Delete button.
            const deleteButton = {
                icon: 'fw-cancel',
                text: 'Delete',
                onClick: () => {
                    deleteNode(attributeValue);
                },
            };
            buttons.push(deleteButton);
            return (<li key={attributeValue.getID()}>
                <ul>
                    <li>
                        <AnnotationAttributeBValue model={attributeValue.getChildren()[0]} />
                        <PopoutButton buttons={buttons} />
                    </li>
                </ul>
            </li>);
        } else if (attributeValue.isAnnotation()) {
            return (<li key={attributeValue.getID()}>
                <AnnotationAttachment model={attributeValue.getChildren()[0]} isNestedAnnotation />
            </li>);
        }
        // TODO: Implement for arrays ?
        return (null);
    }

    /**
     * Renders the key of an annotation attribute.
     *
     * @returns {AnnotationAttributeKey} Component for the key.
     * @memberof AnnotationAttribute
     */
    renderKey() {
        if (this.props.annotationDefinitionModel) {
            return (<AnnotationAttributeKey
                attributeModel={this.props.model}
                annotationDefinitionModel={this.props.annotationDefinitionModel}
            />);
        } else {
            return (<AnnotationAttributeKey
                attributeModel={this.props.model}
            />);
        }
    }

    /**
     * Renders the view for an annotation-attribute-value
     *
     * @returns {ReactElement} JSX of the annotation component.
     * @memberof Annotation
     */
    render() {
        const key = this.renderKey();
        const attributeValue = this.props.model.getValue();
        let errorClass;
        if (attributeValue.isBValue()) {
            const buttons = [];
            // Delete button.
            const deleteButton = {
                icon: 'fw-cancel',
                text: 'Delete',
                onClick: () => {
                    deleteNode(this.props.model);
                },
            };
            buttons.push(deleteButton);

            const bValue = attributeValue.getChildren()[0];
            if (this.state.isBValueEdit) {
                const width = util.getTextWidth(this.state.bValueText, 150, 1000);
                return (
                    <ul
                        className="attribute-value-bvalue"
                    >
                        <li className={cn({ 'annotation-attribute-error': this.state.hasError })}>
                            {key}
                            <span className="annotation-attribute-value-wrapper">
                                <input
                                    className='annotation-attribute-b-value-input'
                                    ref={(ref) => { this.bValueInput = ref; }}
                                    type="text"
                                    value={this.state.bValueText}
                                    onChange={this.onBValueChange}
                                    onBlur={this.onBValueEditFinished}
                                    onKeyPress={this.onBValueKeyPress}
                                    style={{ width: parseInt(width.w + 20, 10) }}
                                />
                            </span>
                            <PopoutButton buttons={buttons} />
                        </li>
                    </ul>
                );
            }

            if (bValue.getStringValue() === undefined || bValue.getStringValue() === '') {
                return (
                    <ul
                        className="attribute-value-bvalue"
                    >
                        <li
                            className={cn({ 'annotation-attribute-error': this.state.hasError })}
                            onClick={() => this.onBValueEdit(false)}
                        >
                            {key}
                            <span className="annotation-attribute-value-wrapper">
                                {bValue.getStringValue()}
                            </span>
                            <PopoutButton buttons={buttons} />
                        </li>
                    </ul>
                );
            }
            return (
                <ul
                    className="attribute-value-bvalue"
                >
                    <li className={cn({ 'annotation-attribute-error': this.state.hasError })}>
                        {key}
                        <span className="annotation-attribute-value-wrapper" onClick={() => this.onBValueEdit(true)}>
                            {bValue.getStringValue()}
                        </span>
                        <PopoutButton buttons={buttons} />
                    </li>
                </ul>
            );
        } else if (attributeValue.isAnnotation()) {
            const annotationAttachment = attributeValue.getChildren()[0];
            const packageName = (<span>{annotationAttachment.getPackageName()}</span>);
            const name = (<span>{annotationAttachment.getName()}</span>);
            const addPopButton = [];
            const deletePopButton = [];
            const annotationDefinition = AnnotationHelper.getAnnotationDefinition(
                                                this.context.environment, annotationAttachment.getFullPackageName(),
                                                annotationAttachment.getName());
            if (annotationDefinition && annotationDefinition.getAnnotationAttributeDefinitions().length > 0) {
                // Add attribute button
                const addAttributeButton = {
                    icon: 'fw-add',
                    text: 'Add Attribute',
                    onClick: () => {
                        addAttribute(annotationAttachment);
                    },
                };
                addPopButton.push(addAttributeButton);
            }
            // Delete button.
            const deleteButton = {
                icon: 'fw-cancel',
                text: 'Delete',
                onClick: () => {
                    deleteNode(this.props.model);
                },
            };
            deletePopButton.push(deleteButton);

            const attributes = this.props.model.getViewState().collapsed ? [] :
                this.renderAnnotationAttributes(annotationAttachment);

            return (
                <ul
                    className="attribute-value-annotation"
                >
                    <li className={cn({ 'annotation-attribute-error': this.state.hasError })}>
                        <CSSTransitionGroup
                            component="span"
                            transitionName="annotation-expand"
                            transitionEnterTimeout={300}
                            transitionLeaveTimeout={300}
                        >
                            <i
                                className={cn('fw fw-right expand-icon',
                                    { 'fw-rotate-90': !this.props.model.getViewState().collapsed },
                                    { hide: annotationAttachment.getChildren().length === 0 })}
                                onClick={this.toggleCollapse}
                            />
                        </CSSTransitionGroup>
                        {key}
                        <span className="annotation-attachment-package-name annotation-attribute-value-wrapper">
                            @{packageName}
                        </span>
                        :{name}
                        <PopoutButton buttons={addPopButton} />
                        <PopoutButton buttons={deletePopButton} />
                        <span className='annotation-attachment-badge hide'>
                            <i className="fw fw-annotation-badge" />
                        </span>
                    </li>
                    {attributes}
                </ul>
            );
        } else if (attributeValue.isArray()) {
            const addPopButton = [];
            const deletePopButton = [];
            // Delete button.
            const deleteButton = {
                icon: 'fw-cancel',
                text: 'Delete',
                onClick: () => {
                    deleteNode(this.props.model);
                },
            };
            const addNewToArray = {
                icon: 'fw-add',
                text: 'Add Value',
                onClick: () => {
                    attributeValue.addChild(getArrayValue(
                        this.context.environment, this.props.model.getKey(), this.props.annotationDefinitionModel));
                },
            };
            addPopButton.push(addNewToArray);
            deletePopButton.push(deleteButton);

            const arrayValues = this.props.model.getViewState().collapsed ? [] :
                attributeValue.getChildren().map((annotationAttributeValue) => {
                    return this.renderAnnotationAttributeValue(annotationAttributeValue);
                });

            return (
                <ul
                    className="attribute-value-array"
                >
                    <li className={cn({ 'annotation-attribute-error': this.state.hasError })}>
                        <CSSTransitionGroup
                            component="span"
                            transitionName="annotation-expand"
                            transitionEnterTimeout={300}
                            transitionLeaveTimeout={300}
                        >
                            <i
                                className={cn('fw fw-right expand-icon',
                                    { 'fw-rotate-90': !this.props.model.getViewState().collapsed },
                                    { hide: attributeValue.getChildren().length === 0 })}
                                onClick={this.toggleCollapse}
                            />
                        </CSSTransitionGroup>
                        {key}
                        <PopoutButton buttons={addPopButton} />
                        <PopoutButton buttons={deletePopButton} />
                        <span className='annotation-attribute-array-badge hide'>
                            <i className="fw fw-annotation-attribute-array-badge" />
                        </span>
                    </li>
                    {arrayValues}
                </ul>
            );
        }

        return (null);
    }
}

AnnotationAttribute.propTypes = {
    model: PropTypes.instanceOf(AnnotationAttributeAST).isRequired,
    annotationDefinitionModel: PropTypes.instanceOf(EnvAnnotationDefinition),
};

AnnotationAttribute.defaultProps = {
    annotationDefinitionModel: undefined,
};

AnnotationAttribute.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default AnnotationAttribute;
