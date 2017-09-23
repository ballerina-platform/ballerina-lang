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
/**
 * Position Calculater for PackageDeclaration.
 *
 * @class PackageDeclarationPositionVisitor
 * */
class PackageDeclarationPositionVisitor {

    /**
     * begin visit.
     *
     * @param {Node} node.
     *
     * @memberOf PackageDeclarationPositionVisitor
     * */
    beginVisit(node) {
        log.debug('begin visit PackageDeclarationPositionCalcVisitor');
        const viewSate = node.viewState;
        const bBox = viewSate.bBox;
        bBox.x = 50;
        bBox.y = 20;
    }

    /**
     * visit the visitor at the end.
     *
     * @param {Node} node.
     *
     * @memberOf PackageDeclarationPositionVisitor
     * */
    endVisit(node) {
        log.debug('end visit PackageDeclarationPositionCalcVisitor');
    }
}

export default PackageDeclarationPositionVisitor;