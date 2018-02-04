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

const COMMON_HEADERS = {
    'content-type': 'text/plain; charset=utf-8',
};

const FORM_CONTENT_COMMON_HEADERS = {
    'content-type': 'application/x-www-form-urlencoded; charset=utf-8',
};

const FS_SERVICE = 'filesystem';

/**
 * Reads a file from file system.
 *
 * @param {String} targetFilePath Complete path of the file
 * @returns {Promise} Resolves {File} or reject with error.
 */
export function read(targetFilePath) {
    const serviceEP = `${getServiceEndpoint(FS_SERVICE)}/read`;
    return axios.post(serviceEP, targetFilePath, { headers: COMMON_HEADERS })
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
                });
}

/**
 * Update the given file with new content.
 *
 * @param {String} path Path of the folder
 * @param {String} name Name of the file
 * @param {String} content Content of the file
 * @param {Boolean} isCustomContent is content to be sent custom.
 *
 * @returns {Promise} Resolves file path or reject with error.
 */
export function createOrUpdate(path, name, content, isCustomContent) {
    const serviceEP = `${getServiceEndpoint(FS_SERVICE)}/write`;
    // FIXME: Refactor backend params
    const data = isCustomContent ? content : `location=${btoa(path)}&configName=${btoa(name)}&config=${
                            encodeURIComponent(content)}`;
    return axios.post(serviceEP, data, { headers: COMMON_HEADERS })
        .then((response) => {
            return response.data;
        });
}

/**
 * Removes given file/folder from file system.
 *
 * @param {String} path Path of the file/folder
 * @returns {Promise} Resolves status or reject with error.
 */
export function remove(path) {
    const serviceEP = `${getServiceEndpoint(FS_SERVICE)}/delete`;
    const data = `path=${btoa(path)}`;
    return axios.post(serviceEP, data, { headers: FORM_CONTENT_COMMON_HEADERS })
        .then((response) => {
            return response.data;
        });
}


/**
 * Creates given file/folder in file system.
 *
 * @param {String} path Path of the file/folder
 * @param {String} type file or folder
 * @param {String} content file content - if creating a file
 *
 * @returns {Promise} Resolves created file path or reject with error.
 */
export function create(path, type, content) {
    const serviceEP = `${getServiceEndpoint(FS_SERVICE)}/create`;
    // FIXME: Refactor backend params
    const data = `path=${btoa(path)}&type=${btoa(type)}&content=${btoa(content)}`;
    return axios.post(serviceEP, data, { headers: FORM_CONTENT_COMMON_HEADERS })
        .then((response) => {
            return response.data;
        });
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
    const data = `srcPath=${btoa(srcPath)}&destPath=${btoa(destPath)}`;
    return axios.post(serviceEP, data, { headers: FORM_CONTENT_COMMON_HEADERS })
            .then((response) => {
                return response.data;
            });
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
    const data = `srcPath=${btoa(srcPath)}&destPath=${btoa(destPath)}`;
    return axios.post(serviceEP, data, { headers: FORM_CONTENT_COMMON_HEADERS })
            .then((response) => {
                return response.data;
            });
}

/**
 * Check whether the file/folder exists
 *
 * @returns {Promise} Resolves boolean file exists
 */
export function exists(path) {
    const endpoint = `${getServiceEndpoint(FS_SERVICE)}/exists?path=${btoa(path)}`;
    return new Promise((resolve, reject) => {
        axios.get(endpoint, { headers: COMMON_HEADERS })
            .then((response) => {
                resolve(response.data);
            }).catch(error => reject(error));
    });
}
