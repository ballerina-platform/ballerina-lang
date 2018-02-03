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
import AnnotationHelper from 'plugins/ballerina/env/helpers/annotation-helper';
import EnvAnnotationDefinition from 'plugins/ballerina/env/annotation-definition';
import AnnotationAttributeTreeNode from 'plugins/ballerina/model/tree/annotation-attachment-attribute-node';
import AnnotationAttachmentTreeNode from 'plugins/ballerina/model/tree/annotation-attachment-node';
import { addAttribute, deleteNode, getArrayValue } from '../decorators/annotation-button-events';
import AnnotationAttachmentNode from './annotation-attachment-node';
import AnnotationAttributeLiteralNode from './annotation-attachment-attribute-literal-node';
import AnnotationAttributeVariableRefNode from './annotation-attachment-attribute-variable-ref-node';
import AnnotationAttributeKey from '../decorators/annotation-attribute-key';
import SizingUtils from '../../sizing-util';

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
        if (attributeValue.isValueLiteral()) {
            const bValueAST = attributeValue.getValue();
            bValue = bValueAST.getValue();
            hasError = bValue === undefined;
        }

        if (props.model.getName().getValue() === undefined || props.model.getName().getValue() === '') {
            this.state = {
                hasError: true,
                isBValueEdit: this.props.model.viewState.isInEdit || false,
                focusBValueInput: false,
                bValueText: bValue,
                addingEmptyAttribute: false,
            };
        } else {
            this.state = {
                hasError,
                isBValueEdit: this.props.model.viewState.isInEdit || false,
                focusBValueInput: false,
                bValueText: bValue,
                addingEmptyAttribute: false,
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
        this.props.model.viewState.isInEdit = false;
        const attributeValue = this.props.model.getValue();
        const literal = attributeValue.getValue();
        literal.setValue(event.target.value);

        if (this.props.model.parent === undefined && this.props.parent) {
            this.props.model.addingEmptyAttribute = false;
            this.props.parent.addAttributes(this.props.model);
        }
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

        this.props.model.viewState.isInEdit = true;
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
            this.props.model.viewState.isInEdit = false;
            const attributeValue = this.props.model.getValue();
            const bValue = attributeValue.getValue();
            bValue.setValue(event.target.value);
        }
    }

    /**
     * Event when the collapse icon is clicked.
     * @memberof AnnotationAttribute
     */
    toggleCollapse() {
        this.props.model.viewState.collapsed = !this.props.model.viewState.collapsed;
        this.context.editor.update();
    }

    /**
     * Renders the annotation attributes of an annotation attachment.
     *
     * @param {AnnotationAttachmentNode} annotationAttachment The annotation attachment.
     * @returns {AnnotationAttribute[]}  A component list.
     * @memberof AnnotationAttribute
     */
    renderAnnotationAttributes(annotationAttachment) {
        if (this.props.annotationDefinitionModel) {
            return annotationAttachment.getAttributes().map((attribute) => {
                const fullPackagePath = AnnotationHelper.resolveFullPackageName(
                        this.context.astRoot, annotationAttachment.getPackageAlias().getValue());
                const annotationDefOfAttribute = AnnotationHelper.getAnnotationDefinition(
                    this.context.environment,
                    fullPackagePath,
                    annotationAttachment.getAnnotationName().getValue());
                annotationDefOfAttribute.setPackagePath(fullPackagePath);
                return (<AnnotationAttribute
                    key={attribute.getID()}
                    model={attribute}
                    annotationDefinitionModel={annotationDefOfAttribute}
                />);
            });
        }

        return annotationAttachment.getAttributes().map((attribute) => {
            return (<AnnotationAttribute
                key={attribute.getID()}
                model={attribute}
            />);
        });
    }

    /**
     * Renders an empty attribute.
     * @returns {ReactElement} The attribute view.
     * @memberof AnnotationAttachmentNode
     */
    renderEmptyAttribute(annotationAttachment) {
        if (this.props.annotationDefinitionModel) {
            const fullPackagePath = AnnotationHelper.resolveFullPackageName(
                this.context.astRoot, annotationAttachment.getPackageAlias().getValue());
            const annotationDefOfAttribute = AnnotationHelper.getAnnotationDefinition(
                this.context.environment,
                fullPackagePath,
                annotationAttachment.getAnnotationName().getValue());
            annotationDefOfAttribute.setPackagePath(fullPackagePath);
            return (<AnnotationAttribute
                key='new-annotation-attachment-inner-attribute'
                model={addAttribute()}
                annotationDefinitionModel={annotationDefOfAttribute}
                parent={annotationAttachment}
            />);
        }

        return (null);
    }

    /**
     * Rendering the values inside an annotation-attribute-value. This is used for rendering elements inside an arrayed
     * annotation-attribute-value.
     *
     * @param {AnnotationAttributeValue} attributeValue The annotation-attribute-value.
     * @returns {AnnotationAttributeLiteralNode|AnnotationAttachmentNode|null} The component of the value.
     * @memberof AnnotationAttribute
     */
    renderAnnotationAttributeValue(attributeValue) {
        if (attributeValue.isValueLiteral()) {
            const actionMenuItems = [];
            // Delete button.
            const deleteButton = {
                key: attributeValue.getID(),
                icon: 'fw-cancel',
                text: 'Delete',
                onClick: () => {
                    deleteNode(attributeValue);
                },
            };
            actionMenuItems.push(deleteButton);

            const actionItems = actionMenuItems.map((item) => {
                return (<i
                    key={`${item.key}-action-menu-item-${item.text.toLowerCase().replace(/\s/g, '')}`}
                    className={'fw ' + item.icon}
                    onClick={item.onClick}
                />);
            });
            return (<li key={attributeValue.getID()}>
                <ul>
                    <li className='action-menu-wrapper'>
                        <span className='annotations-array-item-prefix'>
                            <i className='fw fw-minus' />
                        </span>
                        <AnnotationAttributeLiteralNode model={attributeValue.getValue()} />
                        <span className='annotation-action-menu-items'>
                            {actionItems}
                        </span>
                    </li>
                </ul>
            </li>);
        } else if (attributeValue.isValueAnnotationAttachment()) {
            return (<li key={attributeValue.getID()}>
                <AnnotationAttachmentNode model={attributeValue.getValue()} isNestedAnnotation />
            </li>);
        } else if (attributeValue.isValueVariableRef()) {
            const actionMenuItems = [];
            // Delete button.
            const deleteButton = {
                key: attributeValue.getID(),
                icon: 'fw-cancel',
                text: 'Delete',
                onClick: () => {
                    deleteNode(attributeValue);
                },
            };
            actionMenuItems.push(deleteButton);

            const actionItems = actionMenuItems.map((item) => {
                return (<i
                    key={`${item.key}-action-menu-item-${item.text.toLowerCase().replace(/\s/g, '')}`}
                    className={'fw ' + item.icon}
                    onClick={item.onClick}
                />);
            });
            return (<li key={attributeValue.getID()}>
                <ul>
                    <li className='action-menu-wrapper'>
                        <span className='annotations-array-item-prefix'>
                            <i className='fw fw-minus' />
                        </span>
                        <AnnotationAttributeVariableRefNode model={attributeValue.getValue()} />
                        <span className='annotation-action-menu-items'>
                            {actionItems}
                        </span>
                    </li>
                </ul>
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
                parent={this.props.parent}
                removeEmptyAttribute={this.props.removeEmptyAttribute}
            />);
        } else {
            return (<AnnotationAttributeKey
                attributeModel={this.props.model}
                parent={this.props.parent}
                removeEmptyAttribute={this.props.removeEmptyAttribute}
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
        if (attributeValue.isValueLiteral()) {
            const actionMenuItems = [];
            // Delete button.
            const deleteButton = {
                key: this.props.model.getID(),
                icon: 'fw-cancel',
                text: 'Delete',
                onClick: () => {
                    deleteNode(this.props.model);
                },
            };
            actionMenuItems.push(deleteButton);

            const actionItems = actionMenuItems.map((item) => {
                return (<i
                    key={`${item.key}-action-menu-item-${item.text.toLowerCase().replace(/\s/g, '')}`}
                    className={'fw ' + item.icon}
                    onClick={item.onClick}
                />);
            });

            const literalNode = attributeValue.getValue();
            if (this.state.isBValueEdit) {
                const width = new SizingUtils().getTextWidth(this.state.bValueText, 150, 1000);
                return (
                    <ul className='attribute-value-bvalue'>
                        <li className={cn('action-menu-wrapper',
                            { 'annotation-attribute-error': this.state.hasError })}
                        >
                            {key}
                            <span className='annotation-attribute-value-wrapper'>
                                <input
                                    key={literalNode.id}
                                    className='annotation-attribute-b-value-input'
                                    ref={(ref) => { this.bValueInput = ref; }}
                                    type='text'
                                    value={this.state.bValueText}
                                    onChange={this.onBValueChange}
                                    onBlur={this.onBValueEditFinished}
                                    onKeyPress={this.onBValueKeyPress}
                                    style={{ width: parseInt(width.w + 20, 10) }}
                                />
                            </span>
                            <span className='annotation-action-menu-items'>
                                {actionItems}
                            </span>
                        </li>
                    </ul>
                );
            }
            if (literalNode.getValue() === undefined || literalNode.getValue() === '') {
                return (
                    <ul className='attribute-value-bvalue'>
                        <li
                            className={cn('action-menu-wrapper', { 'annotation-attribute-error': this.state.hasError })}
                            onClick={() => this.onBValueEdit(false)}
                        >
                            {key}
                            <span className='annotation-attribute-value-wrapper'>
                                {literalNode.getValue()}
                            </span>
                            <span className='annotation-action-menu-items'>
                                {actionItems}
                            </span>
                        </li>
                    </ul>
                );
            }
            return (
                <ul
                    className='attribute-value-bvalue'
                >
                    <li className={cn('action-menu-wrapper', { 'annotation-attribute-error': this.state.hasError })}>
                        {key}
                        <span className='annotation-attribute-value-wrapper' onClick={() => this.onBValueEdit(true)}>
                            {literalNode.getValue()}
                        </span>
                        <span className='annotation-action-menu-items'>
                            {actionItems}
                        </span>
                    </li>
                </ul>
            );
        } else if (attributeValue.isValueAnnotationAttachment()) {
            const annotationAttachment = attributeValue.getValue();
            const packageName = (<span>{annotationAttachment.getPackageAlias().getValue()}</span>);
            const name = (<span>{annotationAttachment.getAnnotationName().getValue()}</span>);
            const actionMenuItems = [];
            const fullPackageName = AnnotationHelper.resolveFullPackageName(this.context.astRoot,
                                                                    annotationAttachment.getPackageAlias().getValue());
            const annotationDefinition = AnnotationHelper.getAnnotationDefinition(
                this.context.environment, fullPackageName, annotationAttachment.getAnnotationName().getValue());

            // Delete button.
            const deleteButton = {
                key: this.props.model.getID(),
                icon: 'fw-cancel',
                text: 'Delete',
                onClick: () => {
                    deleteNode(this.props.model);
                },
            };
            actionMenuItems.push(deleteButton);
            if (annotationDefinition && annotationDefinition.getAnnotationAttributeDefinitions().length > 0) {
                // Add attribute button
                const addAttributeButton = {
                    key: this.props.model.getID(),
                    icon: 'fw-add',
                    text: 'Add Attribute',
                    onClick: () => {
                        this.props.model.addingEmptyAttribute = true;
                        this.setState({
                            addingEmptyAttribute: true,
                        });
                    },
                };
                actionMenuItems.push(addAttributeButton);
            }

            const actionItems = actionMenuItems.map((item) => {
                return (<i
                    key={`${item.key}-action-menu-item-${item.text.toLowerCase().replace(/\s/g, '')}`}
                    className={'fw ' + item.icon}
                    onClick={item.onClick}
                />);
            });

            const attributes = this.props.model.viewState.collapsed ? [] :
                this.renderAnnotationAttributes(annotationAttachment);
            let emptyAttribute = (null);
            if (this.state.addingEmptyAttribute) {
                emptyAttribute = <li>{this.renderEmptyAttribute(annotationAttachment)}</li>;
            }
            return (
                <ul
                    className='attribute-value-annotation'
                >
                    <li className={cn('action-menu-wrapper', { 'annotation-attribute-error': this.state.hasError })}>
                        <CSSTransitionGroup
                            component='span'
                            transitionName='annotation-expand'
                            transitionEnterTimeout={300}
                            transitionLeaveTimeout={300}
                        >
                            <i
                                className={cn('fw fw-right expand-icon',
                                    { 'fw-rotate-90': !this.props.model.viewState.collapsed },
                                    { invisible: annotationAttachment.getAttributes().length === 0 })}
                                onClick={this.toggleCollapse}
                            />
                        </CSSTransitionGroup>
                        {key}
                        <span className='annotation-attachment-package-name annotation-attribute-value-wrapper'>
                            @{packageName}
                        </span>
                        :{name}
                        <span className='annotation-attachment-badge hide'>
                            <i className='fw fw-annotation-badge' />
                        </span>
                        <span className='annotation-action-menu-items'>
                            {actionItems}
                        </span>
                    </li>
                    {emptyAttribute}
                    {attributes}
                </ul>
            );
        } else if (attributeValue.isValueArray()) {
            const actionMenuItems = [];
            // Delete button.
            const deleteButton = {
                key: this.props.model.getID(),
                icon: 'fw-cancel',
                text: 'Delete',
                onClick: () => {
                    deleteNode(this.props.model);
                },
            };
            const addNewToArray = {
                key: this.props.model.getID(),
                icon: 'fw-add',
                text: 'Add Value',
                onClick: () => {
                    attributeValue.addValueArray(getArrayValue(
                        this.context.environment, this.props.model.getName().getValue(),
                                                                                this.props.annotationDefinitionModel));
                },
            };
            actionMenuItems.push(deleteButton);
            actionMenuItems.push(addNewToArray);

            const actionItems = actionMenuItems.map((item) => {
                return (<i
                    key={`${item.key}-action-menu-item-${item.text.toLowerCase().replace(/\s/g, '')}`}
                    className={'fw ' + item.icon}
                    onClick={item.onClick}
                />);
            });
            const arrayValues = this.props.model.viewState.collapsed ? [] :
                attributeValue.getValueArray().map((annotationAttributeValue) => {
                    return this.renderAnnotationAttributeValue(annotationAttributeValue);
                });
            return (
                <ul
                    className='attribute-value-array'
                >
                    <li className={cn('action-menu-wrapper', { 'annotation-attribute-error': this.state.hasError })}>
                        <CSSTransitionGroup
                            component='span'
                            transitionName='annotation-expand'
                            transitionEnterTimeout={300}
                            transitionLeaveTimeout={300}
                        >
                            <i
                                className={cn('fw fw-right expand-icon',
                                    { 'fw-rotate-90': !this.props.model.viewState.collapsed },
                                    { invisible: attributeValue.getValueArray().length === 0 })}
                                onClick={this.toggleCollapse}
                            />
                        </CSSTransitionGroup>
                        {key}
                        <span className='annotation-action-menu-items'>
                            {actionItems}
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
    model: PropTypes.instanceOf(AnnotationAttributeTreeNode).isRequired,
    annotationDefinitionModel: PropTypes.instanceOf(EnvAnnotationDefinition),
    parent: PropTypes.instanceOf(AnnotationAttachmentTreeNode),
    removeEmptyAttribute: PropTypes.func,
};

AnnotationAttribute.defaultProps = {
    annotationDefinitionModel: undefined,
    parent: undefined,
    removeEmptyAttribute: undefined,
};

AnnotationAttribute.contextTypes = {
    astRoot: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default AnnotationAttribute;
