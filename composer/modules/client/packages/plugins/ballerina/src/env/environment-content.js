/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import $ from 'jquery';

const environmentContent = {};

/**
 * Get the packages from the package list api.
 * */
environmentContent.getPackages = function (app) {
    const packageServiceURL = app.config.services.packages.endpoint;
    let data = [];

    // TODO: remove the following hard coded url and use a value from application config
    $.ajax({
        type: 'GET',
        url: packageServiceURL,
        contentType: 'application/json; charset=utf-8',
        async: false,
        dataType: 'json',
        success(response) {
            data = response;
        },
        error() {
            data = { error: true, message: 'Unable to retrieve packages.' };
        },
    });
    return data;
};

/**
 *  Get native types from the Ballerina program service
 */
environmentContent.getNativeTypes = function (app) {
    const programNativeTypesServiceURL = app.config.services.programNativeTypes.endpoint;
    let data = [];
    // TODO: remove the following hard coded url and use a value from application config
    $.ajax({
        type: 'GET',
        url: programNativeTypesServiceURL,
        contentType: 'application/json; charset=utf-8',
        async: false,
        dataType: 'json',
        success(response) {
            data = response;
        },
        error() {
            data = { error: true, message: 'Unable to retrieve native types.' };
        },
    });
    return data;
};
export default environmentContent;
