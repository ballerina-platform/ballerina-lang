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
    var PackageDefinitionVisitor = function (parent) {
        AbstractSourceGenVisitor.call(this, parent);
    };

    PackageDefinitionVisitor.prototype = Object.create(AbstractSourceGenVisitor.prototype);
    PackageDefinitionVisitor.prototype.constructor = PackageDefinitionVisitor;

    PackageDefinitionVisitor.prototype.canVisitPackageDefinition = function (packageDefinition) {
        return true;
    };

    PackageDefinitionVisitor.prototype.beginVisitPackageDefinition = function (packageDefinition) {
        /**
         * set the configuration start for the package definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        if (!_.isNil(packageDefinition.getPackageName()) && packageDefinition.getPackageName() !== "") {
            var constructedSourceSegment = 'package ' + packageDefinition.getPackageName();
            this.appendSource(constructedSourceSegment);
        }
        log.debug('Begin Visit PackageDefinition');
    };

    PackageDefinitionVisitor.prototype.visitPackageDefinition = function (packageDefinition) {
        log.debug('Visit PackageDefinition');
    };

    PackageDefinitionVisitor.prototype.endVisitPackageDefinition = function (packageDefinition) {
        if (!_.isNil(packageDefinition.getPackageName()) && packageDefinition.getPackageName() !== "") {
            this.appendSource(";\n");
            this.getParent().appendSource(this.getGeneratedSource());
        }
        log.debug('End Visit PackageDefinition');
    };

    return PackageDefinitionVisitor;
});