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

/**
 * React component for the name of an annotation attachment.
 *
 * @class AnnotationAttachmentName
 * @extends {React.Component}
 */
class AnnotationAttachmentName extends React.Component {

    /**
     * Creates an instance of AnnotationAttachmentName.
     * @param {Object} props React properties
     * @memberof AnnotationAttachmentName
     */
    constructor(props) {
        super(props);

        this.onNameBlur = this.onNameBlur.bind(this);
        this.onNameSelected = this.onNameSelected.bind(this);
    }

    /**
     * Event for on name blur.
     *
     * @memberof AnnotationAttachmentName
     */
    onNameBlur() {
        this.props.isEditStateFunc(false);
    }

    /**
     * Event for when a name is selected.
     *
     * @param {Object} event The actual event.
     * @param {string} suggestionValue The selected value.
     * @memberof AnnotationAttachmentName
     */
    onNameSelected(event, { suggestionValue }) {
        if (this.props.model.getName() !== suggestionValue) {
            this.props.model.setName(suggestionValue);

            // Removing the children of the annotation.
            this.props.model.getChildren().forEach((attribute) => {
                this.props.model.removeChild(attribute);
            });

            this.props.isEditStateFunc(false);
        }
    }

    /**
     * Renders the view for an annotation
     *
     * @returns {ReactElement} JSX of the annotation component.
     * @memberof AnnotationAttachmentName
     */
    render() {
        if (this.props.isInEdit && !this.props.model.getViewState().disableEdit) {
            const model = this.props.model;
            return (<AutoSuggestHtml
                items={this.props.supportedNames}
                placeholder={'Annotation Name'}
                initialValue={model.getName() || ''}
                onSuggestionSelected={this.onNameSelected}
                onBlur={this.onNameBlur}
                minWidth={80}
                maxWidth={1000}
            />);
        }

        return (<span>{this.props.model.getName()}</span>);
    }
}

AnnotationAttachmentName.propTypes = {
    model: PropTypes.instanceOf(AnnotationAttachment).isRequired,
    supportedNames: PropTypes.arrayOf(PropTypes.string),
    isInEdit: PropTypes.bool,
    isEditStateFunc: PropTypes.func.isRequired,
};

AnnotationAttachmentName.defaultProps = {
    supportedNames: [],
    isInEdit: false,
};

export default AnnotationAttachmentName;
