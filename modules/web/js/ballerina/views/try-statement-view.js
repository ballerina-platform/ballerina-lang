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
define(
    ['require', 'lodash', 'log', './block-statement-view', './../ast/try-statement'],
    function (require, _, log, BlockStatementView, TryStatement) {

        /**
         * The view to represent a Try statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {TryStatement} args.model - The If statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} args.parent - Parent Statement View, which in this case the try-catch statement
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @class TryStatementView
         * @constructor
         * @extends BlockStatementView
         */
        var TryStatementView = function (args) {
            _.set(args, "viewOptions.title.text", "Try");
            BlockStatementView.call(this, args);
            this.getModel()._isChildOfWorker = args.isChildOfWorker;
        };

        TryStatementView.prototype = Object.create(BlockStatementView.prototype);
        TryStatementView.prototype.constructor = TryStatementView;

        TryStatementView.prototype.canVisitTryStatement = function(){
            return true;
        };

        /**
         * Set the try statement model
         * @param {TryStatement} model
         */
        TryStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof TryStatement) {
                (this.__proto__.__proto__).setModel(model);
            } else {
                log.error("If statement definition is undefined or is of different type." + model);
                throw "If statement definition is undefined or is of different type." + model;
            }
        };

        TryStatementView.prototype.initFromJson = function (jsonNode) {
            var self = this;
            _.each(jsonNode.children, function (childNode) {
                var child = self.getFactory().createFromJson(childNode);
                child.initFromJson(childNode);
                self.addChild(child);
            });
        };

        return TryStatementView;
    });