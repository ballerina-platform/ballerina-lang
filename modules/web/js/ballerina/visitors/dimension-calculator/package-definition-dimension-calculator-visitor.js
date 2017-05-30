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
import { packageDefinition } from '../../configs/designer-defaults';
import ASTFactory from '../../ast/ballerina-ast-factory';

class PackageDefinitionDimensionCalculatorVisitor {

    canVisit(node) {
        log.info('can visit PackageDefinitionDimensionCalcVisitor');
        return true;
    }

    beginVisit(node) {
        log.info('begin visit PackageDefinitionDimensionCalcVisitor');
    }

    visit(node) {
        log.info('visit PackageDefinitionDimensionCalcVisitor');
    }

    endVisit(node) {
        log.info('end visit PackageDefinitionDimensionCalcVisitor');
        let viewState = node.getViewState();
        const topGutter = 10;
        const topBarHeight = 25;
        const importInputHeight = 40;

        let height = 0;
        const astRoot = node.parent;

        if(viewState.importsExpanded) {
            const imports = astRoot.children.filter(
                c => {return ASTFactory.isImportDeclaration(c);});

            height += topGutter + topBarHeight + importInputHeight +
                      imports.length * packageDefinition.importDeclaration.itemHeight;
        }

        if(viewState.globalsExpanded) {
            const globals = astRoot.children.filter(
                c => {return ASTFactory.isConstantDefinition(c);});

            height += topGutter + topBarHeight + importInputHeight +
                      globals.length * packageDefinition.importDeclaration.itemHeight;
        }

        viewState.bBox.h = height;
        viewState.bBox.w = 0;
    }
}

export default PackageDefinitionDimensionCalculatorVisitor;
