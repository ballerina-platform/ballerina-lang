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
import 'bootstrap';
import './dialog.css';
const SaveToFileDialog = Backbone.View.extend(
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
        },

        show() {
            const self = this;
            this._fileSaveModal.modal('show').on('shown.bs.modal', () => {
                self.trigger('loaded');
            });
            this._fileSaveModal.on('hidden.bs.modal', () => {
                self.trigger('unloaded');
            });
        },

        setSelectedFile(path, fileName) {
            this._fileBrowser.select(path);
            if (!_.isNil(this._configNameInput)) {
                this._configNameInput.val(fileName);
            }
        },

        render() {
            // TODO : this render method should be rewritten with improved UI
            const self = this;
            let fileBrowser;
            const app = this.app;
            const notification_container = this.notification_container;

            if (!_.isNil(this._fileSaveModal)) {
                this._fileSaveModal.remove();
            }

            const fileSave = $(
                    "<div class='modal fade' id='saveConfigModal' tabindex='-1' role='dialog' aria-tydden='true'>" +
                    "<div class='modal-dialog file-dialog' role='document'>" +
                    "<div class='modal-content'>" +
                    "<div class='modal-header'>" +
                    "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                    "<span aria-hidden='true'>&times;</span>" +
                    '</button>' +
                    "<h4 class='modal-title file-dialog-title' id='newConfigModalLabel'>Save File</h4>" +
                    "<hr class='style1'>" +
                    '</div>' +
                    "<div class='modal-body'>" +
                    "<div class='container-fluid'>" +
                    "<form class='form-horizontal' onsubmit='return false'>" +
                    "<div class='form-group'>" +
                    "<label for='configName' class='col-sm-2 file-dialog-label'>File Name :</label>" +
                    "<div class='col-sm-9'>" +
                    "<input class='file-dialog-form-control' id='configName' placeholder='eg: sample.bal'>" +
                    '</div>' +
                    '</div>' +
                    "<div class='form-group'>" +
                    "<label for='location' class='col-sm-2 file-dialog-label'>Location :</label>" +
                    "<div class='col-sm-9'>" +
                    "<input type='text' class='file-dialog-form-control' id='location' placeholder='eg: /home/user/wso2-integration-server/ballerina-configs'>" +
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
                    "<button id='saveButton' type='button' class='btn btn-primary'>Save" +
                    '</button>' +
                    "<div class='divider'/>" +
                    "<button type='cancelButton' class='btn btn-default' data-dismiss='modal'>Cancel</button>" +
                    '</div>' +
                    '</div>' +
                    '</form>' +
                    "<div id='newWizardError' class='alert alert-danger'>" +
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
                    'Configuration saved successfully !' +
                    '</span>' +
                    '</div>');

            function getErrorNotification(detailedErrorMsg) {
                let errorMsg = 'Error while saving configuration';
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

            const saveConfigModal = fileSave.filter('#saveConfigModal');
            const newWizardError = fileSave.find('#newWizardError');
            const location = fileSave.find('input').filter('#location');
            const configName = fileSave.find('input').filter('#configName');

            const treeContainer = fileSave.find('div').filter('#fileTree');
            fileBrowser = new FileBrowser({ container: treeContainer, application: app, fetchFiles: false });

            fileBrowser.render();
            this._fileBrowser = fileBrowser;
            this._configNameInput = configName;

                // Gets the selected location from tree and sets the value as location
            this.listenTo(fileBrowser, 'selected', (selectedLocation) => {
                if (selectedLocation) {
                    location.val(selectedLocation);
                }
            });

            function handleSave() {
                const _location = location.val();
                let _configName = configName.val();
                if (_.isEmpty(_location)) {
                    newWizardError.text('Please enter a valid file location');
                    newWizardError.show();
                    return;
                }
                if (_.isEmpty(_configName)) {
                    newWizardError.text('Please enter a valid file name');
                    newWizardError.show();
                    return;
                }
                if (!_configName.endsWith('.bal')) {
                    _configName += '.bal';
                }

                const callback = function (isSaved) {
                    self.trigger('save-completed', isSaved);
                    if (isSaved) {
                        saveConfigModal.modal('hide');
                    }
                };

                const client = self.app.workspaceManager.getServiceClient();
                const path = `${_location}/${_configName}`;
                const existsResponse = client.exists(path);

                if (existsResponse.exists) {
                        // File with this name already exists. Need confirmation from user to replace
                    const replaceConfirmCb = function (confirmed) {
                        if (confirmed) {
                            saveConfiguration({ location: _location, configName: _configName }, callback);
                        } else {
                            callback(false);
                        }
                    };

                    const options = {
                        path,
                        handleConfirm: replaceConfirmCb,
                    };

                    self.app.commandManager.dispatch('open-replace-file-confirm-dialog', options);
                } else {
                    saveConfiguration({ location: _location, configName: _configName }, callback);
                }
            }

            fileSave.find('button').filter('#saveButton').click(() => {
                handleSave();
            });

            function handleKeyPress(e) {
                if (e.keyCode === 13 || e.which === 13) {
                    e.stopPropagation();
                    e.preventDefault();
                    handleSave();
                }
            }

            location.keypress(handleKeyPress);
            configName.keypress(handleKeyPress);

            $(this.dialog_container).append(fileSave);
            newWizardError.hide();
            this._fileSaveModal = fileSave;

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

            function isJsonString(str) {
                try {
                    JSON.parse(str);
                } catch (e) {
                    return false;
                }
                return true;
            }

            function saveConfiguration(options, callback) {
                const workspaceServiceURL = app.config.services.workspace.endpoint;
                const saveServiceURL = `${workspaceServiceURL }/write`;
                const activeTab = app.tabController.activeTab;
                const config = activeTab.getFile().getContent();
                const payload = `location=${  btoa(options.location)  }&configName=${  btoa(options.configName)
                         }&config=${  encodeURIComponent(config)}`;

                $.ajax({
                    url: saveServiceURL,
                    type: 'POST',
                    data: payload,
                    contentType: 'text/plain; charset=utf-8',
                    async: false,
                    success(data, textStatus, xhr) {
                        if (xhr.status == 200) {
                            activeTab.setTitle(options.configName);
                            activeTab.getFile()
                                            .setPath(options.location)
                                            .setName(options.configName)
                                            .setPersisted(true)
                                            .setLastPersisted(_.now())
                                            .setDirty(false);
                            if (app.workspaceExplorer.isEmpty()) {
                                app.commandManager.dispatch('open-folder', options.location);
                                if (!app.workspaceExplorer.isActive()) {
                                    app.commandManager.dispatch('toggle-file-explorer');
                                }
                            }
                            app.breadcrumbController.setPath(options.location, options.configName);
                            saveConfigModal.modal('hide');
                            log.debug('file saved successfully');
                            callback(true);
                        } else {
                            newWizardError.text(data.Error);
                            newWizardError.show();
                            callback(false);
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
                        newWizardError.text(msg);
                        newWizardError.show();
                        callback(false);
                    },
                });
            }
        },
    });

export default SaveToFileDialog;
