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
define(['lodash', 'log', 'event_channel', './abstract-source-gen-visitor'], function (_, log, EventChannel, AbstractSourceGenVisitor) {

    /**
     * @param parent
     * @constructor
     */
    var ImportDeclarationVisitor = function (parent) {
        AbstractSourceGenVisitor.call(this, parent);
    };

    ImportDeclarationVisitor.prototype = Object.create(AbstractSourceGenVisitor.prototype);
    ImportDeclarationVisitor.prototype.constructor = ImportDeclarationVisitor;

    ImportDeclarationVisitor.prototype.canVisitImportDeclaration = function (importDeclaration) {
        return true;
    };

    ImportDeclarationVisitor.prototype.beginVisitImportDeclaration = function (importDeclaration) {
        /**
         * set the configuration start for the package definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        var constructedSourceSegment = 'import ' + importDeclaration.getPackageName();
        this.appendSource(constructedSourceSegment);
        log.debug('Begin Visit ImportDeclaration');
    };

    ImportDeclarationVisitor.prototype.visitImportDeclaration = function (importDeclaration) {
        log.debug('Visit ImportDeclaration');
    };

    ImportDeclarationVisitor.prototype.endVisitImportDeclaration = function (importDeclaration) {
        this.appendSource(";\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit ImportDeclaration');
    };

    return ImportDeclarationVisitor;
});