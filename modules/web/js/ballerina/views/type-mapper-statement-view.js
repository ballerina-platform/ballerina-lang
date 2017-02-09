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
define(['lodash', 'log','./ballerina-view', './variables-view', 'ballerina/ast/ballerina-ast-factory', 'typeMapper','./../ast/statement'],
    function (_, log,BallerinaView, VariablesView, BallerinaASTFactory, TypeMapper,Statement) {

        var TypeMapperStatementView = function (args) {
            BallerinaView.call(this, args);
            this._parentView = _.get(args, "parentView");

            if (_.isNil(this.getModel()) || !(this._model instanceof Statement)) {
                log.error("Type Struct definition is undefined or is of different type." + this.getModel());
                throw "Type Struct definition is undefined or is of different type." + this.getModel();
            }

        };

        TypeMapperStatementView.prototype = Object.create(BallerinaView.prototype);
        TypeMapperStatementView.prototype.constructor = TypeMapperStatementView;

        TypeMapperStatementView.prototype.canVisitTypeMapperStatementView = function (typeMapperStatementView) {
            return true;
        };

        /**
         * Calls the render method for assignmentStatement.
         * @param {statement} statement - The statement model.
         */
        TypeMapperStatementView.prototype.visitStatement = function (statement) {
            var self = this;
//            var typeMapperStatementView = new TypeMapperStatement({
//                model: statement, parentView: this
//            });
//
//            typeMapperStatementView.render(this.diagramRenderingContext, this._typeMapper);


        }



        return TypeMapperStatementView;
});