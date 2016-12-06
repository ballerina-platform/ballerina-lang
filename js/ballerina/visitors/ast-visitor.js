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
define(['lodash', 'log', 'event_channel'], function(_, log, EventChannel) {

    var ASTVisitor = function() {};

    ASTVisitor.prototype = Object.create(EventChannel.prototype);
    ASTVisitor.prototype.constructor = ASTVisitor;

    ASTVisitor.prototype.canVisitBallerinaASTRoot = function(ballerinaASTRoot){
        return false;
    };
    ASTVisitor.prototype.beginVisitBallerinaASTRoot = function(ballerinaASTRoot){
    };
    ASTVisitor.prototype.visitBallerinaASTRoot= function(ballerinaASTRoot){
    };
    ASTVisitor.prototype.endVisitBallerinaASTRoot = function(ballerinaASTRoot){
    };

    ASTVisitor.prototype.canVisitServiceDefinition = function(serviceDefinition){
        return false;
    };
    ASTVisitor.prototype.beginVisitServiceDefinition = function(serviceDefinition){
    };
    ASTVisitor.prototype.visitServiceDefinition = function(serviceDefinition){
    };
    ASTVisitor.prototype.endVisitServiceDefinition = function(serviceDefinition){
    };

    ASTVisitor.prototype.canVisitResourceDefinition = function(resourceDefinition){
        return false;
    };
    ASTVisitor.prototype.beginVisitResourceDefinition = function(resourceDefinition){
    };
    ASTVisitor.prototype.visitResourceDefinition = function(resourceDefinition){
    };
    ASTVisitor.prototype.endVisitResourceDefinition = function(resourceDefinition){
    };

    return ASTVisitor;

});