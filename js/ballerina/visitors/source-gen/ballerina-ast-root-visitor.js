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
define(['lodash', 'log', 'event_channel', './abstract-source-gen-visitor'], function(_, log, EventChannel, AbstractSourceGenVisitor) {

    var BallerinaASTRootVisitor = function() {
        AbstractSourceGenVisitor.call(this);
    };

    BallerinaASTRootVisitor.prototype = Object.create(AbstractSourceGenVisitor.prototype);
    BallerinaASTRootVisitor.prototype.constructor = BallerinaASTRootVisitor;

    BallerinaASTRootVisitor.prototype.canVisitBallerinaASTRoot = function(serviceDefinition){
        return false;
    };

    BallerinaASTRootVisitor.prototype.beginVisitBallerinaASTRoot = function(serviceDefinition){
        log.info('Begin Visit BallerinaASTRoot');
    };

    BallerinaASTRootVisitor.prototype.visitServiceBallerinaASTRoot = function(serviceDefinition){
        log.info('Visit BallerinaASTRoot');
    };

    BallerinaASTRootVisitor.prototype.endVisitServiceBallerinaASTRoot = function(serviceDefinition){
        log.info('End Visit BallerinaASTRoot');
    };

    return BallerinaASTRootVisitor;
});