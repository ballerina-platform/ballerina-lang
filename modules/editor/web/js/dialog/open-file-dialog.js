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

define(['require', 'lodash','jquery', 'log', 'backbone', 'file_browser', 'ballerina', 'ballerina/diagram-render/diagram-render-context',
        'ballerina/views/source-view', 'workspace/file'],
    function (require, _, $, log, Backbone, FileBrowser, Ballerina, DiagramRenderContext, SourceView, File) {
    var OpenFileDialog = Backbone.View.extend(
        /** @lends SaveToFileDialog.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class SaveToFileDialog
             * @param {Object} config configuration options for the SaveToFileDialog
             */
            initialize: function (options) {
                this.app = options;
                this.dialog_container = _.get(options.config.dialog, 'container');
                this.notification_container = _.get(options.config.tab_controller.tabs.tab.ballerina_editor.notifications, 'container');
                this.source_view_container = _.get(options.config.tab_controller.tabs.tab.ballerina_editor, 'source_view.container');
                this.ballerina_editor = _.get(options.config.tab_controller.tabs.tab, 'ballerina_editor');
            },

            render: function () {
                //TODO : this render method should be rewritten with improved UI
                var self = this;
                var fileBrowser;
                var fileContent;
                var app = this.app;
                var notification_container = this.notification_container;
                var ballerinaEditorOptions = this.ballerina_editor;
                // var sourceViewContainer =  this.source_view_container;
                var diagramRenderingContext = new DiagramRenderContext();
                var BallerinaASTFactory = Ballerina.ast.BallerinaASTFactory;
                var ballerinaAstRoot1 = BallerinaASTFactory.createBallerinaAstRoot();

                var packageDefinition = BallerinaASTFactory.createPackageDefinition();
                packageDefinition.setPackageName("samples.passthrough");
                ballerinaAstRoot1.addChild(packageDefinition);
                ballerinaAstRoot1.setPackageDefinition(packageDefinition);

                var fileOpen = $(
                    "<div class='modal fade' id='openConfigModal' tabindex='-1' role='dialog' aria-tydden='true'>" +
                    "<div class='modal-dialog file-dialog' role='document'>" +
                    "<div class='modal-content'>" +
                    "<div class='modal-header'>" +
                    "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                    "<span aria-hidden='true'>&times;</span>" +
                    "</button>" +
                    "<h4 class='modal-title file-dialog-title'>Ballerina Service Open Wizard</h4>" +
                    "<hr class='style1'>"+
                    "</div>" +
                    "<div class='modal-body'>" +
                    "<div class='container-fluid'>" +
                    "<form class='form-horizontal'>" +
                    "<div class='form-group'>" +
                    "<label for='location' class='col-sm-2 file-dialog-label'>File Name :</label>" +
                    "<div class='col-sm-9'>" +
                    "<input type='text' class='file-dialog-form-control' id='location' placeholder='eg: /home/user/wso2-integration-server/ballerina-configs/sample.bal'>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<div class='file-dialog-form-scrollable-block'>" +
                    "<div id='fileTree'>" +
                    "</div>" +
                    "<div id='file-browser-error' class='alert alert-danger' style='display: none;'>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "<div class='form-group'>" +
                    "<div class='file-dialog-form-btn'>" +
                    "<button id='openButton' type='button' class='btn btn-file-dialog'>open" +
                    "</button>" +
                    "<div class='divider'/>" +
                    "<button type='button' class='btn btn-file-dialog' data-dismiss='modal'>cancel</button>" +
                    "</div>" +
                    "</div>" +
                    "</form>" +
                    "<div id='openFileWizardError' class='alert alert-danger'>" +
                    "<strong>Error!</strong> Something went wrong." +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</div>"
                );

                var successNotification = $(
                    "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-success' id='success-alert'>" +
                    "<span class='notification'>" +
                    "Configuration opened successfully !" +
                    "</span>" +
                    "</div>");

                var errorNotification = $(
                    "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-danger' id='error-alert'>" +
                    "<span class='notification'>" +
                    "Error while opening configuration !" +
                    "</span>" +
                    "</div>");


                var openConfigModal = fileOpen.filter("#openConfigModal");
                var openFileWizardError = fileOpen.find("#openFileWizardError");
                var location = fileOpen.find("input").filter("#location");

                var treeContainer  = fileOpen.find("div").filter("#fileTree")
                fileBrowser = new FileBrowser({container: treeContainer, application:app, action:'openFile'});

                fileBrowser.render();

                //Gets the selected location from tree and sets the value as location
                this.listenTo(fileBrowser, 'selected', function (selectedLocation) {
                    if(selectedLocation){
                        location.val(selectedLocation);
                    }
                });


                fileOpen.find("button").filter("#openButton").click(function () {

                    var _location = location.val();
                    if (_.isEmpty(_location)) {
                        openFileWizardError.text("Invalid Value for Location.");
                        openFileWizardError.show();
                        return;
                    }

                    openConfigModal.modal('hide');
                    openConfiguration({location: location});
                });


                $(this.dialog_container).append(fileOpen);
                openFileWizardError.hide();
                fileOpen.modal('show');

                function alertSuccess() {
                    $(notification_container).append(successNotification);
                    successNotification.fadeTo(2000, 200).slideUp(1000, function () {
                        successNotification.slideUp(1000);
                    });
                };

                function alertError() {
                    $(notification_container).append(errorNotification);
                    errorNotification.fadeTo(2000, 200).slideUp(1000, function () {
                        errorNotification.slideUp(1000);
                    });
                };

                function openModel(source){
                    $.ajax({
                        url: "http://localhost:8289/ballerina/model/content",
                        type: "POST",
                        data: JSON.stringify(source),
                        contentType: "application/json; charset=utf-8",
                        async: false,
                        dataType:"json",
                        success: function (data, textStatus, xhr) {
                            if (xhr.status == 200) {
                                var BallerinaASTDeserializer = Ballerina.ast.BallerinaASTDeserializer;
                                var root = BallerinaASTDeserializer.getASTModel(data);

                                var command = app.commandManager;
                                command.dispatch("create-new-tab", root);

                                alertSuccess();
                            } else {
                                alertError();
                            }
                        },
                        error: function (res, errorCode, error) {
                            alertError();
                        }
                    });
                }

                function openConfiguration() {
                    var defaultView = {configLocation: location.val()};

                    var workspaceServiceURL = "http://localhost:8289/service/workspace";
                    var saveServiceURL = workspaceServiceURL + "/read";

                    var path = defaultView.configLocation;
                    $.ajax({
                        url: saveServiceURL,
                        type: "POST",
                        data: path,
                        contentType: "text/plain; charset=utf-8",
                        async: false,
                        success: function (data, textStatus, xhr) {
                            if (xhr.status == 200) {
                                var pathArray = _.split(path, self.app.getPathSeperator()),
                                    fileName = _.last(pathArray),
                                    folderPath = _.join(_.take(pathArray, pathArray.length -1), self.app.getPathSeperator());

                                var file = new File({
                                    name: fileName,
                                    path: folderPath,
                                    content: data,
                                    isPersisted: true
                                });
                                app.commandManager.dispatch("create-new-tab", {tabOptions: {file: file}});
                                alertSuccess();
                            } else {
                                alertError();
                            }
                        },
                        error: function (res, errorCode, error) {
                            alertError();
                        }
                    });
                };
            },
        });

    return OpenFileDialog;
});