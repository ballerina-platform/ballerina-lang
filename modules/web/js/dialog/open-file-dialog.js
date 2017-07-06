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

import _ from 'lodash';
import $ from 'jquery';
import log from 'log';
import Backbone from 'backbone';
import FileBrowser from 'file_browser';
import BallerinaASTFactory from 'ballerina/ast/ballerina-ast-factory';
import BallerinaASTDeserializer from 'ballerina/ast/ballerina-ast-deserializer';
import DiagramRenderContext from 'ballerina/diagram-render/diagram-render-context';
import File from 'workspace/file';
import 'bootstrap';
import './dialog.css';
const OpenFileDialog = Backbone.View.extend(
        /** @lends SaveToFileDialog.prototype */
    {
            /**
             * @augments Backbone.View
             * @constructs
             * @class SaveToFileDialog
             * @param {Object} config configuration options for the SaveToFileDialog
             */
        initialize(options) {
            this.app = options;
            this.dialog_container = $(_.get(options.config.dialog, 'container'));
            this.notification_container = _.get(options.config.tab_controller.tabs.tab.ballerina_editor.notifications, 'container');
            this.source_view_container = _.get(options.config.tab_controller.tabs.tab.ballerina_editor, 'source_view.container');
            this.ballerina_editor = _.get(options.config.tab_controller.tabs.tab, 'ballerina_editor');
        },

        show() {
            this._fileOpenModal.modal('show');
        },

        select(path) {
            this._fileBrowser.select('path');
        },

        render() {
                // TODO : this render method should be rewritten with improved UI
            const self = this;
            let fileBrowser;
            let fileContent;
            const app = this.app;
            const notification_container = this.notification_container;
            const ballerinaEditorOptions = this.ballerina_editor;
                // var sourceViewContainer =  this.source_view_container;
            const diagramRenderingContext = new DiagramRenderContext();

            if (!_.isNil(this._fileOpenModal)) {
                this._fileOpenModal.remove();
            }

            const fileOpen = $(
                    "<div class='modal fade' id='openConfigModal' tabindex='-1' role='dialog' aria-tydden='true'>" +
                    "<div class='modal-dialog file-dialog' role='document'>" +
                    "<div class='modal-content'>" +
                    "<div class='modal-header'>" +
                    "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                    "<span aria-hidden='true'>&times;</span>" +
                    '</button>' +
                    "<h4 class='modal-title file-dialog-title'>Open File</h4>" +
                    "<hr class='style1'>" +
                    '</div>' +
                    "<div class='modal-body'>" +
                    "<div class='container-fluid'>" +
                    "<form class='form-horizontal' onsubmit='return false'>" +
                    "<div class='form-group'>" +
                    "<label for='location' class='col-sm-2 file-dialog-label'>File Name :</label>" +
                    "<div class='col-sm-9'>" +
                    "<input type='text' class='file-dialog-form-control' id='location' placeholder='eg: /home/user/wso2-integration-server/ballerina-configs/sample.bal'>" +
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
                    "<button id='openButton' type='button' class='btn btn-primary'>Open" +
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
                    '</div>',
                );

            const successNotification = $(
                    "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-success' id='success-alert'>" +
                    "<span class='notification'>" +
                    'Configuration opened successfully !' +
                    '</span>' +
                    '</div>');

            function getErrorNotification(detailedErrorMsg) {
                let errorMsg = 'Error while opening configuration';
                if (!_.isEmpty(detailedErrorMsg)) {
                    errorMsg += (` : ${  detailedErrorMsg}`);
                }
                return $(
                        `${"<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-danger' id='error-alert'>" +
                        "<span class='notification'>"}${
                        errorMsg
                        }</span>` +
                        '</div>');
            }

            const openConfigModal = fileOpen.filter('#openConfigModal');
            const openFileWizardError = fileOpen.find('#openFileWizardError');
            const location = fileOpen.find('input').filter('#location');

            const treeContainer = fileOpen.find('div').filter('#fileTree');
            fileBrowser = new FileBrowser({ container: treeContainer, application: app, fetchFiles: true });

            fileBrowser.render();
            this._fileBrowser = fileBrowser;

                // Gets the selected location from tree and sets the value as location
            this.listenTo(fileBrowser, 'selected', (selectedLocation) => {
                if (selectedLocation) {
                    location.val(selectedLocation);
                }
            });

            // Open file on enter key press.
            openConfigModal.keyup((e) => {
                if (e.keyCode === 13) {
                    const _location = location.val();
                    if (_.isEmpty(_location)) {
                        openFileWizardError.text('Invalid Value for Location.');
                        openFileWizardError.show();
                        return;
                    }
                    openConfiguration({ location });
                }
            });

            fileOpen.find('button').filter('#openButton').click(() => {
                const _location = location.val();
                if (_.isEmpty(_location)) {
                    openFileWizardError.text('Invalid Value for Location.');
                    openFileWizardError.show();
                    return;
                }
                openConfiguration({ location });
            });


            $(this.dialog_container).append(fileOpen);
            openFileWizardError.hide();
            this._fileOpenModal = fileOpen;

            function alertSuccess() {
                $(notification_container).append(successNotification);
                successNotification.fadeTo(2000, 200).slideUp(1000, () => {
                    successNotification.slideUp(1000);
                });
            }

            function alertError(errorMessage) {
                const errorNotification = getErrorNotification(errorMessage);
                $(notification_container).append(errorNotification);
                errorNotification.fadeTo(2000, 200).slideUp(1000, () => {
                    errorNotification.slideUp(1000);
                });
            }

            function openModel(source) {
                const parserServiceURL = app.config.services.parser.endpoint;
                $.ajax({
                    url: parserServiceURL,
                    type: 'POST',
                    data: JSON.stringify(source),
                    contentType: 'application/json; charset=utf-8',
                    async: false,
                    dataType: 'json',
                    success(data, textStatus, xhr) {
                        if (xhr.status == 200) {
                            let root = BallerinaASTDeserializer.getASTModel(data);
                            openConfigModal.modal('hide');
                            let command = app.commandManager;
                            command.dispatch('create-new-tab', root);
                        } else {
                            alertError(data.Error);
                        }
                    },
                    error(res, errorCode, error) {
                        alertError(JSON.parse(res.responseText).Error);
                    },
                });
            }

            function isJsonString(str) {
                try {
                    JSON.parse(str);
                } catch (e) {
                    return false;
                }
                return true;
            }

            function openConfiguration() {
                const defaultView = { configLocation: location.val() };

                const workspaceServiceURL = app.config.services.workspace.endpoint;
                const saveServiceURL = `${workspaceServiceURL}/read`;

                const path = defaultView.configLocation;
                $.ajax({
                    url: saveServiceURL,
                    type: 'POST',
                    data: path,
                    contentType: 'text/plain; charset=utf-8',
                    async: false,
                    success(data, textStatus, xhr) {
                        if (xhr.status == 200) {
                            let pathArray = _.split(path, self.app.getPathSeperator());
                            let fileName = _.last(pathArray);
                            let folderPath = _.join(_.take(pathArray, pathArray.length - 1), self.app.getPathSeperator());

                            let file = new File({
                                name: fileName,
                                path: folderPath,
                                content: data.content,
                                isPersisted: true,
                                isDirty: false,
                            });
                            openConfigModal.modal('hide');
                            app.commandManager.dispatch('create-new-tab', { tabOptions: { file } });
                        } else {
                            openFileWizardError.text(data.Error);
                            openFileWizardError.show();
                        }
                    },
                    error(res, errorCode, error) {
                        let msg = _.isString(error) ? error : res.statusText;
                        if (isJsonString(res.responseText)) {
                            let resObj = JSON.parse(res.responseText);
                            if (_.has(resObj, 'Error')) {
                                msg = _.get(resObj, 'Error');
                            }
                        }
                        openFileWizardError.text(msg);
                        openFileWizardError.show();
                    },
                });
            }
        },
    });

export default OpenFileDialog;
