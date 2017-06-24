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

import _ from 'lodash';
import $ from 'jquery';
import Backbone from 'backbone';
import FileBrowser from 'file_browser';
import BallerinaASTFactory from 'ballerina/ast/ballerina-ast-factory';
import SourceGenVisitor from 'ballerina/visitors/source-gen/ballerina-ast-root-visitor';
import File from 'workspace/file';
import 'bootstrap';
import './dialog.css';
import SwaggerParser from '../swagger-parser/swagger-parser';

const SwaggerImportDialog = Backbone.View.extend(
    {
        /**
         * @augments Backbone.View
         * @constructs
         * @class SaveToFileDialog
         * @param {Object} options configuration options for the SwaggerImportDialog.
         */
        initialize(options) {
            this.app = options;
            this.dialog_container = $(_.get(options.config.dialog, 'container'));
            this._openConfigModal = undefined;
            this._fileBrowser = undefined;
            this._swaggerOpenModal = undefined;
            this._openFileWizardError = undefined;
        },

        show() {
            this._swaggerOpenModal.modal('show');
        },

        select() {
            this._fileBrowser.select('path');
        },

        render() {
            const app = this.app;

            if (!_.isNil(this._swaggerOpenModal)) {
                this._swaggerOpenModal.remove();
            }

            const fileOpen = $(
                "<div class='modal fade' id='openConfigModal' tabindex='-1' role='dialog' aria-tydden='true'>" +
                "<div class='modal-dialog file-dialog' role='document'>" +
                "<div class='modal-content'>" +
                "<div class='modal-header'>" +
                "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                "<span aria-hidden='true'>&times;</span>" +
                '</button>' +
                "<h4 class='modal-title file-dialog-title'>Open Swagger Definition</h4>" +
                "<hr class='style1'>" +
                '</div>' +
                "<div class='modal-body'>" +
                "<div class='container-fluid'>" +
                "<form class='form-horizontal' onsubmit='return false'>" +
                "<div class='form-group'>" +
                "<label for='location' class='col-sm-2 file-dialog-label'>File Name :</label>" +
                "<div class='col-sm-9'>" +
                "<input type='text' class='file-dialog-form-control' id='location'" +
                "placeholder='eg: /home/user/swagger-configs/swagger.json'>" +
                '</div>' +
                '</div>' +
                "<div class='form-group'>" +
                "<div class='file-dialog-form-scrollable-block'>" +
                "<div id='fileTree'>" +
                '</div>' +
                "<div id='file-browser-error' class='alert alert-danger' style='display: none;'>" +
                '</div>' +
                '</div>' +
                '</div>' +
                "<div class='form-group'>" +
                "<div class='file-dialog-form-btn'>" +
                "<button id='openButton' type='button' class='btn btn-primary'>Import" +
                '</button>' +
                "<div class='divider'/>" +
                "<button type='button' class='btn btn-default' data-dismiss='modal'>Cancel</button>" +
                '</div>' +
                '</div>' +
                '</form>' +
                "<div id='openFileWizardError' class='alert alert-danger'>" +
                '<strong>Error!</strong> Something went wrong.' +
                '</div>' +
                '</div>' +
                '</div>' +
                '</div>' +
                '</div>' +
                '</div>');

            this._openConfigModal = fileOpen.filter('#openConfigModal');
            this._openFileWizardError = fileOpen.find('#openFileWizardError');
            const location = fileOpen.find('input').filter('#location');

            const treeContainer = fileOpen.find('div').filter('#fileTree');
            this._fileBrowser = new FileBrowser({
                container: treeContainer,
                application: app,
                fetchFiles: true,
                extensions: ['.json', '.yaml', '.yml'],
            });

            this._fileBrowser.render();

            // Gets the selected location from tree and sets the value as location
            this.listenTo(this._fileBrowser, 'selected', (selectedLocation) => {
                if (selectedLocation) {
                    location.val(selectedLocation);
                }
            });

            fileOpen.find('button').filter('#openButton').click(() => {
                if (_.isEmpty(location.val())) {
                    this._openFileWizardError.text('Invalid Value for Location.');
                    this._openFileWizardError.show();
                    return;
                }
                this.openConfiguration(location);
            });

            $(this.dialog_container).append(fileOpen);
            this._openFileWizardError.hide();
            this._swaggerOpenModal = fileOpen;
        },

        isJsonString(str) {
            try {
                JSON.parse(str);
            } catch (e) {
                return false;
            }
            return true;
        },

        openConfiguration(location) {
            const workspaceServiceURL = this.app.config.services.workspace.endpoint;
            const readServiceURL = `${workspaceServiceURL}/read`;

            const path = location.val();
            $.ajax({
                url: readServiceURL,
                type: 'POST',
                data: path,
                contentType: 'text/plain; charset=utf-8',
                async: false,
                success: (data, textStatus, xhr) => {
                    if (xhr.status === 200) {
                        try {
                            const pathArray = _.split(path, this.app.getPathSeperator());
                            const fileName = _.last(pathArray);
                            const folderPath = _.join(_.take(pathArray, pathArray.length - 1),
                                                                                        this.app.getPathSeperator());

                            // Creating the AST according to the swagger def.
                            let swaggerParser;
                            if (fileName.split('.').pop().toLowerCase() === '.json') {
                                swaggerParser = new SwaggerParser(JSON.parse(data.content));
                            } else {
                                swaggerParser = new SwaggerParser(data.content, true);
                            }
                            const ballerinaAstRoot = BallerinaASTFactory.createBallerinaAstRoot();
                            const serviceDefinition = BallerinaASTFactory.createServiceDefinition();
                            ballerinaAstRoot.addChild(serviceDefinition);
                            swaggerParser.mergeToService(serviceDefinition);

                            // Creating the source code.
                            const sourceGenVisitor = new SourceGenVisitor();
                            ballerinaAstRoot.accept(sourceGenVisitor);

                            // Creating a new tab
                            const file = new File({
                                name: fileName.replace(/\.[^/.]+$/, '.bal'),
                                path: folderPath,
                                content: sourceGenVisitor.getGeneratedSource(),
                                isPersisted: true,
                                isDirty: true,
                            });
                            this._openConfigModal.modal('hide');
                            this.app.commandManager.dispatch('create-new-tab', { tabOptions: { file } });
                        } catch (error) {
                            this._openFileWizardError.text(`Error occurred when importing: ${error.message}`);
                            this._openFileWizardError.show();
                        }
                    } else {
                        this._openFileWizardError.text(data.Error);
                        this._openFileWizardError.show();
                    }
                },
                error: (res, errorCode, error) => {
                    let msg = _.isString(error) ? error : res.statusText;
                    if (this.isJsonString(res.responseText)) {
                        const resObj = JSON.parse(res.responseText);
                        if (_.has(resObj, 'Error')) {
                            msg = _.get(resObj, 'Error');
                        }
                    }
                    this._openFileWizardError.text(msg);
                    this._openFileWizardError.show();
                },
            });
        },
    });

export default SwaggerImportDialog;
