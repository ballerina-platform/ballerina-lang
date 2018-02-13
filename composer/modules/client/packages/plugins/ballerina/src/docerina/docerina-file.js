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
import _ from 'lodash';
import { Log as log } from '@ballerina-lang/composer-core';
import $ from 'jquery';
import React from 'react';
import ReactDOM from 'react-dom';
import Document from './document';

/**
 * Docerina Documentation for particular given package.
 * @class DocerinaFile
 * */
class DocerinaFile {
    /**
     * Constructor for Docerina File class.
     * @param {object} args - argument to create the tab.
     * @constructor
     * @memberOf DocerinaFile
     * */
    constructor(args) {
        let errMsg;
        if (!_.has(args, 'tab')) {
            errMsg = 'unable to find a reference for Docerina tab';
            log.error(errMsg);
            throw errMsg;
        }
        this._tab = _.get(args, 'tab');
        const container = $(this._tab.getContentContainer());
        // make sure default tab content are cleared
        container.empty();
        // check whether container element exists in dom
        if (!container.length > 0) {
            errMsg = `unable to find container for Docerina Tab with selector: ${_.get(args, 'container')}`;
            log.error(errMsg);
            throw errMsg;
        }
        this._$parent_el = container;
        this._options = args;
    }

    /**
     * Render Docerina Documentation Tab.
     *
     * @memberOf DocerinaFile
     * */
    render() {
        const view = React.createElement(Document, {
            packageName: this._options.packageName,
            functionName: this._options.functionName,
        }, null);
        ReactDOM.render(view, this._$parent_el[0]);
    }
}

export default DocerinaFile;
