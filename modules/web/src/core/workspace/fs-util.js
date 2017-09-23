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

const WORKSPACE_SERVICE = 'workspace';

/**
 * Reads a file from file system.
 *
 * @param {String} targetFilePath Complete path of the file
 * @returns {Promise} Resolves {File} or reject with error.
 */
export function read(targetFilePath) {
    const serviceEP = `${getServiceEndpoint(WORKSPACE_SERVICE)}/read`;
    return new Promise((resolve, reject) => {
        axios.post(serviceEP, targetFilePath, { headers: COMMON_HEADERS })
            .then((response) => {
                const { fileContent, fileName, filePath, fileFullPath, extension } = response.data;
                const name = fileName;
                const path = filePath;
                const fullPath = fileFullPath;
                const content = fileContent;
                resolve(new File({ content, name, fullPath, path, extension, isPersisted: true, isDirty: false }));
            }).catch(error => reject(error));
    });
}


/**
 * Update the given file with new content.
 *
 * @param {String} path Path of the folder
 * @param {String} name Name of the file
 * @param {String} content Content of the file
 *
 * @returns {Promise} Resolves file path or reject with error.
 */
export function update(path, name, content) {
    const serviceEP = `${getServiceEndpoint(WORKSPACE_SERVICE)}/write`;
    // FIXME: Refactor backend params
    const data = `location=${btoa(path)}&configName=${btoa(name)}&config=${
                            encodeURIComponent(content)}`;
    return new Promise((resolve, reject) => {
        axios.post(serviceEP, data, { headers: COMMON_HEADERS })
            .then((response) => {
                resolve(true);
            }).catch(error => reject(error));
    });
}

/**
 * Removes given file/folder from file system.
 *
 * @param {String} filePath Path of the file/folder
 * @returns {Promise} Resolves removed file path or reject with error.
 */
export function remove(filePath) {
    return new Promise();
}


/**
 * Creates given file/folder in file system.
 *
 * @param {String} filePath Path of the file/folder
 * @returns {Promise} Resolves created file path or reject with error.
 */
export function create(filePath) {
    return new Promise();
}
