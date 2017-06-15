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
import ASTFactory from './../ast/ballerina-ast-factory';
import Annotation from './annotation';
import AnnoationEntry from './../ast/annotations/annotation-entry';
import AutoSuggestHtml from './utils/autosuggest-html';
import BallerinaEnvironment from '../env/environment';
import { util } from './../visitors/sizing-utils';

/**
 * React component for an annotation entry({@link AnnotationEntry}).
 *
 * @class AnnotationAttribute
 * @extends {React.Component}
 */
class AnnotationAttribute extends React.Component {

    /**
     * Creates an instance of AnnotationAttribute.
     * @param {any} props React properties for the component
     *
     * @memberof AnnotationAttribute
     */
    constructor(props) {
        super(props);

        let rightValueLength = 0;
        if (_.isString(props.model.getRightValue())) {
            rightValueLength = util.getTextWidth(props.model.getRightValue(), 150, 1000).w + 10;
        }

        if (!_.isUndefined(props.model.getLeftValue()) || props.model.getLeftValue() === '') {
            this.state = {
                isLeftValueInEdit: props.model.getLeftValue() === '',
                isRightValueInEdit: props.model.getRightValue() === '' || props.model.getRightValue() === '""',
                leftValue: props.model.getLeftValue(),
                rightValue: props.model.getRightValue(),
                rightValueLength,
                setRightValueFocus: false,
            };
        } else {
            this.state = {
                isRightValueInEdit: props.model.getRightValue() === '' || props.model.getRightValue() === '""',
                rightValue: props.model.getRightValue(),
                rightValueLength,
                setRightValueFocus: false,
            };
        }

        // Binding events.
        this.deleteAttribute = this.deleteAttribute.bind(this);
        this.onLeftValueSelected = this.onLeftValueSelected.bind(this);
        this.onLeftValueClick = this.onLeftValueClick.bind(this);
        this.addAnnotationEntry = this.addAnnotationEntry.bind(this);
        this.onRightValueClick = this.onRightValueClick.bind(this);
        this.onRightValueChange = this.onRightValueChange.bind(this);
    }

    /**
     * Focusing on the right side value if this.state.setRightValueFocus is true.
     *
     *
     * @memberof AnnotationAttribute
     */
    componentDidMount() {
        if (this.state.setRightValueFocus) {
            this.rightValueInput.focus();
            this.onUpdate(() => {
                this.setState({
                    setRightValueFocus: false,
                });
            });
        }
    }

    /**
     * Focusing on the right side value if this.state.setRightValueFocus is true.
     *
     * @memberof AnnotationAttribute
     */
    componentDidUpdate() {
        if (this.state.setRightValueFocus && this.rightValueInput) {
            this.rightValueInput.focus();
            this.onUpdate(() => {
                this.setState({
                    setRightValueFocus: false,
                });
            });
        }
    }

    /**
     * Event when the left side value is changed
     *
     * @param {any} event The actual event.
     *
     * @memberof AnnotationAttribute
     */
    onLeftValueChange(event) {
        this.setState({
            leftValue: event.target.value,
        });
    }

    /**
     * Event when the right side value is changed,
     *
     * @param {any} event The actual event.
     *
     * @memberof AnnotationAttribute
     */
    onRightValueChange(event) {
        this.setState({
            rightValue: event.target.value,
            rightValueLength: util.getTextWidth(event.target.value, 150, 1000).w + 10,

        });
        this.props.model.setRightValue(event.target.value, { doSilently: true });
    }

    /**
     * Event for editing a left side value.
     *
     * @param {any} e The actual event.
     *
     * @memberof AnnotationAttribute
     */
    onLeftValueClick(e) {
        this.setState({
            isLeftValueInEdit: true,
        });
        e.stopPropagation();
    }

    /**
     * Event for editing a right side value
     *
     * @param {any} e The actual event
     *
     * @memberof AnnotationAttribute
     */
    onRightValueClick(e) {
        this.setState({
            isRightValueInEdit: true,
            setRightValueFocus: true,
        });
        e.stopPropagation();
    }

