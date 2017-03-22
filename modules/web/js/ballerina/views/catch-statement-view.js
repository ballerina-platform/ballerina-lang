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
import CatchStatement from '../ast/statements/catch-statement';

/**
 * The view to represent a Catch statement which is an AST visitor.
 * @param {Object} args - Arguments for creating the view.
 * @param {CatchStatement} args.model - The Try Catch statement model.
 * @param {Object} args.container - The HTML container to which the view should be added to.
 * @param {Object} args.parent - Parent Statement View, which in this case the try-catch statement
 * @param {Object} [args.viewOptions={}] - Configuration values for the view.
 * @class CatchStatementView
 * @constructor
 * @extends BlockStatementView
 */
class CatchStatementView extends BlockStatementView {
    constructor(args) {
        _.set(args, "viewOptions.title.text", "Catch");
        super(args);
        this.getModel()._isChildOfWorker = args.isChildOfWorker;
    }

    canVisitCatchStatement() {
        return true;
    }

    /**
     * set the catch statement model
     * @param {CatchStatement} model
     */
    setModel(model) {
        if (!_.isNil(model) && model instanceof CatchStatement) {
            (this.__proto__.__proto__).setModel(model);
        } else {
            log.error("Try Catch statement definition is undefined or is of different type." + model);
            throw "Try Catch statement definition is undefined or is of different type." + model;
        }
    }

    render(diagramRenderingContext) {
        // Calling super render.
        (this.__proto__.__proto__).render.call(this, diagramRenderingContext);

        this.listenTo(this._model, 'update-property-text', function(value, key){
            this._model.setParameter(value);
        });
    }
}

export default CatchStatementView;
