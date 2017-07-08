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
import AnnotationAttribute from './annotation-attribute';
import BallerinaEnvironment from '../env/environment';
import AutoSuggestHtml from './utils/autosuggest-html';
import ASTFactory from './../ast/ballerina-ast-factory';

/**
 * React component for an {@link Annotation} AST
 *
 * @class Annotation
 * @extends {React.Component}
 */
class Annotation extends React.Component {

    /**
     * Creates an instance of Annotation.
     * @param {Object} props React properties
     *
     * @memberof Annotation
     */
    constructor(props) {
        super(props);
        this.state = {
            // Is in edit state
            isInEdit: false || props.model.getIdentifier() === '',
            packageNameSelected: !_.isUndefined(props.model.getPackageName()),
        };

        this.deleteAnnotation = this.deleteAnnotation.bind(this);
        this.onClickAnnotation = this.onClickAnnotation.bind(this);
        this.addAnnotationAttribute = this.addAnnotationAttribute.bind(this);
        this.onIdentifierSelected = this.onIdentifierSelected.bind(this);
        this.onIdentifierKeyDown = this.onIdentifierKeyDown.bind(this);
        this.onPackageNameSelected = this.onPackageNameSelected.bind(this);
    }

    /**
     * Event when clicking on an annotation allowing to edit.
     *
     * @param {any} e The actual event
     *
     * @memberof Annotation
     */
    onClickAnnotation(e) {
        this.setState({ isInEdit: true });
        e.preventDefault();
        e.stopPropagation();
    }

    /**
     * Keydown event when editing the identifier of the annotation.
     *
     * @param {any} e The actual event
     *
     * @memberof Annotation
     */
    onIdentifierKeyDown(e) {
        if (e.keyCode === 8 && e.target.value === '') {
            this.props.model.setIdentifier('', { doSilently: true });
            this.setState({
                packageNameSelected: false,
            });
            e.preventDefault();
        }
    }

    /**
     * Event for selecting an identifier from the dropdown
     *
     * @param {Object} event The actual event
     * @param {string} suggestionValue The selected value.
     *
     * @memberof Annotation
     */
    onIdentifierSelected(event, { suggestionValue }) {
        this.setState({
            isInEdit: false,
        });
        this.props.model.setIdentifier(suggestionValue);
    }

    /**
     * Event when a package is selected from a dropdown.
     *
     * @param {Object} event The actual event
     * @param {string} suggestionValue The selected value
     *
     * @memberof Annotation
     */
    onPackageNameSelected(event, { suggestionValue }) {
        this.setState({
            packageNameSelected: true,
            isInEdit: true,
        });

        if (this.props.model.getFullPackageName() !== suggestionValue) {
            this.props.model.setFullPackageName(suggestionValue, { doSilently: true });
            this.props.model.setPackageName(suggestionValue.split('.').pop(), { doSilently: true });
            // Add import if not imported to AST-Root.
            const importToBeAdded = ASTFactory.createImportDeclaration({
                packageName: suggestionValue,
            });
            this.context.astRoot.addImport(importToBeAdded);

            // Removing the children of the annotation.
            for (const annotationAttribute of this.props.model.getChildren()) {
                this.props.model.removeChild(annotationAttribute);
            }
        }
    }

    /**
     * Gets an array of annotation attribute definitions.
     *
     * @param {string} fullPackageName The complete package name.
     * @param {string} identifier The identifier of the attribute.
     * @returns {AnnotationAttributeDefinition[]} Matching annotation attribute definitions.
     *
     * @memberof AnnotationAttribute
     */
    getAnnotationAttributes(fullPackageName, identifier) {
        const annotationAttributes = [];
        for (const packageDefintion of BallerinaEnvironment.getPackages()) {
            if (packageDefintion.getName() === fullPackageName) {
                for (const annotationDefinition of packageDefintion.getAnnotationDefinitions()) {
                    if (annotationDefinition.getName() === identifier) {
                        for (const annotationAttribute of annotationDefinition.getAnnotationAttributeDefinitions()) {
                            annotationAttributes.push(annotationAttribute);
                        }
                    }
                }
            }
        }

        return annotationAttributes;
    }