    /**
     * Event for when left side value is selected.
     *
     * @param {any} event The actual event
     * @param {any} { suggestionValue } The selected value.
     *
     * @memberof AnnotationAttribute
     */
    onLeftValueSelected(event, { suggestionValue }) {
        if (suggestionValue !== this.props.model.getLeftValue()) {
            this.props.model.setLeftValue(suggestionValue);
            const selectedAnnotationAttribute = this.props.annotationAttributes.filter(annotationAttribute =>
                                                            annotationAttribute.getIdentifier() === suggestionValue)[0];

            const valueType = selectedAnnotationAttribute.getBType();
            if (BallerinaEnvironment.getTypes().includes(valueType) && !selectedAnnotationAttribute.isArrayType()) {
                if (valueType === 'string') {
                    this.props.model.setRightValue('""', { doSilently: true });
                } else {
                    this.props.model.setRightValue('', { doSilently: true });
                }
            } else if (selectedAnnotationAttribute.isArrayType()) {
                this.props.model.setRightValue(ASTFactory.createAnnotationEntryArray(), { doSilently: true });
            } else {
                let annotationDefinition;
                for (const packageDefEnv of BallerinaEnvironment.getPackages()) {
                    if (packageDefEnv.getName() === selectedAnnotationAttribute.getPackagePath()) {
                        for (const annotationDef of packageDefEnv.getAnnotationDefinitions()) {
                            if (annotationDef.getName() === selectedAnnotationAttribute.getBType()) {
                                annotationDefinition = annotationDef;
                            }
                        }
                    }
                }

                let newAnnotation;
                if (annotationDefinition) {
                    newAnnotation = ASTFactory.createAnnotation({
                        fullPackageName: annotationDefinition.getPackagePath(),
                        packageName: annotationDefinition.getPackagePath().split('.').pop(),
                        identifier: annotationDefinition.getName(),
                    });
                } else {
                    newAnnotation = ASTFactory.createAnnotation({ packageName: undefined, identifier: '' });
                }

                this.props.model.setRightValue(newAnnotation, { doSilently: true });
            }
            this.setState({
                isRightValueInEdit: true,
                leftValue: suggestionValue,
                setRightValueFocus: true,
            });
        }

        this.setState({
            isLeftValueInEdit: false,
        });
    }

    /**
     * Remove icon click event for removing child from a {@link AnnotationEntryArray}.
     *
     * @param {object} model
     *
     * @memberof AnnotationAttribute
     */
    onArrayEntryRemoveIcon(model) {
        model.getParent().removeChild(model);
        this.setState(this.state);
    }

    /**
     * Gets an array of annotation attribute definitions.
     *
     * @param {any} fullPackageName The complete package name.
     * @param {any} identifier The identifier of the attribute.
     * @returns An array of {@link AnnotationAttributeDefinition}
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
     * Gets an array of annotation attribute definitions.
     *
     * @param {any} annotationAST The annotation AST
     * @param {any} annotationAttributeIdentifier The identifier of the annotation AST.
     * @returns An array of {@link AnnotationAttributeDefinition}
     *
     * @memberof AnnotationAttribute
     */
    getAnnotationAttributesByAST(annotationAST, annotationAttributeIdentifier) {
        let packageNameOfAttribute;
        let annotationDefinitionIdentifier;
        for (const packageDefintion of BallerinaEnvironment.getPackages()) {
            if (packageDefintion.getName() === annotationAST.getFullPackageName()) {
                for (const annotationDefinition of packageDefintion.getAnnotationDefinitions()) {
                    if (annotationDefinition.getName() === annotationAST.getIdentifier()) {
                        for (const annotationAttribute of annotationDefinition.getAnnotationAttributeDefinitions()) {
                            if (annotationAttribute.getName() === annotationAttributeIdentifier) {
                                packageNameOfAttribute = packageDefintion.getName();
                                annotationDefinitionIdentifier = annotationDefinition.getName();
                            }
                        }
                    }
                }
            }
        }

        return this.getAnnotationAttributes(packageNameOfAttribute, annotationDefinitionIdentifier);
    }

