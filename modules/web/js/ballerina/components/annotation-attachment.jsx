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
import AnnotationAttachmentAST from './../ast/annotations/annotation-attachment';
import AnnotationAttribute from './annotation-attribute';
import { deleteNode, addAttribute } from './utils/annotation-button-events';
import AnnotationHelper from '../env/helpers/annotation-helper';
import AutoSuggestHtml from './utils/autosuggest-html';
import ASTFactory from './../ast/ballerina-ast-factory';
import PopoutButton from './popout-button';

/**
 * React component for an {@link AnnotationAttachment} AST
 *
 * @class Annotation
 * @extends {React.Component}
 */
class AnnotationAttachment extends React.Component {

    /**
     * Creates an instance of Annotation.
     * @param {Object} props React properties
     *
     * @memberof AnnotationAttachment
     */
    constructor(props) {
        super(props);
        if (props.model.getFullPackageName() === undefined || props.model.getFullPackageName() === '') {
            this.state = {
                isPackageNameInEdit: true,
                hasError: true,
            };
        } else if (props.model.getPackageName() === undefined || props.model.getPackageName() === '') {
            this.state = {
                isPackageNameInEdit: true,
                hasError: true,
            };
        } else if (props.model.getName() === undefined || props.model.getName() === '') {
            this.state = {
                isNameInEdit: true,
                hasError: true,
            };
        } else {
            this.state = {
                isPackageNameInEdit: false,
                isNameInEdit: false,
                hasError: false,
            };
        }

        this.onNameEdit = this.onNameEdit.bind(this);
        this.onNameEditFinished = this.onNameEditFinished.bind(this);
        this.onPackageNameEdit = this.onPackageNameEdit.bind(this);
        this.onPackageNameEditFinished = this.onPackageNameEditFinished.bind(this);
        this.onPackageNameSelected = this.onPackageNameSelected.bind(this);
        this.onNameSelected = this.onNameSelected.bind(this);
    }

    /**
     * Event when a name is selected for the annotation attachment.
     *
     * @param {Object} event The event object.
     * @param {string} suggestionValue The selected value.
     * @memberof AnnotationAttachment
     */
    onNameSelected(event, { suggestionValue }) {
        if (this.props.model.getName() !== suggestionValue) {
            this.props.model.setName(suggestionValue);

            // Removing the children of the annotation.
            this.props.model.getChildren().forEach((attribute) => {
                this.props.model.removeChild(attribute);
            });
        }
        this.setState({ isNameInEdit: false });
    }

    /**
     * Event when a package name is selected.
     *
     * @param {Object} event The actualy event.
     * @param {string} suggestionValue The selected value.
     * @memberof AnnotationAttachment
     */
    onPackageNameSelected(event, { suggestionValue }) {
        if (this.props.model.getFullPackageName() !== suggestionValue) {
            const packageName = suggestionValue.split('.').pop();
            this.props.model.setFullPackageName(suggestionValue);
            this.props.model.setPackageName(packageName);
            // Add import if not imported to AST-Root.
            const importToBeAdded = ASTFactory.createImportDeclaration({
                packageName: suggestionValue,
            });
            importToBeAdded.setParent(this.context.renderingContext.ballerinaFileEditor.getModel());
            this.context.renderingContext.ballerinaFileEditor.getModel().addImport(importToBeAdded);

            // Removing the children of the annotation.
            this.props.model.getChildren().forEach((attribute) => {
                this.props.model.removeChild(attribute);
            });

            this.props.model.setName(undefined);
        }

        this.setState({ isPackageNameInEdit: false });
    }

    /**
     * When the annotation attachment name is to be edited.
     * @memberof AnnotationAttachment
     */
    onNameEdit() {
        this.setState({ isNameInEdit: true });
    }

    /**
     * When the annotation attachment name is edited.
     * @memberof AnnotationAttachment
     */
    onNameEditFinished() {
        this.setState({ isNameInEdit: false });
    }

    /**
     * When the annotation attachment package name is to be edited.
     * @memberof AnnotationAttachment
     */
    onPackageNameEdit() {
        this.setState({ isPackageNameInEdit: true });
    }

    /**
     * When the annotation attachment package name is edited.
     * @memberof AnnotationAttachment
     */
    onPackageNameEditFinished() {
        this.setState({ isPackageNameInEdit: false });
    }

    /**
     * Renders the annotation attributes of the annotation attachment.
     *
     * @returns {AnnotationAttribute[]} Annotation attribute components.
     * @memberof AnnotationAttachment
     */
    renderAttributes() {
        const model = this.props.model;
        const componentsOfAttributes = [];
        const annotationDefModel = AnnotationHelper.getAnnotationDefinition(model.getFullPackageName(),
                                                                                                model.getName());
        if (annotationDefModel) {
            model.getChildren().forEach((attribute) => {
                componentsOfAttributes.push(<AnnotationAttribute
                    key={attribute.getID()}
                    model={attribute}
                    annotationDefinitionModel={
                        AnnotationHelper.getAnnotationDefinition(model.getFullPackageName(), model.getName())}
                />);
            });
        } else {
            this.setState({ hasError: true });
        }

        return componentsOfAttributes;
    }

