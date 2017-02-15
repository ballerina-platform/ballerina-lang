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
define(['require', 'lodash', 'jquery', 'log', './../ast/import-declaration'],
    function (require, _, $, log, ImportDeclaration) {

        /**
         * Creates a new instance for a package definition pane view.
         * @param args - Arguments for creating the package definition pane.
         * @param {BallerinaASTRoot} args.model - The Ballerina AST root model.
         * @param {HTMLElement} args.paneAppendElement - The element to which the pane to be appended.
         * @param {BallerinaFileEditor} args.view - The ballerina filed editor view.
         * @constructor
         */
        var ImportDeclarationView = function(args) {
            this._model = _.get(args, "model");
            this._container = _.get(args, "container");
            this._toolPalette = _.get(args, "toolPalette");
        };

        ImportDeclarationView.prototype.constructor = ImportDeclarationView;

        /**
         * Creates the pane view of the import declaration.
         */
        ImportDeclarationView.prototype.render = function() {
            var self = this;
            var paneAppendElement = this._container.find('.imports-content-wrapper');
            var importDeclarationWrapper = $("<div/>", {
                id: this._model.getID(),
                class: "import-declaration-wrapper",
            }).appendTo(paneAppendElement);

            var importType = $("<div/>", {
                class: "import-declaration-type",
            }).appendTo(importDeclarationWrapper);

            var importedPackageName = $("<div/>", {
                class: "import-package-name",
                text: this._model._packageName
            }).appendTo(importDeclarationWrapper);

            var importDelete = $("<i class='fw fw-cancel'></i>").appendTo(importDeclarationWrapper);

            // Creating import delete event.
            $(importDelete).on("click", function () {
                var packageName = importDeclarationWrapper.text().trim();
                self._model.remove();
                self._toolPalette.getItemProvider().removeImportToolGroup(packageName);
            });
            this._model.on('before-remove', function () {
                importDeclarationWrapper.remove();
            }, this);

        };


        return ImportDeclarationView;

    });