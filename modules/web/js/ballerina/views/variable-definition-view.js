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
import $ from 'jquery';
import Alerts from 'alerts';
import BallerinaView from './ballerina-view';

/**
 * Arguments for creating a constant definition view.
 * @class VariableDefinitionView
 * @extends BallerinaView
 */
class VariableDefinitionView extends BallerinaView {

    /**
     * Constructor for VariableDefinitionView
     * @param args - See docs of {@link BallerinaView}.
     * @constructor
     */
    constructor(args) {
        super(args);
        this._variableDefinitionWrapper = undefined;
        this._deleteButton = undefined;
    }

    /**
     * @inheritDoc
     * Implements the view for a constant definition.
     */
    render(diagramRenderContext) {
        this.setDiagramRenderingContext(diagramRenderContext);

        var self = this;

        var variableDefinitionWrapper = $('<div/>', {
            id: this.getModel().getID(),
            class: 'variable-wrapper variable-wrapper-message'
        }).data('model', this.getModel()).appendTo(this.getContainer());

        this._variableDefinitionWrapper = variableDefinitionWrapper;

        var variableDefinitionTypeWrapper = $('<div/>', {
            text: this.getModel().getBType().trim(),
            'contenteditable': true,
            class: 'variable-type variable-type-message'
        }).keyup(function() {
            try {
                self.getModel().setStatementString($(this).text().trim() + ' ' + $(variableDefintionStatementWrapper).text().trim());
            } catch (error) {
                Alerts.error(error);
            }
        }).appendTo(variableDefinitionWrapper);

        var variableDefintionStatementWrapper = $('<span/>', {
            text: this.getModel().getStatementString().replace(this.getModel().getBType().trim(), '').trim(),
            'contenteditable': true,
            class: 'variable-identifier variable-identifier-message'
        }).keyup(function() {
            try {
                self.getModel().setStatementString($(variableDefinitionTypeWrapper).text().trim() + ' ' + $(this).text().trim());
            } catch (error) {
                Alerts.error(error);
            }
        }).appendTo(variableDefinitionWrapper);

        // Creating delete button.
        var deleteButton = $('<i class=\'fw fw-cancel\'></i>').appendTo(variableDefinitionWrapper);

        this._deleteButton = deleteButton.get(0);

        // Removes the value of the argument in the model and rebind the arguments to the arguments view.
        // $(deleteButton).click(function () {
        //     $(variableDefinitionWrapper).remove();
        //     self.getParent().removeVariableDefinitionStatement(self.getModel().getID());
        // });
    }

    getVariableDefinitionWrapper() {
        return this._variableDefinitionWrapper;
    }

    getDeleteButton() {
        return this._deleteButton;
    }

    removeVariableDefinition() {
        $(this.getVariableDefinitionWrapper()).remove();
        this.getParent().removeVariableDefinitionStatement(this.getModel().getID());
    }
}

export default VariableDefinitionView;
    