    /**
     * Getting the list of values for the left side value(key).
     *
     * @returns An array of string as suggestions.
     *
     * @memberof AnnotationAttribute
     */
    getLeftValuesForSuggestions() {
        let leftValueSuggestions = this.props.annotationAttributes.map(annotationAttribute =>
                                                                                annotationAttribute.getIdentifier());

        if (ASTFactory.isAnnotation(this.props.model.getParent())) {
            for (const annotationEntry of this.props.model.getParent().getChildren()) {
                leftValueSuggestions = leftValueSuggestions.filter(suggestion =>
                    suggestion !== annotationEntry.getLeftValue() || suggestion === this.props.model.getLeftValue());
            }
        }

        return leftValueSuggestions;
    }

    /**
     * Event for adding an AST for an arrayed type right value.
     *
     *
     * @memberof AnnotationAttribute
     */
    addAnnotationEntry() {
        const selectedAnnotationAttribute = this.props.annotationAttributes.filter(annotationAttribute =>
                                            annotationAttribute.getIdentifier() === this.props.model.getLeftValue())[0];

        const valueType = selectedAnnotationAttribute.getBType();
        const annotationEntryArray = this.props.model.getRightValue();
        if (BallerinaEnvironment.getTypes().includes(valueType)) {
            if (valueType === 'string') {
                annotationEntryArray.addChild(ASTFactory.createAnnotationEntry({ leftValue: '', rightValue: '""' }));
            } else {
                annotationEntryArray.addChild(ASTFactory.createAnnotationEntry({ leftValue: '', rightValue: '' }));
            }
        } else {
            const newAnnotation = ASTFactory.createAnnotation({
                fullPackageName: selectedAnnotationAttribute.getPackagePath(),
                packageName: selectedAnnotationAttribute.getPackagePath().split('.').pop(),
                identifier: valueType,
            });

            const newAnnotationEntry = ASTFactory.createAnnotationEntry({ leftValue: '', rightValue: newAnnotation });
            annotationEntryArray.addChild(newAnnotationEntry);
        }

        this.setState(this.state);
    }

    /**
     * Event for deleting/removing an attribute.
     *
     *
     * @memberof AnnotationAttribute
     */
    deleteAttribute() {
        this.props.model.parent.removeChild(this.props.model);
        this.setState(this.state);
    }

