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
import EventChannel from 'event_channel';
import 'bootstrap';
import './dialog.css';

class ModalDialog extends EventChannel {
    constructor(options) {
        super();
        this._options = options;
        this._application = _.get(options, 'application');
        this._$container = $(_.get(options, 'container', 'body'));
    }

    remove() {
        this._modalContainer.remove();
        this.off();
    }

    getBody() {
        return this._$body;
    }

    getHeader() {
        return this._$header;
    }

    getFooter() {
        return this._$footer;
    }

    getSubmitBtn() {
        return this._$submitBtn;
    }

    getErrorContainer() {
        return this._$errorContainer;
    }

    setTitle(title) {
        this._$title.text(title);
    }

    setSubmitBtnText(text) {
        this._$submitBtn.text(text);
    }

    setCloseBtnText(text) {
        this._$closeBtn.text(text);
    }

    show() {
        const self = this;
        this._$modalContainer.modal('show').on('shown.bs.modal', () => {
            self.trigger('loaded');
        });
    }

    showError(error) {
        this.getErrorContainer().text(error);
        this.getErrorContainer().show();
    }

    clearError() {
        this.getErrorContainer().text('');
        this.getErrorContainer().hide();
    }

    hide() {
        const self = this;
        this._$modalContainer.modal('hide').on('hidden.bs.modal', () => {
            self.trigger('unloaded');
        });
    }

    render() {
        if (!_.isNil(this._$modalContainer)) {
            this._$modalContainer.remove();
        }
        const modalContainer = $(`<div class='modal fade ${_.get(this._options, 'class')
            }' tabindex='-1' role='dialog' aria-hidden='true'></div>`);
        const modalDialog = $("<div class='modal-dialog file-dialog' role='document'></div>");
        const modalContent = $("<div class='modal-content'></div>");
        const modalHeader = $("<div class='modal-header'></div>");
        const modalCloseBtnTop = $("<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
            "<span aria-hidden='true'>&times;</span></button>");
        const modalTitle = $("<h4 class='modal-title '></h4>");
        const modalBody = $("<div class='modal-body'></div>");
        const modalFooter = $("<div class='modal-footer'></div>");
        const modalCloseBtnBottom = $("<button type='button' class='btn btn-default btn-file-dialog'" +
            " data-dismiss='modal'>Close</button>");
        const modalSubmitBtn = $("<button type='button' class='btn btn-primary btn-file-dialog'>Submit</button>");
        const errorContainer = $("<div class='alert alert-danger errors-container'></div>");

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

        if (_.has(this._options, 'title')) {
            modalTitle.text(_.get(this._options, 'title'));
        }
        if (_.has(this._options, 'submitBtnText')) {
            modalSubmitBtn.text(_.get(this._options, 'submitBtnText'));
        }
        return modalContainer;
    }
}

export default ModalDialog;
