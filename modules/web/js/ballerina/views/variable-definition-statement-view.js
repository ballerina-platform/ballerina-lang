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
import _ from 'lodash';
import log from 'log';
import SimpleStatementView from './simple-statement-view';
import VariableDefinitionStatement from '../ast/statements/variable-definition-statement';
import alerts from 'alerts';

/**
 * The view to represent a assignment definition which is an AST visitor.
 * @class VariableDefinitionStatementView
 * @extends SimpleStatementView
 */
class VariableDefinitionStatementView extends SimpleStatementView {

    /**
     * Constructor for VariableDefinitionStatementView
     * @param {Object} args - Arguments for creating the view.
     * @param {VariableDefinitionStatement} args.model - The variable definition statement model.
     * @param {Object} args.container - The HTML container to which the view should be added to.
     * @param {Object} [args.viewOptions={}] - Configuration values for the view.
     * @constructor
     */
    constructor(args) {
        super(args);

        if (_.isNil(this._container)) {
            log.error('Container for Variable Definition statement is undefined.' + this._container);
            throw 'Container for Variable Definition statement is undefined.' + this._container;
        }
    }

    canVisitStatement() {
        return true;
    }

    setModel(model) {
        if (!_.isNil(model) && model instanceof VariableDefinitionStatement) {
            (this.__proto__.__proto__).setModel(model);
        } else {
            log.error('Variable Definition statement undefined or is of different type.' + model);
            throw 'Variable Definition statement undefined or is of different type.' + model;
        }
    }

    /**
     * Renders the view for Variable Definition statement.
     * @returns {group} - The SVG group which holds the elements of the Variable Definition statement.
     */
    render(renderingContext) {
        // Calling super class's render function.
        (this.__proto__.__proto__).render.call(this, renderingContext);
        // Setting display text.
        var model = this.getModel();
        this.renderDisplayText(model.getStatementString());

        model.accept(this);

        // Creating property pane
        var editableProperties = [];
        var editableProperty = {
            propertyType: 'text',
            key: 'VariableDefinition',
            model: model,
            getterMethod: model.getStatementString,
            setterMethod: model.setStatementString
        };
        editableProperties.push(editableProperty);
        this._createPropertyPane({
            model: model,
            statementGroup:this.getStatementGroup(),
            editableProperties: editableProperty
        });
        this._createDebugIndicator({
            statementGroup: this.getStatementGroup()
        });
        this.listenTo(model, 'update-property-text', this.updateStatementText);
        this.listenTo(model, 'focus-out', this.validateNode);
    }

    updateStatementText(updatedText) {
        if (!_.isUndefined(updatedText) && updatedText !== '') {
            // Updating variable definition statement model.
            var model = this.getModel();
            model.setStatementString(updatedText);
            this.renderDisplayText(model.getStatementString());// Set display text.
        }
    }

    /**
     * Validate the node type on focus out of the statement's expression editor
     * @override
     */
    validateNode() {
        var ballerinaFileEditor = this._diagramRenderingContext.ballerinaFileEditor;
        var generatedSource = ballerinaFileEditor.generateSource();
        var response = ballerinaFileEditor.getModelFromSource(generatedSource);
        var pathVector = [];
        if (this.getModel().getFactory().isBallerinaAstRoot(response)) {
            ballerinaFileEditor.getPathToNode(this.getModel(), pathVector);
            var parsedNode = ballerinaFileEditor.getNodeByVector(response, pathVector);
            if (!this.getModel().getFactory().isVariableDefinitionStatement(parsedNode)) {
                this.getSvgRect().classed('statement-rect', false).classed('statement-rect-error', true);
                alerts.error('Node type expected to be Variable Definition Statement');
            } else {
                this.getSvgRect().classed('statement-rect', true).classed('statement-rect-error', false);
            }
        } else {
            alerts.error(response);
        }
    }
}

export default VariableDefinitionStatementView;
    
