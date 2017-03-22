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
import CompoundStatementView from './compound-statement-view';
import TryCatchStatement from '../ast/statements/trycatch-statement';
import CatchStatement from '../ast/statements/catch-statement';
import Point from './point';

/**
 * The view to represent a Try Catch statement which is an AST visitor.
 * @param {Object} args - Arguments for creating the view.
 * @param {TryCatchStatement} args.model - The Try Catch statement model.
 * @param {Object} args.container - The HTML container to which the view should be added to.
 * @param {Object} args.parent - Parent View (Resource, Worker, etc)
 * @param {Object} [args.viewOptions={}] - Configuration values for the view.
 * @class TryCatchStatementView
 * @constructor
 * @extends CompoundStatementView
 */
class TryCatchStatementView extends CompoundStatementView {
    constructor(args) {
        super(args);

        this._tryBlockView = undefined;
        this._catchBlockView = undefined;
        this.getModel()._isChildOfWorker = args.isChildOfWorker;

        if (_.isNil(this._model) || !(this._model instanceof TryCatchStatement)) {
            log.error("Try Catch statement definition is undefined or is of different type." + this._model);
            throw "Try Catch statement definition is undefined or is of different type." + this._model;
        }

        if (_.isNil(this._container)) {
            log.error("Container for Try Catch statement is undefined." + this._container);
            throw "Container for Try Catch statement is undefined." + this._container;
        }
    }

    canVisitTryCatchStatement() {
        return true;
    }

    /**
     * Visit Try Statement
     * @param {TryStatement} statement
     */
    visitTryStatement(statement) {
        this._tryBlockView = this.visitChildStatement(statement);
    }

    /**
     * Visit Catch Statement
     * @param {CatchStatement} statement
     */
    visitCatchStatement(statement) {
        this._catchBlockView = this.visitChildStatement(statement);
    }

    render(diagramRenderingContext) {
        // Calling super render.
        (this.__proto__.__proto__).render.call(this, diagramRenderingContext);

        // Creating property pane
        var model = this.getModel();
        var editableProperty = {};
        _.forEach(model.getChildren(), function (childStatement, index) {
            if (childStatement instanceof CatchStatement) {
                editableProperty = {
                    propertyType: "text",
                    key: "Catch parameter",
                    model: childStatement,
                    getterMethod: childStatement.getParameter
                };
                return false;
            }
        });
        this._createPropertyPane({
                                     model: model,
                                     statementGroup: this.getStatementGroup(),
                                     editableProperties: editableProperty
                                 });
        this._createDebugIndicator({
                                       statementGroup: this.getStatementGroup()
                                   });
    }

    /**
     * Set the TryCatchStatement model
     * @param {TryCatchStatement} model
     */
    setModel(model) {
        if (!_.isNil(model) && model instanceof TryCatchStatement) {
            (this.__proto__.__proto__).setModel(model);
        } else {
            log.error("Try Catch statement definition is undefined or is of different type." + model);
            throw "Try Catch statement definition is undefined or is of different type." + model;
        }
    }

    getTryBlockView() {
        return this._tryBlockView;
    }

    getCatchBlockView() {
        return this._catchBlockView;
    }

    onBeforeModelRemove() {
        this.removeAllChildStatements();
    }

    _getEditPaneLocation() {
        var statementBoundingBox = this.getCatchBlockView().getBoundingBox();
        return new Point((statementBoundingBox.x()), (statementBoundingBox.y() - 1));
    }
}

export default TryCatchStatementView;
