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

import $ from 'jquery';
import log from 'log';
import Backbone from 'backbone';
import FileBrowser from 'file_browser';
import 'bootstrap';
import './dialog.css';
var FolderOpenDialog = Backbone.View.extend(
        /** @lends FolderOpenDialog.prototype */
    {
            /**
             * @augments Backbone.View
             * @constructs
             * @class FolderOpenDialog
             * @param {Object} options configuration options
             */
        initialize: function (options) {
            this._options = options;
            this.application = _.get(options, "application");
            this._dialogContainer =  $(_.get(options, "application.config.dialog.container"));
        },

        show: function () {
            this._modalContainer.modal('show');
            this._errorsContainer.hide();
        },

        setSelectedFolder: function (path) {
            this._fileBrowser.select(path);
        },

            /**
             * This will open the folder and focus file explorer by dispatching "open-folder", "toggle-file-explorer" events.
             * @param {Object} openFolderModal modal
             */
        openFolder: function(openFolderModal){
            var app = this.application;
            var options = this._options;
            var errorsContainer = openFolderModal.find(_.get(options, 'errors_container'));
            var location = openFolderModal.find("input").filter(_.get(options, 'location_input'));
            var path = location.val();
            if (_.isEmpty(path)) {
                errorsContainer.text("Invalid value for location.");
                errorsContainer.show();
                return;
            }
            openFolderModal.modal('hide');
            app.commandManager.dispatch("open-folder", path);
            if(!app.workspaceExplorer.isActive()){
                app.commandManager.dispatch("toggle-file-explorer");
            }
        },

        render: function () {
            var fileBrowser,
                app = this.application,
                options = this._options,
                self = this;

            if(!_.isNil(this._modalContainer)){
                this._modalContainer.remove();
            }

            var openFolderModal = $(_.get(options, 'modal_selector')).clone();

            var errorsContainer = openFolderModal.find(_.get(options, 'errors_container'));
            var location = openFolderModal.find("input").filter(_.get(options, 'location_input'));
            var treeContainer = openFolderModal.find("div").filter(_.get(options, 'tree_container'));
            var innerContainer = $('<div></div>');
            treeContainer.empty();
            treeContainer.append(innerContainer);

            fileBrowser = new FileBrowser({container: innerContainer, application: app, fetchFiles: false});
            fileBrowser.render();
            this._fileBrowser = fileBrowser;

                //Gets the selected location from tree and sets the value as location
            this.listenTo(fileBrowser, 'selected', function (selectedLocation) {
                if (selectedLocation) {
                    errorsContainer.hide();
                    location.val(selectedLocation);
                }
            });

            openFolderModal.find("button").filter(_.get(options, 'submit_button')).click(function() {
                self.openFolder(openFolderModal);
            });

            openFolderModal.keyup(function(e) {
                if (e.keyCode == 13) {
                    self.openFolder(openFolderModal);
                } else {}
            });

            this._dialogContainer.append(openFolderModal);
            errorsContainer.hide();
            this._errorsContainer = errorsContainer;
            this._modalContainer = openFolderModal;
        }
    });

export default FolderOpenDialog;
