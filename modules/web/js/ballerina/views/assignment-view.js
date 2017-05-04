/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import AssignmentStatement from './../ast/assignment';
import alerts from 'alerts';

/**
 * The view to represent a assignment definition which is an AST visitor.
 * @class AssignmentStatementView
 * @extends SimpleStatementView
 */
class AssignmentStatementView extends SimpleStatementView {

    /**
     * @param {Object} args - Arguments for creating the view.
     * @param {Assignment} args.model - The assignment statement model.
     * @param {Object} args.container - The HTML container to which the view should be added to.
     * @param {Object} [args.viewOptions={}] - Configuration values for the view.
     * @constructor
     */
    constructor(args) {
        super(args);

        if (_.isNil(this._container)) {
            log.error('Container for Assignment statement is undefined.' + this._container);
            throw 'Container for Assignment statement is undefined.' + this._container;
        }
    }

    canVisitStatement() {
        return true;
    }

    setModel(model) {
        if (!_.isNil(model) && model instanceof AssignmentStatement) {
            (this.__proto__.__proto__).setModel(model);
        } else {
            log.error('Assignment statement undefined or is of different type.' + model);
            throw 'Assignment statement undefined or is of different type.' + model;
        }
    }

    /**
     * Renders the view for assignment statement.
     * @returns {group} - The SVG group which holds the elements of the assignment statement.
     */
    render(diagramRenderingContext) {
        // Calling super class's render function.
        (this.__proto__.__proto__).render.call(this, diagramRenderingContext);
        // Update model.
        var model = this.getModel();
        model.accept(this);
        // Setting display text.
        this.renderDisplayText(model.getStatementString());

        var statementGroup = this.getStatementGroup();
        // Creating property pane.
        var editableProperty = {
            propertyType: 'text',
            key: 'Assignment',
            model: model,
            getterMethod: model.getStatementString,
            setterMethod: model.setStatementString
        };
        this._createPropertyPane({
            model: model,
            statementGroup: statementGroup,
            editableProperties: editableProperty
        });

        this.listenTo(model, 'update-property-text', this.updateStatementText);
        this.listenTo(model, 'focus-out', this.validateNode);

        this._createDebugIndicator({
            statementGroup: statementGroup
        });

        return statementGroup;
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
            if (!this.getModel().getFactory().isAssignmentStatement(parsedNode)) {
                this.getSvgRect().classed('statement-rect', false).classed('statement-rect-error', true);
                alerts.error('Node type expected to be Assignment Statement');
            } else {
                this.getSvgRect().classed('statement-rect', true).classed('statement-rect-error', false);
            }
        } else {
            alerts.error(response);
        }
    }

    updateStatementText(newStatementText) {
        this._model.setStatementString(newStatementText);
        var displayText = this._model.getStatementString();
        this.renderDisplayText(displayText);
    }
}

export default AssignmentStatementView;
