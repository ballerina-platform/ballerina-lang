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

define(['require', 'jquery', 'event_channel', 'bootstrap'], function (require, $, EventChannel) {

    var ModalDialog = function (options) {
        this._options = options;
        this._application = _.get(options, "application");
        this._$container = $(_.get(options, 'container', 'body'));
    };

    ModalDialog.prototype = Object.create(EventChannel.prototype);
    ModalDialog.prototype.constructor = ModalDialog;

    ModalDialog.prototype.remove = function () {
        this._modalContainer.remove();
        this.off();
    };

    ModalDialog.prototype.getBody = function () {
        return this._$body;
    };

    ModalDialog.prototype.getHeader = function () {
        return this._$header;
    };

    ModalDialog.prototype.getFooter = function () {
        return this._$footer;
    };

    ModalDialog.prototype.getSubmitBtn = function () {
        return this._$submitBtn;
    };

    ModalDialog.prototype.getErrorContainer = function () {
        return this._$errorContainer;
    };

    ModalDialog.prototype.setTitle = function (title) {
        this._$title.text(title);
    };

    ModalDialog.prototype.setSubmitBtnText = function (text) {
        this._$submitBtn.text(text);
    };

    ModalDialog.prototype.setCloseBtnText = function (text) {
        this._$closeBtn.text(text);
    };

    ModalDialog.prototype.show = function () {
        var self = this;
        this._$modalContainer.modal('show').on('shown.bs.modal', function(){
            self.trigger('loaded');
        });
    };

    ModalDialog.prototype.showError = function (error) {
       this.getErrorContainer().text(error);
       this.getErrorContainer().show();
    };

    ModalDialog.prototype.clearError = function () {
       this.getErrorContainer().text("");
       this.getErrorContainer().hide();
    };

    ModalDialog.prototype.hide = function () {
        var self = this;
        this._$modalContainer.modal('hide').on('hidden.bs.modal', function(){
            self.trigger('unloaded');
        });
    };


    ModalDialog.prototype.render = function () {
        if(!_.isNil(this._$modalContainer)){
            this._$modalContainer.remove();
        }
        var modalContainer = $("<div class='modal fade " + _.get(this._options, 'class') +
            "' tabindex='-1' role='dialog' aria-hidden='true'></div>");
        var modalDialog = $("<div class='modal-dialog file-dialog' role='document'></div>");
        var modalContent = $("<div class='modal-content'></div>");
        var modalHeader = $("<div class='modal-header'></div>");
        var modalCloseBtnTop = $("<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
            "<span aria-hidden='true'>&times;</span></button>");
        var modalTitle = $("<h4 class='modal-title '></h4>");
        var modalBody = $("<div class='modal-body'></div>");
        var modalFooter = $("<div class='modal-footer'></div>");
        var modalCloseBtnBottom = $("<button type='button' class='btn btn-default btn-file-dialog'" +
            " data-dismiss='modal'>Close</button>");
        var modalSubmitBtn = $("<button type='button' class='btn btn-primary btn-file-dialog'>Submit</button>");
        var errorContainer = $("<div class='alert alert-danger errors-container'></div>");

        this._$title = modalTitle;
        this._$body = modalBody;
        this._$header = modalHeader;
        this._$footer = modalFooter;
        this._$submitBtn = modalSubmitBtn;
        this._$closeBtn = modalCloseBtnBottom;
        this._$modalContainer = modalContainer;
        this._$errorContainer = errorContainer;

        modalContainer.append(modalDialog);
        modalDialog.append(modalContent);
        modalContent.append(modalHeader);
        modalHeader.append(modalCloseBtnTop);
        modalHeader.append(modalTitle);
        modalContent.append(modalBody);
        modalContent.append(errorContainer);
        errorContainer.hide();
        modalContent.append(modalFooter);
        modalFooter.append(modalSubmitBtn);
        modalFooter.append(modalCloseBtnBottom);
        this._$container.append(modalContainer);

        if(_.has(this._options, 'title')){
            modalTitle.text(_.get(this._options, 'title'));
        }
        if(_.has(this._options, 'submitBtnText')){
            modalSubmitBtn.text(_.get(this._options, 'submitBtnText'));
        }
        return modalContainer;
    };

    return ModalDialog;
});