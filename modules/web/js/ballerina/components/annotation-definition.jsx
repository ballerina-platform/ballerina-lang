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
import PanelDecorator from './panel-decorator';
import {getComponentForNodeArray} from './utils';
import AnnotationAttributeDecorator from './annotation-attribute-decorator';
import AnnotationDefinitionAttachment from './annotation-definition-attachment';
import TagController from './utils/tag-component';
import Alerts from 'alerts';
import _ from 'lodash';
import PropTypes from 'prop-types';

class AnnotationDefinition extends React.Component {

    constructor() {
        super();
        this.onAttachmentDelete = this.onAttachmentDelete.bind(this);
        this.addAttachmentPoint = this.addAttachmentPoint.bind(this);
    }

    /**
     * Render view for annotation definition
     * */
    render() {
        let model = this.props.model;
        let viewState = model.viewState;
        let bBox = model.viewState.bBox;
        let title = model.getAnnotationName();
        let children = getComponentForNodeArray(model.getChildren());
        let attachmentPoints = this.props.model.getAttachmentPoints();
        let attachments = [];

        // Create the annotationDefinitionAttachment components for attachment points.
        for (let i = 0; i < attachmentPoints.length; i++) {
            let attachmentValue = attachmentPoints[i];
            attachments.push(React.createElement(AnnotationDefinitionAttachment, {
                model: model,
                key: i,
                viewState: viewState.attachments[attachmentValue].viewState,
                attachmentValue: attachmentValue,
                onDelete: this.onAttachmentDelete
            }, null));
        }

        let componentData = {
            components: {
                openingBracket: this.props.model.getViewState().components.openingParameter,
                closingBracket: this.props.model.getViewState().components.closingParameter
            },
            prefixView: this.props.model.getViewState().components.parametersPrefixContainer,
            openingBracketClassName: 'parameter-bracket-text',
            closingBracketClassName: 'parameter-bracket-text',
            prefixTextClassName: 'parameter-prefix-text'
        };

        let tagController = (<TagController key={model.getID()} model={model} setter={this.addAttachmentPoint}
                                            validateInput={this.validateInput} modelComponents={attachments}
                                            componentData={componentData} isSelectBox={true}
                                            suggestions={this.getAnnotationAttachmentPointsForSuggestions()}
                                            groupClass="annotation-attachment-group" label={"attach"}/>);
        let titleComponentData = [{
            isNode: false,
            model: tagController
        }];

        return (<PanelDecorator icon="annotation-black"
                                title={title}
                                bBox={bBox}
                                model={model}
                                titleComponentData={titleComponentData}>
            <AnnotationAttributeDecorator model={model} bBox={bBox}/>
            {children}
        </PanelDecorator>);
    }

    /**
     * Add Attachment point to annotation definition.
     * @param {string} attachment - Attachment to be added to the annotation definition
     * @return {boolean} true - if add successful, false - if add unsuccessful.
     * */
    addAttachmentPoint(attachment) {
        let model = this.props.model;
        try {
            if (this.validateType(attachment)) {
                model.addAnnotationAttachmentPoint(attachment);
            } else {
                let errorString = "Incorrect Annotation Attachment Type: " + attachment;
                Alerts.error(errorString);
                return false;
            }
        } catch (e) {
            Alerts.error(e);
            return false;
        }
        return true;
    }

    /**
     * Validate input from tag controller and apply condition to tell whether to change he state.
     * @param {string} input - input from tag controller.
     * @return {boolean} true - change the state, false - don't change the state.
     * */
    validateInput(input) {
        let splittedExpression = input.split(" ");
        return splittedExpression.length === 1;
    }

    /**
     * Get types of ballerina to which can be applied when declaring variables.
     * @return {[string]} Attachment types
     * */
    getTypeDropdownValues() {
        const {renderingContext} = this.context;
        return renderingContext.environment.getAnnotationAttachmentTypes();
    }

    getAnnotationAttachmentPointsForSuggestions() {
        const {renderingContext} = this.context;
        let suggestions = [];
        for (let i = 0; i < renderingContext.environment.getAnnotationAttachmentTypes().length; i++) {
            let suggestion = {
                name: renderingContext.environment.getAnnotationAttachmentTypes()[i]
            };
            suggestions.push(suggestion);
        }
        return suggestions;
    }

    /**
     * Validate annotation attachment type.
     * @param {string} bType
     * @return {boolean} isValid
     * */
    validateType(bType) {
        let isValid = false;
        let typeList = this.getTypeDropdownValues();
        let filteredTypeList = _.filter(typeList, function (type) {
            return type === bType;
        });
        if (filteredTypeList.length > 0) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * Delete attachment tag event handler.
     * @param {string} attachment - attachment to be deleted
     * */
    onAttachmentDelete(attachment) {
        let model = this.props.model;
        delete model.getViewState().attachments[attachment];
        model.removeAnnotationAttachmentPoints(attachment);
    }
}

AnnotationDefinition.contextTypes = {
    renderingContext: PropTypes.instanceOf(Object).isRequired
};

export default AnnotationDefinition;