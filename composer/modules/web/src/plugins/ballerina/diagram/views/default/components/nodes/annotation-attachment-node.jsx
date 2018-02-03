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
import AnnotationAttachmentTreeNode from 'plugins/ballerina/model/tree/annotation-attachment-node';
import AnnotationHelper from 'plugins/ballerina/env/helpers/annotation-helper';
import NodeFactory from 'plugins/ballerina/model/node-factory';
import { deleteNode, addAttribute } from '../decorators/annotation-button-events';
import AnnotationAttachmentAttribute from './annotation-attachment-attribute-node';
import AutoSuggestHtml from '../decorators/autosuggest-html';
import ActionMenu from '../decorators/action-menu';

/**
 * React component for an {@link AnnotationAttachmentNode} AST
 *
 * @class Annotation
 * @extends {React.Component}
 */
class AnnotationAttachmentNode extends React.Component {

    /**
     * Creates an instance of Annotation.
     * @param {Object} props React properties
     *
     * @memberof AnnotationAttachmentNode
     */
    constructor(props) {
        super(props);
        this.state = {
            isInEdit: false,
            hasError: false,
            addingEmptyAttribute: props.model.viewState.addingEmptyAttribute,
        };

        this.onNameEdit = this.onNameEdit.bind(this);
        this.onNameEditFinished = this.onNameEditFinished.bind(this);
        this.onPackageNameEdit = this.onPackageNameEdit.bind(this);
        this.onPackageNameEditFinished = this.onPackageNameEditFinished.bind(this);
        this.onPackageNameSelected = this.onPackageNameSelected.bind(this);
        this.onNameSelected = this.onNameSelected.bind(this);
        this.toggleCollapse = this.toggleCollapse.bind(this);
        this.removeEmptyAttribute = this.removeEmptyAttribute.bind(this);
    }

    /**
     * Event when a name is selected for the annotation attachment.
     *
     * @param {Object} event The event object.
     * @param {string} suggestionValue The selected value.
     * @memberof AnnotationAttachmentNode
     */
    onNameSelected(event, { suggestionValue }) {
        if (this.props.model.getAnnotationName() !== suggestionValue) {
            this.props.model.setAnnotationName(suggestionValue);

            // Removing the children of the annotation.
            this.props.model.getAttributes().forEach((attribute) => {
                this.props.model.removeAttributes(attribute);
            });
        }
        this.setState({ isInEdit: false });
    }

    /**
     * Event when a package name is selected.
     *
     * @param {Object} event The actualy event.
     * @param {string} suggestionValue The selected value.
     * @memberof AnnotationAttachmentNode
     */
    onPackageNameSelected(event, { suggestionValue }) {
        const selectedPackageAlias = suggestionValue.split('.').pop();
        if (this.props.model.getPackageAlias() !== selectedPackageAlias) {
            this.props.model.setPackageAlias(selectedPackageAlias);
            // Add import if not imported to AST-Root.
            const importToBeAdded = NodeFactory.createImport({
                alias: selectedPackageAlias,
                packageName: suggestionValue.split('.'),
            });
            this.context.astRoot.addImports(importToBeAdded);

            // Removing the children of the annotation.
            this.props.model.getAttributes().forEach((attribute) => {
                this.props.model.removeAttributes(attribute);
            });
        }

        this.setState({ isInEdit: false });
    }

    /**
     * When the annotation attachment name is to be edited.
     * @memberof AnnotationAttachmentNode
     */
    onNameEdit() {
        this.setState({ isInEdit: true });
    }

    /**
     * When the annotation attachment name is edited.
     * @memberof AnnotationAttachmentNode
     */
    onNameEditFinished() {
        this.setState({ isInEdit: false });
    }

    /**
     * When the annotation attachment package name is to be edited.
     * @memberof AnnotationAttachmentNode
     */
    onPackageNameEdit() {
        this.setState({ isInEdit: true });
    }

    /**
     * When the annotation attachment package name is edited.
     * @memberof AnnotationAttachmentNode
     */
    onPackageNameEditFinished() {
        this.setState({ isInEdit: false });
    }

    /**
     * Event for collpasing the view.
     * @memberof AnnotationAttachmentNode
     */
    toggleCollapse() {
        this.props.model.viewState.collapsed = !this.props.model.viewState.collapsed;
        this.context.editor.update();
    }

