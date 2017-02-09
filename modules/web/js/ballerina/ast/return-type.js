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
define(['lodash', 'log', './node'], function (_, log, ASTNode) {

    var ReturnType = function (args) {
        ASTNode.call(this, 'ReturnType');
    };

    ReturnType.prototype = Object.create(ASTNode.prototype);
    ReturnType.prototype.constructor = ReturnType;

    /**
     * Gets the value of struct-type child
     * @return {string} - String structType.
     */
    ReturnType.prototype.getStructType = function() {
        var structType = undefined;
        var ballerinaASTFactory = this.getFactory();

        _.forEach(this.getChildren(), function (child) {
            if (ballerinaASTFactory.isStructType(child)) {
                structType = child.getTypeName();
                return false;
            }
        });
        return structType;
    };


    /**
     * initialize from json
     * @param jsonNode
     */
    ReturnType.prototype.initFromJson = function (jsonNode) {
        var self = this;
        var BallerinaASTFactory = this.getFactory();

        _.each(jsonNode.children, function (childNode) {
            var child = BallerinaASTFactory.createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };

    return ReturnType;
});