    /**
     * Rendering the component.
     *
     * @returns JSX markup for annotation entry.
     *
     * @memberof AnnotationAttribute
     */
    render() {
        const model = this.props.model;

        const removeIcon = (<div className="annotation-attribute-remove" onClick={this.deleteAttribute}>
            <i className="fw fw-cancel" />
        </div>);

        // Creating the view for the left side value of the annotation entry.
        let key = (null);
        if (!ASTFactory.isAnnotationEntryArray(model.getParent())) {
            if (this.state.isLeftValueInEdit) {
                key = (<td className="annotation-attribute-key">
                    <AutoSuggestHtml
                        items={this.getLeftValuesForSuggestions()}
                        placeholder={'Identifier'}
                        initialValue={this.state.leftValue}
                        onSuggestionSelected={this.onLeftValueSelected}
                    />
                    <span className="annotation-attribute-separator">:</span>
                </td>);
            } else {
                key = (<td
                    className="annotation-attribute-key"
                    onClick={this.onLeftValueClick}
                >{this.state.leftValue}<span className="annotation-attribute-separator">:</span></td>);
            }
        }

        // Creating the view for the right side value of the annotation entry.
        let value;
        if (ASTFactory.isAnnotationEntryArray(model.getRightValue())) {
            const addIcon = (<div className="annotation-attribute-add" onClick={this.addAnnotationEntry}>
                <i className="fw fw-add" />
            </div>);
            // The model is an AnnotationEntryArray
            if (model.getRightValue().getChildren().length > 0) {
                const annotationEntries = model.getRightValue().getChildren();
                const annotationEntryComponents = [];

                let annotationAttributes = [];
                if (ASTFactory.isAnnotation(model.getParent().getParent())) {
                    annotationAttributes = this.getAnnotationAttributesByAST(model.getParent().getParent(),
                                                                                    model.getParent().getLeftValue());
                }

                for (const annotationEntry of annotationEntries) {
                    const arrayEntryRemoveIcon = (<div
                        className="annotation-array-entry-remove"
                        onClick={this.onArrayEntryRemoveIcon.bind(this, annotationEntry)}
                    >
                        <i className="fw fw-cancel" />
                    </div>);
                    annotationEntryComponents.push(<tr key={`annotation-array-entry-row-${annotationEntry.getID()}`}>
                        <td>
                            <table className="annotation-array-entry">
                                <tbody>
                                    <AnnotationAttribute
                                        model={annotationEntry}
                                        annotationAttributes={annotationAttributes}
                                        removeIcon={arrayEntryRemoveIcon}
                                    />
                                </tbody>
                            </table>
                        </td>
                    </tr>);
                }

                // Deciding to have comma except for the last child(The last child of the AnnotationEntryArray)
                let haveEndingComma = false;
                if (ASTFactory.isAnnotation(model.getParent())) {
                    const lengthSameLevelChildren = model.getParent().getChildren().length;
                    const indexOfCurrentNode = model.getParent().getIndexOfChild(model);
                    if (indexOfCurrentNode !== lengthSameLevelChildren - 1) {
                        haveEndingComma = true;
                    }
                }

                if (!haveEndingComma) {
                    value = (<td className="annotation-attribute-value">
                        <table>
                            <tbody>
                                <tr>
                                    <td>[{addIcon}{removeIcon}</td>
                                </tr>
                                {annotationEntryComponents}
                                <tr>
                                    <td>]</td>
                                </tr>
                            </tbody>
                        </table>
                    </td>);
                } else {
                    value = (<td className="annotation-attribute-value">
                        <table>
                            <tbody>
                                <tr>
                                    <td>[{addIcon}{removeIcon}</td>
                                </tr>
                                {annotationEntryComponents}
                                <tr>
                                    <td>] ,</td>
                                </tr>
                            </tbody>
                        </table>
                    </td>);
                }
            } else {
                value = (<td className="annotation-attribute-value">
                    [] {addIcon}{removeIcon}
                </td>);
            }
            return <tr>{key}{value}</tr>;
        } else if (ASTFactory.isAnnotation(model.getRightValue())) {
            let haveEndingComma = false;
            if (ASTFactory.isAnnotationEntryArray(model.getParent())) {
                const lengthSameLevelChildren = model.getParent().getChildren().length;
                const indexOfCurrentNode = model.getParent().getIndexOfChild(model);
                if (indexOfCurrentNode !== lengthSameLevelChildren - 1) {
                    haveEndingComma = true;
                }
            }

            value = (<td className="annotation-attribute-value">
                <Annotation model={model.getRightValue()} haveEndingComma={haveEndingComma} removeIcon={removeIcon} />
            </td>);
            return <tr>{key}{value}</tr>;
        }
        let endingComma = '';
        if (ASTFactory.isAnnotationEntryArray(model.getParent()) || ASTFactory.isAnnotation(model.getParent())) {
            const lengthSameLevelChildren = model.getParent().getChildren().length;
            const indexOfCurrentNode = model.getParent().getIndexOfChild(model);
            if (indexOfCurrentNode !== lengthSameLevelChildren - 1) {
                endingComma = ' ,';
            }
        }

        if (this.state.isRightValueInEdit) {
            value = (<td className="annotation-attribute-value" onClick={this.onRightValueClick}>
                <input
                    type="text"
                    placeholder="value"
                    value={this.state.rightValue}
                    onChange={this.onRightValueChange}
                    onBlur={() => { this.setState({ isRightValueInEdit: false }); }}
                    ref={(input) => { this.rightValueInput = input; }}
                    style={{ width: this.state.rightValueLength }}
                />
                {endingComma}{removeIcon}
            </td>);
        } else {
            value = (<td className="annotation-attribute-value"onClick={this.onRightValueClick}>
                {this.state.rightValue}{endingComma}{removeIcon}</td>);
        }

        return <tr>{key}{value}</tr>;
    }

}

AnnotationAttribute.propTypes = {
    model: PropTypes.instanceOf(AnnoationEntry).isRequired,
    annotationAttributes: PropTypes.array,
};

export default AnnotationAttribute;
