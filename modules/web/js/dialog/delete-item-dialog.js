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
import log from 'log';
import './dialog.css';

class DeleteItemDialog extends ModalDialog {
    constructor(options) {
        _.set(options, 'class', 'delete-item-wizard');
        super(options);
        this._serviceClient = _.get(options, 'application.workspaceManager').getServiceClient();
    }

    onSubmit(data) {
        this.clearError();
        const response = this._serviceClient.delete(data.path, data.type);
        if (response.error) {
            this.showError(response.message);
        } else {
            this.hide();
            const successCallBack = _.get(data, 'onSuccess');
            if (_.isFunction(successCallBack)) {
                successCallBack.call();
            }
            log.debug(`${data.path} deleted successfully`);
        }
    }

    displayWizard(data) {
        this.setTitle(`Delete ${_.upperFirst(data.type)}`);
        this.setSubmitBtnText('delete');
        const body = this.getBody();
        body.empty();
        this.getSubmitBtn().unbind('click');
        this.clearError();
        const msgSuffix = _.isEqual(data.type, 'folder')
                        ? 'folder and its contents' : 'file';
        const modalBody =
            $(`<div class='delete-item-dialog'>
                   <div class='icon'>
                        <i class='fw fw-warning fw-5x'></i>
                   </div>
                   <div class='text'>
                        <h4>Warning! Are you sure you want to delete selected ${msgSuffix}?</h4>
                        <p>
                            Following ${data.type} will be deleted from file system</br>
                            ${data.path}
                        </p>
                   </div>
                </div>`);
        body.append(modalBody);

        this.show();
        const self = this;
        this.getSubmitBtn().click((e) => {
            self.onSubmit(data);
        });
    }
}

export default DeleteItemDialog;
