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

import ASTVisitor from './../ast-visitor'
import log from 'log';

class ServiceDimensionCalcVisitor extends ASTVisitor {

    canVisitServiceDefinitionDimensionCalc(node) {
        log.debug('can visit ServiceStatementDimensionCalc');
        window.console.log('can visit ServiceStatementDimensionCalc');
    }

    beginVisitServiceDefinitionDimensionCalc(node) {
        log.debug('begin visit ServiceStatementDimensionCalc');
        window.console.log('begin visit ServiceStatementDimensionCalc');
    }

    visitServiceDefinitionDimensionCalc(node) {
        log.debug('visit ServiceStatementDimensionCalc');
        window.console.log('visit ServiceStatementDimensionCalc');
    }

    endVisitServiceDefinitionDimensionCalc(node) {
        log.debug('end visit ServiceStatementDimensionCalc');
        window.console.log('end visit ServiceStatementDimensionCalc');
    }
}