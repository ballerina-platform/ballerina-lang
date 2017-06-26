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
import _ from 'lodash';
import ModalDialog from './modal-dialog';
import './dialog.css';

class SwitchToSourceViewConfirmDialog extends ModalDialog {
    constructor(options) {
        super();
    }

    init() {
        const continueBtn = $("<button type='button' class='btn btn-primary'>Continue</button>");
        const cancelBtn = $("<button type='button' class='btn btn-default'" +
                          " data-dismiss='modal'>Cancel</button>");
        this._continueBtn = continueBtn;
        this._cancelBtn = cancelBtn;

        this.getFooter().empty();
        this.getFooter().append(continueBtn, cancelBtn);
        this._$modalContainer.addClass('switch-to-source-confirm-dialog');
    }

    askConfirmation(action, handleConfirm) {
        this.setTitle('Switch to Source View?');

        const line1 = `You are going to ${action} a change done in source view.`;
        const line2 = 'To proceed, we need to switch to source view. Please confirm';
        const body = this.getBody();
        body.empty();
        body.append($(`<p><br>${line1}<br>${line2}</p>`));

        this._continueBtn.unbind('click');
        this._cancelBtn.unbind('click');

        this._continueBtn.click((e) => {
            if (_.isFunction(handleConfirm)) {
                this.hide();
                handleConfirm(true);
            }
        });

        this._cancelBtn.click((e) => {
            if (_.isFunction(handleConfirm)) {
                this.hide();
                handleConfirm(false);
            }
        });

        this.show();
    }
}
// create singleton
const dialog = new SwitchToSourceViewConfirmDialog();
dialog.render();
dialog.init();

export default dialog;
