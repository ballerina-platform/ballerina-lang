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
import TryStatement from '../ast/statements/try-statement';

/**
 * The view to represent a Try statement which is an AST visitor.
 * @class TryStatementView
 * @extends BlockStatementView
 */
class TryStatementView extends BlockStatementView {

    /**
     * Constructor for TryStatementView
     * @param {Object} args - Arguments for creating the view.
     * @param {TryStatement} args.model - The If statement model.
     * @param {Object} args.container - The HTML container to which the view should be added to.
     * @param {Object} args.parent - Parent Statement View, which in this case the try-catch statement
     * @param {Object} [args.viewOptions={}] - Configuration values for the view.
     * @constructor
     */
    constructor(args) {
        _.set(args, 'viewOptions.title.text', 'Try');
        super(args);
        this.getModel()._isChildOfWorker = args.isChildOfWorker;
    }

    canVisitTryStatement() {
        return true;
    }

    /**
     * Set the try statement model
     * @param {TryStatement} model
     */
    setModel(model) {
        if (!_.isNil(model) && model instanceof TryStatement) {
            (this.__proto__.__proto__).setModel(model);
        } else {
            log.error('If statement definition is undefined or is of different type.' + model);
            throw 'If statement definition is undefined or is of different type.' + model;
        }
    }

    initFromJson(jsonNode) {
        var self = this;
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            child.initFromJson(childNode);
            self.addChild(child);
        });
    }
}

export default TryStatementView;
