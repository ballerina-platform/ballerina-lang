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
import ReactDOM from 'react-dom';
import PropTypes from 'prop-types';
import { getComponentForNodeArray } from './utils';
import Autosuggest from 'react-autosuggest';
import BallerinaEnvironment from '../env/environment';
import ASTFactory from './../ast/ballerina-ast-factory';
import _ from 'lodash';

/**
 * React component for the annotation container.
 * 
 * @class AnnotationContainer
 * @extends {React.Component}
 */
class AnnotationContainer extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            // Annotation package name states
            selectedPackageNameValue: '',
            shownPackageNameSuggestions: [],
            hasPackageNameSelected: false,

            // Annotation identifier states
            selectedIdentifierValue: '',
            shownIdentifierSuggestions: [],
        };

        this.onAnnotationPackageNameChange = this.onAnnotationPackageNameChange.bind(this);
        this.onAnnotationPackageNameBlur = this.onAnnotationPackageNameBlur.bind(this);
        this.onAnnotationPackageNameSuggestionsFetchRequested = this.onAnnotationPackageNameSuggestionsFetchRequested.bind(this);
        this.onAnnotationPackageNameSuggestionsClearRequested = this.onAnnotationPackageNameSuggestionsClearRequested.bind(this);
        this.onAnnotationPackageNameSelected = this.onAnnotationPackageNameSelected.bind(this);

        this.onAnnotationIdentifierChange = this.onAnnotationIdentifierChange.bind(this);
        this.onAnnotationIdentifierKeyDown = this.onAnnotationIdentifierKeyDown.bind(this);
        this.onAnnotationIdentifierSuggestionsFetchRequested = this.onAnnotationIdentifierSuggestionsFetchRequested.bind(this);
        this.onAnnotationIdentifierSuggestionsClearRequested = this.onAnnotationIdentifierSuggestionsClearRequested.bind(this);
        this.onAnnotationIdentifierSelected = this.onAnnotationIdentifierSelected.bind(this);

        this.storeCurrentInputReference = autosuggest => {
            if (autosuggest !== null) {
                this.currentInput = autosuggest.input;
            }
        };
    }

    /**
     * Rendering the annotation editor view.
     * 
     * @returns annotation editor view jsx
     * 
     * @memberof AnnotationContainer
     */
    render() {
        // Creating style object for positioning the annotation container.
        let bBox = this.props.model.bBox;
        let style = {
            position: 'absolute',
            top: bBox.y + 10,
            left: bBox.x - 1,
            width: bBox.w + 2,
            height: bBox.h
        };

        // Getting the components for the annotation of the current model.
        let annotations = getComponentForNodeArray(this.props.model.annotations);

        if (this.state.hasPackageNameSelected) {
            // Input properties for the package name
            const inputProps = {
                placeholder: 'Annotation Identifier',
                value: this.state.selectedIdentifierValue,
                onChange: this.onAnnotationIdentifierChange,
                onKeyDown: this.onAnnotationIdentifierKeyDown,
            };

            return <div style={style} className="annotation-container">
                {annotations}
                <div className='annotation-add-wrapper'>
                    <span className='annotation-add-at-sign'>@</span>
                    <span className='annotation-package-name'>{this.state.selectedPackageNameValue.split('.').pop()}:</span>
                    <Autosuggest
                        suggestions={this.state.shownIdentifierSuggestions}
                        onSuggestionsFetchRequested={this.onAnnotationIdentifierSuggestionsFetchRequested}
                        onSuggestionsClearRequested={this.onAnnotationIdentifierSuggestionsClearRequested}
                        onSuggestionSelected={this.onAnnotationIdentifierSelected}
                        getSuggestionValue={this.getAnnotationIdentifierSuggestionValue}
                        renderSuggestion={this.renderAnnotationIdentifierSuggestion}
                        ref={this.storeCurrentInputReference}
                        shouldRenderSuggestions={() => true}
                        inputProps={inputProps} />
                </div>
            </div>;
        } else {
            // Input properties for the package name
            const inputProps = {
                placeholder: 'Annotation Package',
                value: this.state.selectedPackageNameValue,
                onChange: this.onAnnotationPackageNameChange,
                onBlur: this.onAnnotationPackageNameBlur
            };

            return <div style={style} className="annotation-container">
                {annotations}
                <div className='annotation-add-wrapper'>
                    <span className='annotation-add-at-sign'>@</span>
                    <Autosuggest
                        suggestions={this.state.shownPackageNameSuggestions}
                        onSuggestionsFetchRequested={this.onAnnotationPackageNameSuggestionsFetchRequested}
                        onSuggestionsClearRequested={this.onAnnotationPackageNameSuggestionsClearRequested}
                        onSuggestionSelected={this.onAnnotationPackageNameSelected}
                        getSuggestionValue={this.getAnnotationPackageNameSuggestionValue}
                        renderSuggestion={this.renderAnnotationPackageNameSuggestion}
                        shouldRenderSuggestions={() => true}
                        inputProps={inputProps} />
                </div>
            </div>;
        }
    }

    /**
     * Gets the supported annotation depending on the type of node
     * 
     * @returns Supported annotations.
     * 
     * @memberof AnnotationContainer
     */
    getSupportedAnnotation() {
        let supportedAnnotations = [];
        let attachmentType = '';
        if (ASTFactory.isServiceDefinition(this.props.model.parentNode)) {
            attachmentType = 'service';
        } else if (ASTFactory.isResourceDefinition(this.props.model.parentNode)) {
            attachmentType = 'resource';
        } else if (ASTFactory.isFunctionDefinition(this.props.model.parentNode)) {
            attachmentType = 'function';
        } else if (ASTFactory.isConnectorDefinition(this.props.model.parentNode)) {
            attachmentType = 'connector';
        } else if (ASTFactory.isConnectorAction(this.props.model.parentNode)) {
            attachmentType = 'action';
        } else if (ASTFactory.isAnnotationDefinition(this.props.model.parentNode)) {
            attachmentType = 'annotation';
        } else if (ASTFactory.isStructDefinition(this.props.model.parentNode)) {
            attachmentType = 'struct';
        } 
        // TODO : Add the rest of the attatchment points.

        for (let packageDefintion of BallerinaEnvironment.getPackages()) {
            for (let annotationDefinition of packageDefintion.getAnnotationDefinitions()) {
                if (_.includes(annotationDefinition.getAttachmentPoints(), attachmentType)) {
                    // Filtering by ignoring already added annotations.
                    let parentNodeAnnotations = this.props.model.parentNode.getChildrenOfType(ASTFactory.isAnnotation);
                    let annotationAlreadyExists = false;
                    for (let annotation of parentNodeAnnotations) {
                        if (annotation.getFullPackageName() === packageDefintion.getName() &&
                            annotation.getIdentifier() === annotationDefinition.getName()) {
                            annotationAlreadyExists = true;
                            break;
                        }
                    }

                    if (!annotationAlreadyExists) {
                        supportedAnnotations.push({ packageName: packageDefintion.getName(), annotationDefinition });
                    }
                }
            }
        }

        return supportedAnnotations;
    }

    /**
     * Gets the list of package names for suggestions
     * 
     * @returns Package names
     * 
     * @memberof AnnotationContainer
     */
    getPackageNameSuggestions(supportedAnnotations) {
        let tempPackageNameSuggestions = supportedAnnotations.map((supportedAnnotation) => {
            return {
                name: supportedAnnotation.packageName
            };
        });

        return tempPackageNameSuggestions.filter((obj, pos, arr) => {
            return arr.map(mapObj => mapObj['name']).indexOf(obj['name']) === pos;
        });
    }

    //// Start of package name autosuggest dropdown funcs.

    onAnnotationPackageNameChange(event, { newValue }) {
        this.setState({
            selectedPackageNameValue: newValue,
            hasPackageNameSelected: false
        });
    }

    onAnnotationPackageNameBlur() {
        this.setState({
            hasPackageNameSelected: false
        });
    }

    onAnnotationPackageNameSuggestionsFetchRequested({ value }) {
        this.setState({
            shownPackageNameSuggestions: this.getAnnotationPackageNameSuggestions(value)
        });
    }

    onAnnotationPackageNameSuggestionsClearRequested() {
        this.setState({
            shownPackageNameSuggestions: []
        });
    }

    onAnnotationPackageNameSelected() {
        this.setState({
            hasPackageNameSelected: true
        });
    }

    // https://developer.mozilla.org/en/docs/Web/JavaScript/Guide/Regular_Expressions#Using_Special_Characters
    escapeRegexCharacters(str) {
        return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    getAnnotationPackageNameSuggestions(value) {
        const escapedValue = this.escapeRegexCharacters(value.trim());

        const regex = new RegExp('^' + escapedValue, 'i');
        let supportedAnnotations = this.getSupportedAnnotation();
        let packageNameSuggestions = this.getPackageNameSuggestions(supportedAnnotations);

        return packageNameSuggestions.filter(packageName => regex.test(packageName.name));
    }

    getAnnotationPackageNameSuggestionValue(suggestion) {
        return suggestion.name;
    }

    renderAnnotationPackageNameSuggestion(suggestion) {
        return (
            <span>{suggestion.name}</span>
        );
    }

    //// End of package name autosuggest dropdown funcs.

    //// Start of identifier autosuggest dropdown funcs.

    onAnnotationIdentifierChange(event, { newValue }) {
        this.setState({
            selectedIdentifierValue: newValue
        });
    }

    onAnnotationIdentifierKeyDown(e) {
        if (e.keyCode === 8 && this.state.selectedIdentifierValue === '') {
            this.setState({
                hasPackageNameSelected: false,
                selectedIdentifierValue: ''
            });
            e.preventDefault();
        }
    }

    onAnnotationIdentifierSuggestionsFetchRequested({ value }) {
        this.setState({
            shownIdentifierSuggestions: this.getAnnotationIdentifierSuggestions(value)
        });
    }

    onAnnotationIdentifierSuggestionsClearRequested() {
        this.setState({
            shownIdentifierSuggestions: []
        });
    }

    onAnnotationIdentifierSelected(event, { suggestionValue }) {
        // Add annotation to the node(serviceDefinition, resourceDefinition, etc)
        // http://jsbin.com/orokep/edit?html,css,js,output
        var match = /([^.;+_]+)$/.exec(this.state.selectedPackageNameValue),
            packagePrefix = match && match[1];

        let newAnnotation = ASTFactory.createAnnotation({
            fullPackageName: this.state.selectedPackageNameValue,
            packageName: packagePrefix,
            identifier: suggestionValue,
        });

        this.props.model.parentNode.addChild(newAnnotation);

        // Add import if not imported to AST-Root.
        let importToBeAdded = ASTFactory.createImportDeclaration({
            packageName: this.state.selectedPackageNameValue
        });
        this.context.renderingContext.ballerinaFileEditor.getModel().addImport(importToBeAdded);

        // Resetting the state of the component.
        this.setState({
            selectedPackageNameValue: '',
            hasPackageNameSelected: false,
            selectedIdentifierValue: ''
        });
    }

    getAnnotationIdentifierSuggestions(value) {
        const escapedValue = this.escapeRegexCharacters(value.trim());
        const regex = new RegExp('^' + escapedValue, 'i');

        // Get the list of annotations that belongs to the selected package. this.supportedAnnotations is already filtered by attatchment points.
        let selectedAnnotations = this.getSupportedAnnotation().filter(
            annotationObj => annotationObj.packageName === this.state.selectedPackageNameValue
        );

        // Get an object array of the supported annotations with the name/identifier of an annotation.
        let annotationNames = selectedAnnotations.map(supportedAnnotation => {
            return {
                name: supportedAnnotation.annotationDefinition.getName()
            };
        });

        // Filtering the annotations with the typed in value.
        return annotationNames.filter(annotationName => regex.test(annotationName.name));
    }

    getAnnotationIdentifierSuggestionValue(suggestion) {
        return suggestion.name;
    }

    renderAnnotationIdentifierSuggestion(suggestion) {
        return (
            <span>{suggestion.name}</span>
        );
    }

    //// End of package name autosuggest dropdown funcs.

    componentDidUpdate() {
        this.currentInput && this.currentInput.focus();
    }
}

AnnotationContainer.contextTypes = {
    // Used for accessing ast-root to add imports
    renderingContext: PropTypes.instanceOf(Object).isRequired
};

export default AnnotationContainer;