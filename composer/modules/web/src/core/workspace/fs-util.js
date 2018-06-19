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

import { getServiceEndpoint } from 'api-client/api-client';
import axios from 'axios';
import File from './model/file';

const CONTENT_TYPE_JSON_HEADER = {
    'content-type': 'application/json;charset=utf-8',
};

const FS_SERVICE = 'filesystem';

const derriveErrorMsg = (error) => {
    let errMsg = error && error.message ? error.message : 'Unknown error while fs operation call.';
    const response = error.response;
    if (response && response.data) {
        const respData = response.data;
        if (respData.Error) {
            errMsg = respData.Error;
        } else if (respData.error) {
            errMsg = respData.error;
        }
    }
    throw Error(errMsg);
};

/**
 * Reads a file from file system.
 *
 * @param {String} targetFilePath Complete path of the file
 * @returns {Promise} Resolves {File} or reject with error.
 */
export function read(targetFilePath) {
    const serviceEP = `${getServiceEndpoint(FS_SERVICE)}/read`;
    const data = {
        path: targetFilePath,
    };
    return axios.post(serviceEP, data, { headers: CONTENT_TYPE_JSON_HEADER })
                .then((response) => {
                    const { fileContent, fileName, filePath, fileFullPath, extension } = response.data;
                    const name = fileName;
                    const path = filePath;
                    const fullPath = fileFullPath;
                    const content = fileContent;
                    return new File({
                        content,
                        name,
                        fullPath,
                        path,
                        extension,
                        isPersisted: true,
                        isDirty: false,
                    });
                }).catch(derriveErrorMsg);
}

/**
 * Update the given file with new content.
 *
 * @param {String} path Path of the folder
 * @param {String} name Name of the file
 * @param {String} content Content of the file
 * @param {Boolean} isBase64Encoded is content to be sent encoded in base64.
 *
 * @returns {Promise} Resolves file path or reject with error.
 */
export function createOrUpdate(path, name, content, isBase64Encoded) {
    const serviceEP = `${getServiceEndpoint(FS_SERVICE)}/write`;
    const data = {
        path,
        name,
        content,
        isBase64Encoded,
    };
    return axios.post(serviceEP, data, { headers: CONTENT_TYPE_JSON_HEADER })
        .then((response) => {
            return response.data;
        })
        .catch(derriveErrorMsg);
}


/**
 * Removes given file/folder from file system.
 *
 * @param {String} path Path of the file/folder
 * @returns {Promise} Resolves status or reject with error.
 */
export function remove(path) {
    const serviceEP = `${getServiceEndpoint(FS_SERVICE)}/delete`;
    const data = {
        path,
    };
    return axios.post(serviceEP, data, { headers: CONTENT_TYPE_JSON_HEADER })
        .then((response) => {
            return response.data;
        }).catch(derriveErrorMsg);
}


/**
 * Creates given file/folder in file system.
 *
 * @param {String} fullPath Path of the file/folder
 * @param {String} type file or folder
 * @param {String} content file content - if creating a file
 *
 * @returns {Promise} Resolves created file path or reject with error.
 */
export function create(fullPath, type, content) {
    const serviceEP = `${getServiceEndpoint(FS_SERVICE)}/create`;
    const data = {
        fullPath,
        type,
        content,
    };
    return axios.post(serviceEP, data, { headers: CONTENT_TYPE_JSON_HEADER })
        .then((response) => {
            return response.data;
        }).catch(derriveErrorMsg);
}


/**
 * Moves given file/folder in file system to given destination.
 *
 * @param {String} srcPath Path of the source file/folder
 * @param {String} destPath Path of the destination file/folder
 *
 * @returns {Promise} Resolves status or reject with error.
 */
export function move(srcPath, destPath) {
    const serviceEP = `${getServiceEndpoint(FS_SERVICE)}/move`;
    const data = {
        srcPath,
        destPath,
    };
    return axios.post(serviceEP, data, { headers: CONTENT_TYPE_JSON_HEADER })
            .then((response) => {
                return response.data;
            }).catch(derriveErrorMsg);
}

/**
 * Copies given file/folder in file system to given destination.
 *
 * @param {String} srcPath Path of the source file/folder
 * @param {String} destPath Path of the destination file/folder
 *
 * @returns {Promise} Resolves status or reject with error.
 */
export function copy(srcPath, destPath) {
    const serviceEP = `${getServiceEndpoint(FS_SERVICE)}/copy`;
    const data = {
        srcPath,
        destPath,
    };
    return axios.post(serviceEP, data, { headers: CONTENT_TYPE_JSON_HEADER })
            .then((response) => {
                return response.data;
            }).catch(derriveErrorMsg);
}

/**
 * Check whether the file/folder exists
 *
 * @returns {Promise} Resolves boolean file exists
 */
export function exists(path) {
    const endpoint = `${getServiceEndpoint(FS_SERVICE)}/exists`;
    const data = {
        path,
    };
    return axios.post(endpoint, data, { headers: CONTENT_TYPE_JSON_HEADER })
            .then((response) => {
                return response.data;
            }).catch(derriveErrorMsg);
}


/**
 * Create project
 *
 * @returns {Promise} Resolves status or reject with error.
 */
export function createProject(path) {
    const endpoint = `${getServiceEndpoint(FS_SERVICE)}/project/create`;
    const data = {
        path,
    };
    return axios.post(endpoint, data, { headers: CONTENT_TYPE_JSON_HEADER })
                .then((response) => {
                    return response.data;
                }).catch(derriveErrorMsg);
}
