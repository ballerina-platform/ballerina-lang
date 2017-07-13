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
import AnnotationAttachment from './../ast/annotations/annotation-attachment';
import AutoSuggestHtml from './utils/autosuggest-html';
import ASTFactory from './../ast/ballerina-ast-factory';

/**
 * React component for the package name of an annotation attachment.
 *
 * @class AnnotationAttachmentPackageName
 * @extends {React.Component}
 */
class AnnotationAttachmentPackageName extends React.Component {

    /**
     * Creates an instance of AnnotationAttachmentPackageName.
     * @param {Object} props React properties.
     * @memberof AnnotationAttachmentPackageName
     */
    constructor(props) {
        super(props);

        this.onPackageNameBlur = this.onPackageNameBlur.bind(this);
        this.onPackageNameSelected = this.onPackageNameSelected.bind(this);
    }

    /**
     * Event when the package name is blurred/out focused.
     *
     * @memberof AnnotationAttachmentPackageName
     */
    onPackageNameBlur() {
        this.props.isEditStateFunc(false);
    }

    /**
     * Event when a package name is selected.
     *
     * @param {Object} event The actual event
     * @param {string} suggestionValue The selected value.
     * @memberof AnnotationAttachmentPackageName
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
            importToBeAdded.setParent(this.context.astRoot);
            this.context.astRoot.addImport(importToBeAdded);

            // Removing the children of the annotation.
            this.props.model.getChildren().forEach((attribute) => {
                this.props.model.removeChild(attribute);
            });

            this.props.model.setName(undefined);

            this.props.isEditStateFunc(false);
        }
    }

    /**
     * Renders the view for an annotation
     *
     * @returns {ReactElement} JSX of the annotation component.
     * @memberof AnnotationAttachmentPackageName
     */
    render() {
        if (this.props.isInEdit && !this.props.model.getViewState().disableEdit) {
            const model = this.props.model;
            return (<span>@
                <AutoSuggestHtml
                    items={this.props.supportedPackageNames}
                    placeholder={'Package Name'}
                    initialValue={model.getFullPackageName() || ''}
                    onSuggestionSelected={this.onPackageNameSelected}
                    onBlur={this.onPackageNameBlur}
                    minWidth={130}
                    maxWidth={1000}
                />:
            </span>);
        }

        let packageName = '';
        if (!(this.props.model.getPackageName() === undefined || this.props.model.getPackageName() === null)) {
            packageName = `${this.props.model.getPackageName()}:`;
        }

        return (<span
            className="annotation-attachment-package-name"
        >@{packageName}</span>);
    }
}

AnnotationAttachmentPackageName.propTypes = {
    model: PropTypes.instanceOf(AnnotationAttachment).isRequired,
    supportedPackageNames: PropTypes.arrayOf(PropTypes.string),
    isInEdit: PropTypes.bool,
    isEditStateFunc: PropTypes.func.isRequired,
};

AnnotationAttachmentPackageName.defaultProps = {
    supportedPackageNames: [],
    isInEdit: false,
};

AnnotationAttachmentPackageName.contextTypes = {
    // Used for accessing ast-root to add imports
    astRoot: PropTypes.instanceOf(Object).isRequired,
};

export default AnnotationAttachmentPackageName;
