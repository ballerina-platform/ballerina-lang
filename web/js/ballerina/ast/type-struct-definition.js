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
define(['lodash', 'require', 'log', './node'],
    function (_, require, log, ASTNode) {

        /**
         * Constructor for ResourceDefinition
         * @param {Object} args - The arguments to create the ServiceDefinition
         * @param {string} [args.resourceName=newResource] - Service name
         * @param {string[]} [args.annotations] - Resource annotations
         * @param {string} [args.annotations.Method] - Resource annotation for Method
         * @param {string} [args.annotations.Path] - Resource annotation for Path
         * @constructor
         */
        var TypeStructDefinition = function (args) {
            this._typeStructName = _.get(args, 'typeStructName', 'Struct1');
            this._annotations = _.get(args, 'annotations', []);
            this._schema;
            this.BallerinaASTFactory = this.getFactory();
        };

        TypeStructDefinition.prototype = Object.create(ASTNode.prototype);
        TypeStructDefinition.prototype.constructor = TypeStructDefinition;

        TypeStructDefinition.prototype.setTypeStructName = function (typeStructName) {
            if (!_.isNil(typeStructName)) {
                this._typeStructName = typeStructName;
            }
        };

        TypeStructDefinition.prototype.getTypeStructName = function () {
           return this._typeStructName;
        };

        TypeStructDefinition.prototype.setSchema = function (schema) {
            if (!_.isNil(schema)) {
                this._schema = schema;
            }
        };

        return TypeStructDefinition;
    });
