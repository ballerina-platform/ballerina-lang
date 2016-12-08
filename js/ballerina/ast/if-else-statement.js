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
define(['lodash', 'log','./node', './if-statement', './else-statement'], function (_, log, ASTNode, IfStatement, ElseStatement) {

    /**
     * Class for if conditions in ballerina.
     * @param elseStatements The 'else' statements of an IF condition.
     * @param elseIfStatements The 'else if' statements of an IF condition.
     * @constructor
     */
    var IfElseStatement = function (condition, ifStatements, elseStatements) {
        if(!_.isNil(condition) && !_.Nil){
            //this._ifNode = new IfStatement(condition,ifStatements);
            this.addChild(new IfStatement(condition,ifStatements))
        }
        else {
            log.error("Condition expression cannot be undefined");
        }
        if(!_.isNil(elseStatements)){
           // this._elseNode = new ElseStatement(elseStatements);
            this.addChild(new ElseStatement(elseStatements))
        }
    };

    IfElseStatement.prototype = Object.create(ASTNode.prototype);
    IfElseStatement.prototype.constructor = IfElseStatement;

    return IfStatement;
});