    /**
     * Gets the identifiers as suggestions.
     *
     * @returns {string[]} An array of identifiers
     *
     * @memberof Annotation
     */
    getIdentifiersForSuggestion() {
        const annotationIdentifiers = [];
        for (const packageDefintion of BallerinaEnvironment.getPackages()) {
            if (packageDefintion.getName() === this.props.model.getFullPackageName()) {
                for (const annotationDefinition of packageDefintion.getAnnotationDefinitions()) {
                    annotationIdentifiers.push(annotationDefinition.getName());
                    // TODO : Ignore already added annotations
                }
            }
        }

        return annotationIdentifiers;
    }

    /**
     * Get the supported package names as suggestions
     *
     * @returns {string[]} An array of package names
     *
     * @memberof Annotation
     */
    getPackageNamesForSuggestion() {
        const packageNames = new Set();
        let attachmentType = '';
        if (ASTFactory.isServiceDefinition(this.props.model.getParent())) {
            attachmentType = 'service';
        } else if (ASTFactory.isResourceDefinition(this.props.model.getParent())) {
            attachmentType = 'resource';
        }
        for (const packageDefintion of BallerinaEnvironment.getPackages()) {
            for (const annotationDefinition of packageDefintion.getAnnotationDefinitions()) {
                if (annotationDefinition.getAttachmentPoints().length === 0 ||
                    _.includes(annotationDefinition.getAttachmentPoints(), attachmentType)) {
                    // Filtering by ignoring already added annotations.
                    let annotationAlreadyExists = false;
                    if (this.props.model.getParent()) {
                        const parentNodeAnnotations =
                                                this.props.model.getParent().getChildrenOfType(ASTFactory.isAnnotation);
                        for (const annotation of parentNodeAnnotations) {
                            if (annotation.getFullPackageName() === annotationDefinition.getPackagePath() &&
                                annotation.getIdentifier() === annotationDefinition.getName()) {
                                annotationAlreadyExists = true;
                                break;
                            }
                        }
                    }

                    if (!annotationAlreadyExists) {
                        packageNames.add(packageDefintion.getName());
                    }
                }
            }
        }

        return Array.from(packageNames);
    }

    /**
     * Adds new attribute to the annotation.
     *
     *
     * @memberof Annotation
     */
    addAnnotationAttribute() {
        this.props.model.addChild(ASTFactory.createAnnotationEntry({ leftValue: '', rightValue: '' }));
        this.setState(this.state);
    }

    /**
     * Event for deleting an annotation.
     *
     *
     * @memberof Annotation
     */
    deleteAnnotation() {
        this.props.model.parent.removeChild(this.props.model);
    }

    /**
     * Renders the view for editing
     *
     * @param {object} model The annotation AST model
     * @returns {ReactElement} A JSX of editing the annotation.
     *
     * @memberof Annotation
     */
    renderEditView(model) {
        if (this.state.packageNameSelected) {
            return (<td className="annotation-key">
                <span className="package-name"><span>@</span>{model.getPackageName()}</span>
                <span className="annotation-separator">:</span>
                <AutoSuggestHtml
                    items={this.getIdentifiersForSuggestion()}
                    placeholder={'Identifier'}
                    initialValue={model.getIdentifier()}
                    onSuggestionSelected={this.onIdentifierSelected}
                    onKeyDown={this.onIdentifierKeyDown}
                    showAllAtStart
                    minWidth={130}
                    maxWidth={1000}
                />
            </td>);
        }
        return (<td className="annotation-key">
            <span className="package-name">@</span>
            <AutoSuggestHtml
                items={this.getPackageNamesForSuggestion()}
                placeholder={'Add Annotation'}
                initialValue={model.getFullPackageName()}
                onSuggestionSelected={this.onPackageNameSelected}
                showAllAtStart
                minWidth={150}
                maxWidth={1000}
            />
        </td>);
    }