    /**
     * Renders the name of the annotation attribute
     *
     * @returns {ReactElement} The name component.
     * @memberof AnnotationAttachment
     */
    renderName() {
        const supportedNames = AnnotationHelper.getNames(this.props.model.getParent(), this.props.model.getFullPackageName());
        if (this.state.isNameInEdit && !this.props.model.getViewState().disableEdit) {
            const model = this.props.model;
            return (<AutoSuggestHtml
                items={supportedNames}
                placeholder={'Annotation Name'}
                initialValue={model.getName() || ''}
                onSuggestionSelected={this.onNameSelected}
                minWidth={130}
                maxWidth={1000}
                onBlur={this.onNameEditFinished}
                showAllAtStart
            />);
        }

        return (<span onClick={this.onNameEdit}>{this.props.model.getName()}</span>);
    }

    /**
     * Renders the operation buttons for an annotation attachment.
     *
     * @returns {PopoutButton} The operations button.
     * @memberof AnnotationAttachment
     */
    renderOperationsButton() {
        const buttons = [];
        if (AnnotationHelper.getAnnotationDefinition(this.props.model.getFullPackageName(),
                                        this.props.model.getName()).getAnnotationAttributeDefinitions().length > 0) {
            // Add attribute button
            const addAttributeButton = {
                icon: 'fw-add',
                text: 'Add Attribute',
                onClick: () => {
                    addAttribute(this.props.model);
                },
            };
            buttons.push(addAttributeButton);
        }

        // Delete button.
        const deleteButton = {
            icon: 'fw-cancel',
            text: 'Delete',
            onClick: () => {
                deleteNode(this.props.model);
            },
        };
        buttons.push(deleteButton);

        return <PopoutButton buttons={buttons} />;
    }

    /**
     * Renders the package name view of the annotation attachment.
     *
     * @returns {ReactElement} The package name
     * @memberof AnnotationAttachment
     */
    renderPackageName() {
        const supportedPackageNames = AnnotationHelper.getPackageNames(this.props.model.getParent());
        if (this.state.isPackageNameInEdit && !this.props.model.getViewState().disableEdit) {
            const model = this.props.model;
            return (<span>@
                <AutoSuggestHtml
                    items={supportedPackageNames}
                    placeholder={'Package Name'}
                    initialValue={model.getFullPackageName() || ''}
                    onSuggestionSelected={this.onPackageNameSelected}
                    minWidth={130}
                    maxWidth={1000}
                    onBlur={this.onPackageNameEditFinished}
                    showAllAtStart
                />:
            </span>);
        }

        let packageName = '';
        if (!(this.props.model.getPackageName() === undefined || this.props.model.getPackageName() === null)) {
            packageName = `${this.props.model.getPackageName()}:`;
        }

        return (<span
            className="annotation-attachment-package-name"
            onClick={this.onPackageNameEdit}
        >@{packageName}</span>);
    }

    /**
     * Renders the view for an annotation attachment.
     *
     * @returns {ReactElement} JSX of the annotation component.
     * @memberof AnnotationAttachment
     */
    render() {
        const packageName = this.renderPackageName();
        const name = this.renderName();
        const operationsButton = this.renderOperationsButton();
        let errorClass = '';
        if (this.state.hasError) {
            errorClass = 'annotation-attachment-error';
        }

        const hasAttributes = this.props.model.getChildren().length > 0;
        if (hasAttributes) {
            const attributes = this.renderAttributes();
            return (
                <ul className="annotation-attachment-ul">
                    <li className={`annotation-attachment-text-li ${errorClass}`}>
                        {packageName}
                        <span className='annotation-attachment-name'>{name}</span>
                        <span className="annotations-open-bracket">{'{'}</span>
                        {operationsButton}
                    </li>
                    <li>
                        {attributes}
                    </li>
                    <li>
                        <span className="annotations-close-bracket">{'}'}</span>
                    </li>
                </ul>
            );
        }
        return (<ul className="annotation-attachment-ul">
            <li className={`annotation-attachment-text-li ${errorClass}`}>
                {packageName}{name}
                <span className="annotations-open-bracket">{'{'}</span>
                <span className="annotations-close-bracket">{'}'}</span>
                {operationsButton}
            </li>
        </ul>);
    }
}

AnnotationAttachment.propTypes = {
    model: PropTypes.instanceOf(AnnotationAttachmentAST).isRequired,
    parentASTNodeType: PropTypes.string,
};

AnnotationAttachment.defaultProps = {
    parentASTNodeType: '',
};

AnnotationAttachment.contextTypes = {
    // Used for accessing ast-root to add imports
    renderingContext: PropTypes.instanceOf(Object).isRequired,
};

export default AnnotationAttachment;
