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
import log from 'log';
import AbstractExpressionSourceGenVisitor from './abstract-expression-source-gen-visitor';

class SructFieldAccessExpressionVisitor extends AbstractExpressionSourceGenVisitor {

    canVisitStructFieldAccessExpression() {
        return true;
    }

    beginVisitStructFieldAccessExpression(structFieldAccessExpression) {
        this.appendSource(structFieldAccessExpression.getExpressionString());
        log.debug('Begin Visit Struct Field Access Expression');
    }

    visitStructFieldAccessExpression() {
        log.debug('Visit Struct Field Access Expression');
    }

    endVisitStructFieldAccessExpression() {
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Struct Field Access Expression');
    }
}

export default SructFieldAccessExpressionVisitor;
