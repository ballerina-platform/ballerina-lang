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

define(['require', 'jquery', 'lodash', './modal-dialog', 'alerts'], function (require, $, _, ModalDialog, alerts) {

    var NewItemDialog = function (options) {
        _.set(options, 'class', 'create-new-item-wizard');
        ModalDialog.call(this, options);
    };

    NewItemDialog.prototype = Object.create(ModalDialog.prototype);
    NewItemDialog.prototype.constructor = NewItemDialog;

    NewItemDialog.prototype.onSubmit = function () {
    };

    NewItemDialog.prototype.displayWizard = function (data) {
        this.setTitle("New "+ data.type);
        this.setSubmitBtnText("create");
        var body = this.getBody();
        body.empty();
        var modalBody = $("<div class='container-fluid'>" +
                                "<div class='form-group'>" +
                                    "<label for='item-name' class='col-sm-2 file-dialog-label'>Enter Name</label>" +
                                    "<div class='col-sm-9'>" +
                                          "<input type='text' id='item-name' class='file-dialog-form-control item-name' placeholder='name'>" +
                                    "</div>" +
                                "</div>"+
                           "</div>" );
        body.append(modalBody);
        this.show();
        var self = this,
            input = modalBody.find('input');

        this.on('loaded', function(){
            input.focus();
        });

        this.getSubmitBtn().click(function(e){
            self.onSubmit();
        });
        input.keyup(function(e){
            if(e.keyCode == 13)
            {
               self.onSubmit();
            }
        });

        this._itemNameInput = input;
    };



    return NewItemDialog;
});