    /**
     * Renders the view for an annotation
     *
     * @returns {ReactElement} JSX of the annotation component.
     *
     * @memberof Annotation
     */
    render() {
        const model = this.props.model;

        let removeIcon = (null);
        if (!_.isNil(this.props.removeIcon)) {
            removeIcon = this.props.removeIcon;
        } else {
            removeIcon = (<div className="annotation-remove" onClick={this.deleteAnnotation}>
                <i className="fw fw-cancel" />
            </div>);
        }

        let key;
        let annotationKeyPart = (null);
        if (this.state.isInEdit) {
            annotationKeyPart = this.renderEditView(model);
        } else {
            annotationKeyPart = (<td className="annotation-key" onClick={this.onClickAnnotation}>
                <span className="package-name"><span>@</span>{model.getPackageName()}</span>
                :
                <span className="identifier">{model.getIdentifier()}</span>
            </td>);
        }

        let addIcon = (null);
        const attributesOfCurrentAnnotation = this.getAnnotationAttributes(
            this.props.model.getFullPackageName(), this.props.model.getIdentifier());
        if (attributesOfCurrentAnnotation.length > this.props.model.getChildren().length &&
            this.getAnnotationAttributes(model.getFullPackageName(), model.getIdentifier()).length > 0) {
            addIcon = (<div className="annotation-attribute-add" onClick={this.addAnnotationAttribute}>
                <i className="fw fw-add" />
            </div>);
        }
        if (model.children.length === 0) {
            key = (<tr>
                {annotationKeyPart}
                <td>
                    <span className="annotation-openning-bracket">{'{ '}{addIcon}{' }'}{removeIcon}</span>
                </td>
            </tr>);
        } else {
            key = (<tr>
                {annotationKeyPart}
                <td>
                    <span className="annotation-openning-bracket">{'{'}{addIcon}{removeIcon}</span>
                </td>
            </tr>);
        }

        const annotationAttributeComponents = [];
        for (const annotationAttribute of model.getChildren()) {
            annotationAttributeComponents.push(
                <tr
                    key={annotationAttribute.getID()}
                    className="annotation-attribute-row"
                >
                    <td />
                    <td>
                        <table>
                            <tbody>
                                <AnnotationAttribute
                                    model={annotationAttribute}
                                    annotationAttributes={this.getAnnotationAttributes(model.getFullPackageName(),
                                                                                                model.getIdentifier())}
                                />
                            </tbody>
                        </table>
                    </td>
                </tr>);
        }

        let closingBracket = <tr />;
        if (model.getChildren().length !== 0) {
            if (!_.isUndefined(this.props.haveEndingComma) && this.props.haveEndingComma === true) {
                closingBracket = (<tr>
                    <td>
                        <span className="annotation-closing-bracket">{'} ,'}</span>
                    </td>
                    <td />
                </tr>);
            } else {
                closingBracket = (<tr>
                    <td>
                        <span className="annotation-closing-bracket">{'}'}</span>
                    </td>
                    <td />
                </tr>);
            }
        }

        return (<table key={model.getID()} className="annotation-table" >
            <tbody>
                {key}
                {annotationAttributeComponents}
                {closingBracket}
            </tbody>
        </table>);
    }
}

Annotation.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
    haveEndingComma: PropTypes.bool,
    removeIcon: PropTypes.element,
};

Annotation.defaultProps = {
    haveEndingComma: false,
    removeIcon: (null),
};

Annotation.contextTypes = {
    // Used for accessing ast-root to add imports
    astRoot: PropTypes.instanceOf(Object).isRequired,
};


export default Annotation;