    /**
     * Removes empty attribute adding state.
     * @memberof AnnotationAttachmentNode
     */
    removeEmptyAttribute() {
        this.props.model.viewState.addingEmptyAttribute = false;
        this.setState({
            addingEmptyAttribute: false,
        });
    }

    /**
     * Renders the annotation attributes of the annotation attachment.
     *
     * @returns {AnnotationAttachmentAttribute[]} Annotation attribute components.
     * @memberof AnnotationAttachmentNode
     */
    renderAttributes() {
        const model = this.props.model;
        const componentsOfAttributes = [];
        const annotationDefModel = AnnotationHelper.getAnnotationDefinition(
            this.context.environment, AnnotationHelper.resolveFullPackageName(
                this.context.astRoot, this.props.model.getPackageAlias().getValue()),
            this.props.model.getAnnotationName().getValue());
        if (annotationDefModel) {
            annotationDefModel.setPackagePath(AnnotationHelper.resolveFullPackageName(
                this.context.astRoot, this.props.model.getPackageAlias().getValue()));
            model.getAttributes().forEach((attribute) => {
                componentsOfAttributes.push(<AnnotationAttachmentAttribute
                    key={attribute.getID()}
                    model={attribute}
                    annotationDefinitionModel={annotationDefModel}
                />);
            });
        } else {
            model.getAttributes().forEach((attribute) => {
                componentsOfAttributes.push(<AnnotationAttachmentAttribute
                    key={attribute.getID()}
                    model={attribute}
                />);
            });
        }

        return componentsOfAttributes;
    }

    /**
     * Renders the name of the annotation attribute
     *
     * @returns {ReactElement} The name component.
     * @memberof AnnotationAttachmentNode
     */
    renderName() {
        if (this.state.isNameInEdit && !this.props.model.viewState.disableEdit) {
            const supportedNames = AnnotationHelper.getNames(
                this.context.environment, this.props.model.parent,
                AnnotationHelper.resolveFullPackageName(this.context.astRoot, this.props.model.getPackageAlias().getValue()));
            return (<AutoSuggestHtml
                items={supportedNames}
                placeholder={'Annotation Name'}
                initialValue={this.props.model.getAnnotationName().getValue() || ''}
                onSuggestionSelected={this.onNameSelected}
                minWidth={130}
                maxWidth={1000}
                onBlur={this.onNameEditFinished}
                showAllAtStart
            />);
        }

        return (<span onClick={this.onNameEdit}>{this.props.model.getAnnotationName().getValue()}</span>);
    }

    /**
     * Renders the action menu.
     *
     * @returns {ActionMenu} Action menu view.
     * @memberof AnnotationAttachmentNode
     */
    renderActionMenu() {
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

        const annotationDefinition = AnnotationHelper.getAnnotationDefinition(
            this.context.environment, AnnotationHelper.resolveFullPackageName(
                                            this.context.astRoot, this.props.model.getPackageAlias().getValue()),
            this.props.model.getAnnotationName().getValue());
        if (annotationDefinition && annotationDefinition.getAnnotationAttributeDefinitions().length > 0) {
            // Add attribute button
            const addAttributeButton = {
                key: this.props.model.getID(),
                icon: 'fw-add',
                text: 'Add Attribute',
                onClick: () => {
                    this.props.model.viewState.collapsed = false;
                    this.props.model.viewState.addingEmptyAttribute = true;
                    this.setState({
                        addingEmptyAttribute: true,
                    });
                    this.context.editor.update();
                },
            };
            actionMenuItems.push(addAttributeButton);
        }
        return actionMenuItems.map((item) => {
            return (<i
                key={`${item.key}-action-menu-item-${item.text.toLowerCase().replace(/\s/g, '')}`}
                className={'fw ' + item.icon}
                onClick={item.onClick}
            />);
        });
        // return <ActionMenu items={actionMenuItems} />;
    }

