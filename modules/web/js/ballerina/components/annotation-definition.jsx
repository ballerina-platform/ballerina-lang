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
import { getComponentForNodeArray } from './utils';
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
        const model = this.props.model;
        const viewState = model.viewState;
        const bBox = model.viewState.bBox;
        const title = model.getAnnotationName();
        const children = getComponentForNodeArray(model.getChildren());
        const attachmentPoints = this.props.model.getAttachmentPoints();
        const attachments = [];

        // Create the annotationDefinitionAttachment components for attachment points.
        for (let i = 0; i < attachmentPoints.length; i++) {
            const attachmentValue = attachmentPoints[i];
            attachments.push(React.createElement(AnnotationDefinitionAttachment, {
                model,
                key: i,
                viewState: viewState.attachments[attachmentValue].viewState,
                attachmentValue,
                onDelete: this.onAttachmentDelete,
            }, null));
        }

        const componentData = {
            components: {
                openingBracket: this.props.model.getViewState().components.openingParameter,
                closingBracket: this.props.model.getViewState().components.closingParameter,
            },
            prefixView: this.props.model.getViewState().components.parametersPrefixContainer,
            openingBracketClassName: 'parameter-bracket-text',
            closingBracketClassName: 'parameter-bracket-text',
            prefixTextClassName: 'parameter-prefix-text',
            defaultText: '+ Add Attachment',
        };

        const tagController = (<TagController
            key={model.getID()} model={model} setter={this.addAttachmentPoint}
            validateInput={this.validateInput} modelComponents={attachments}
            componentData={componentData} isSelectBox
            suggestions={this.getAnnotationAttachmentPointsForSuggestions()}
            groupClass="annotation-attachment-group" label={'attach'}
        />);
        const titleComponentData = [{
            isNode: false,
            model: tagController,
        }];

        return (<PanelDecorator
            icon="annotation-black"
            title={title}
            bBox={bBox}
            model={model}
            titleComponentData={titleComponentData}
        >
            <AnnotationAttributeDecorator model={model} bBox={bBox} />
            {children}
        </PanelDecorator>);
    }

    /**
     * Add Attachment point to annotation definition.
     * @param {string} attachment - Attachment to be added to the annotation definition
     * @return {boolean} true - if add successful, false - if add unsuccessful.
     * */
    addAttachmentPoint(attachment) {
        const model = this.props.model;
        try {
            if (this.validateType(attachment)) {
                model.addAnnotationAttachmentPoint(attachment);
            } else {
                const errorString = `Incorrect Annotation Attachment Type: ${attachment}`;
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
        const splittedExpression = input.split(' ');
        return splittedExpression.length === 1;
    }

    /**
     * Get types of ballerina to which can be applied when declaring variables.
     * @return {[string]} Attachment types
     * */
    getTypeDropdownValues() {
        const { environment } = this.context;
        return environment.getAnnotationAttachmentTypes();
    }

    getAnnotationAttachmentPointsForSuggestions() {
        const { environment } = this.context;
        const suggestions = [];
        for (let i = 0; i < environment.getAnnotationAttachmentTypes().length; i++) {
            const suggestion = {
                name: environment.getAnnotationAttachmentTypes()[i],
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
        const typeList = this.getTypeDropdownValues();
        const filteredTypeList = _.filter(typeList, type => type === bType);
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
        const model = this.props.model;
        delete model.getViewState().attachments[attachment];
        model.removeAnnotationAttachmentPoints(attachment);
    }
}

AnnotationDefinition.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
};

export default AnnotationDefinition;
