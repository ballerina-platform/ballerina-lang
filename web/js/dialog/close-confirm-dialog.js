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

define(['jquery', './modal-dialog'], function ($, ModalDialog) {

    var CloseConfirmDialog = function (options) {
        this._options = options;
        this._$container = $(_.get(options, 'container', 'body'));
        this._initialized = false;
    };

    CloseConfirmDialog.prototype = Object.create(ModalDialog.prototype);
    CloseConfirmDialog.prototype.constructor = CloseConfirmDialog;

    CloseConfirmDialog.prototype.init = function () {
        if(this._initialized) {
            return;
        }

        var dontSaveBtn = $("<button type='button' class='btn btn-primary btn-file-dialog'>Don't Save</button>");
        this._saveBtn = this.getSubmitBtn();
        this._dontSaveBtn = dontSaveBtn;
        this._saveBtn.after(dontSaveBtn);
        this.setSubmitBtnText("Save");

        this._initialized = true;
    }

    CloseConfirmDialog.prototype.askConfirmation = function (options) {
        var self = this;
        this.init();

        var name = options.file.getName();
        this.setTitle(name + ' contains changes. Do you want to save them before closing?')

        this._saveBtn.unbind('click');
        this._dontSaveBtn.unbind('click');

        this._saveBtn.click(function(e){
            if(_.has(options, 'handleConfirm')){
                self.hide();
                options.handleConfirm(true);
            }
        });

        this._dontSaveBtn.click(function(e){
            if(_.has(options, 'handleConfirm')){
                self.hide();
                options.handleConfirm(false);
            }
        });

        this.show();
    }

    return CloseConfirmDialog;
});