    /**
     * Renders the package name view of the annotation attachment.
     *
     * @returns {ReactElement} The package name
     * @memberof AnnotationAttachmentNode
     */
    renderPackageName() {
        const supportedPackageNames = AnnotationHelper.getPackageNames(
            this.context.environment, this.props.model.parent);
        if (this.state.isPackageNameInEdit && !this.props.model.viewState.disableEdit) {
            return (<span>@
                <AutoSuggestHtml
                    items={supportedPackageNames}
                    placeholder={'Package Name'}
                    initialValue={AnnotationHelper.resolveFullPackageName(
                        this.context.astRoot, this.props.model.getPackageAlias().getValue()) || ''}
                    onSuggestionSelected={this.onPackageNameSelected}
                    minWidth={130}
                    maxWidth={1000}
                    onBlur={this.onPackageNameEditFinished}
                    showAllAtStart
                />:
            </span>);
        }
        if (this.props.model.getPackageAlias().getValue() &&
            this.props.model.getPackageAlias().getValue() !== 'builtin') {
            return (<span className='annotation-attachment-package-name'>@
                <span
                    onClick={this.onPackageNameEdit}
                >{this.props.model.getPackageAlias().getValue()}:</span>
            </span>);
        } else {
            return (<span className='annotation-attachment-package-name'>@</span>);
        }
    }

    /**
     * Renders an empty attribute.
     * @returns {ReactElement} The attribute view.
     * @memberof AnnotationAttachmentNode
     */
    renderEmptyAttribute() {
        const annotationDefModel = AnnotationHelper.getAnnotationDefinition(
            this.context.environment, AnnotationHelper.resolveFullPackageName(
                this.context.astRoot, this.props.model.getPackageAlias().getValue()),
            this.props.model.getAnnotationName().getValue());
        if (annotationDefModel) {
            annotationDefModel.setPackagePath(AnnotationHelper.resolveFullPackageName(
                this.context.astRoot, this.props.model.getPackageAlias().getValue()));
            return (<AnnotationAttachmentAttribute
                key='new-annotation-attribute'
                model={addAttribute()}
                annotationDefinitionModel={annotationDefModel}
                parent={this.props.model}
                removeEmptyAttribute={this.removeEmptyAttribute}
            />);
        }
        return (null);
    }

    /**
     * Renders the view for an annotation attachment.
     *
     * @returns {ReactElement} JSX of the annotation component.
     * @memberof AnnotationAttachmentNode
     */
    render() {
        const packageName = this.renderPackageName();
        const name = this.renderName();
        const actionMenu = this.renderActionMenu();
        let emptyAttribute = (null);
        if (this.state.addingEmptyAttribute) {
            emptyAttribute = <li>{this.renderEmptyAttribute()}</li>;
        }
        const attributes = this.props.model.viewState.collapsed ? (null) : <li>{this.renderAttributes()}</li>;
        return (<ul className='annotation-attachment-ul'>
            <li className={cn('annotation-attachment-text-li', 'action-menu-wrapper',
                { 'annotation-attachment-error': this.state.hasError })}
            >
                <span className={cn('annotations-array-item-prefix', { hide: !this.props.isNestedAnnotation })}>
                    <i className='fw fw-minus' />
                </span>
                <CSSTransitionGroup
                    component='span'
                    transitionName='annotation-expand'
                    transitionEnterTimeout={300}
                    transitionLeaveTimeout={300}
                >
                    <i
                        key={`${this.props.model.getID()}-collapser`}
                        className={cn('fw fw-right expand-icon',
                            { 'fw-rotate-90': !this.props.model.viewState.collapsed },
                            { invisible: this.props.model.getAttributes().length === 0 })}
                        onClick={this.toggleCollapse}
                    />
                </CSSTransitionGroup>
                {packageName}{name}
                <span className='annotation-attachment-badge hide'>
                    <i className='fw fw-annotation-badge' />
                </span>
                <span className='annotation-action-menu-items'>
                    {actionMenu}
                </span>
            </li>
            {emptyAttribute}
            {attributes}
        </ul>);
    }
}

AnnotationAttachmentNode.propTypes = {
    model: PropTypes.instanceOf(AnnotationAttachmentTreeNode).isRequired,
    isNestedAnnotation: PropTypes.bool,
};

AnnotationAttachmentNode.defaultProps = {
    isNestedAnnotation: false,
};

AnnotationAttachmentNode.contextTypes = {
    // Used for accessing ast-root to add imports
    astRoot: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default AnnotationAttachmentNode;
