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

define(['require', 'jquery', 'lodash', './modal-dialog', 'log'], function (require, $, _, ModalDialog, log) {

    var NewItemDialog = function (options) {
        _.set(options, 'class', 'create-new-item-wizard');
        ModalDialog.call(this, options);
        this._serviceClient = _.get(options, 'application.workspaceManager').getServiceClient();
    };

    NewItemDialog.prototype = Object.create(ModalDialog.prototype);
    NewItemDialog.prototype.constructor = NewItemDialog;

    NewItemDialog.prototype.onSubmit = function (data, itemName) {
        var app = this._options.application,
            path = data.path + app.getPathSeperator() + itemName,
            existsResponse = this._serviceClient.exists(path);
        if (existsResponse.exists) {
            this.showError(itemName + " already exists at " + data.path);
        } else {
            this.clearError();
            var response = this._serviceClient.create(path, data.type);
            if (response.error) {
                this.showError(response.message);
            } else {
                this.hide();
                var successCallBack = _.get(data, 'onSuccess');
                if(_.isFunction(successCallBack)){
                    successCallBack.call();
                }
                log.debug('file' + path + " created successfully");
                if(!_.isEqual('folder', data.type)){
                    var file = this._serviceClient.readFile(path);
                    app.commandManager.dispatch("create-new-tab", {tabOptions: {file: file}});
                }
            }
        }
    };

    NewItemDialog.prototype.displayWizard = function (data) {
        this.setTitle("new "+ data.type);
        this.setSubmitBtnText("create");
        var body = this.getBody();
        body.empty();
        this.getSubmitBtn().unbind('click');
        this.clearError();
        var modalBody = $("<hr class='file-dialog-hr'>"+
                            "<div class='container-fluid'>" +
                            "<form class='form-horizontal' onsubmit='return false'>" +
                                "<div class='form-group'>" +
                                    "<label for='item-name' class='col-sm-2 file-dialog-form-label'>Enter Name</label>" +
                                    "<div class='file-dialog-input-field'>" +
                                          "<input type='text' id='item-name' class='file-dialog-form-control item-name' placeholder='name'>" +
                                    "</div>" +
                                "</div>"+
                            "</form>"+
                           "</div>" );
        body.append(modalBody);
        this.show();
        var self = this,
            input = modalBody.find('input');

        this.on('loaded', function(){
            input.focus();
        });

        this.getSubmitBtn().click(function(e){
            self.onSubmit(data, input.val());
        });
        input.keyup(function(e){
            if(e.keyCode == 13) {
               self.onSubmit(data, input.val());
            } else {
                self.clearError();
            }
        });
    };



    return NewItemDialog;
});