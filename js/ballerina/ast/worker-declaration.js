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
define(['lodash', './node'], function(_, ASTNode){

    var WorkerDeclaration = function(connections, variables, statements, replyStatement) {
        this.childrenList = [];
        this.reply = replyStatement;
    };

    WorkerDeclaration.prototype = Object.create(ASTNode.prototype);
    WorkerDeclaration.prototype.constructor = WorkerDeclaration;

    WorkerDeclaration.prototype.addChild = function (child, index) {
        if (_.isUndefined(index)) {
            this.childrenList.insert(index, child)
        } else {
            this.childrenList.push(child);
        }
    };

    WorkerDeclaration.prototype.setReply = function(replyStatement){
        if(!_.isNil(replyStatement)){
            this.reply = replyStatement;
        }
    };

    WorkerDeclaration.prototype.getReply = function(){
        return this.reply;
    };

    WorkerDeclaration.prototype.accept = function (visitor) {
        visitor.visitWorkerDeclaration();
    };

});