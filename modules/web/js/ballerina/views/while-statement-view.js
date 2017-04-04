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
import BlockStatementView from './block-statement-view';
import WhileStatement from '../ast/statements/while-statement';
import * as d3 from 'd3';

/**
 * The view to represent a If statement which is an AST visitor.
 * @param {Object} args - Arguments for creating the view.
 * @param {WhileStatement} args.model - The If statement model.
 * @param {Object} args.container - The HTML container to which the view should be added to.
 * @param {Object} args.parent - Parent View, which in this case the parent model
 * @param {Object} [args.viewOptions={}] - Configuration values for the view.
 * @class WhileStatementView
 * @constructor
 * @extends BlockStatementView
 */
class WhileStatementView extends BlockStatementView {
    constructor(args) {
        _.set(args, "viewOptions.title.text", "While");
        super(args);
        this.getModel()._isChildOfWorker = args.isChildOfWorker;
    }

    canVisitWhileStatement() {
        return true;
    }

    /**
     * Remove View callback
     * @param {ASTNode} parent - parent node
     * @param {ASTNode} child - child node
     */
    onBeforeModelRemove() {
        this._statementContainer.getBoundingBox().off('bottom-edge-moved');
        d3.select("#_" +this._model.id).remove();
        this.getBoundingBox().w(0).h(0);
    }

    /**
     * Render the while statement
     */
    render(diagramRenderingContext) {
        // Calling super render.
        (this.__proto__.__proto__).render.call(this, diagramRenderingContext);

        // Creating property pane
        var model = this.getModel();
        var editableProperty = {
            propertyType: "text",
            key: "Condition",
            model: model,
            getterMethod: model.getCondition,
            setterMethod: model.setCondition
        };
        this._createPropertyPane({
                                     model: model,
                                     statementGroup: this.getStatementGroup(),
                                     editableProperties: editableProperty
                                 });

        this._createDebugIndicator({
            statementGroup: this.getStatementGroup()
        });

        var parentStatementContainerBBox = this.getParent().getStatementContainer().getBoundingBox();
        var bBox = this.getBoundingBox();
        this.listenTo(bBox, 'width-changed', function () {
            if (parentStatementContainerBBox.w() > bBox.w()) {
                bBox.x(parentStatementContainerBBox.x() + (parentStatementContainerBBox.w() - bBox.w())/2);
            }
        });

        this.listenTo(model, 'update-property-text', this.updateConditionExpression);
    }

    updateConditionExpression(newCondition, propertyKey) {
        this.getModel().setCondition(newCondition);
    }
}

export default WhileStatementView;
    