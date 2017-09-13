import { getServiceEndpoint, getPathSeperator } from 'api-client/api-client';
import _ from 'lodash';
import axios from 'axios';
import File from './model/file';

const COMMON_HEADERS = {
    'content-type': 'text/plain; charset=utf-8',
};

const WORKSPACE_SERVICE = 'workspace';

/**
 * Reads a file from file system.
 *
 * @param {String} filePath Path of the file
 * @returns {Promise} Resolves {File} or reject with error.
 */
export function read(filePath) {
    const serviceEP = `${getServiceEndpoint(WORKSPACE_SERVICE)}/read`;
    return new Promise((resolve, reject) => {
        axios.post(serviceEP, filePath, { headers: COMMON_HEADERS })
            .then((response) => {
                const { content } = response.data;
                const pathArray = _.split(filePath, getPathSeperator());
                const name = _.last(pathArray);
                const path = _.join(_.take(pathArray, pathArray.length - 1), getPathSeperator())
                                    + getPathSeperator();
                const fullPath = filePath;
                resolve(new File({ content, name, fullPath, path, isPersisted: true, isDirty: false }));
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
