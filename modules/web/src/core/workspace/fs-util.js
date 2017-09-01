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
                const path = _.join(_.take(pathArray, pathArray.length - 1), getPathSeperator());
                resolve(new File({ content, name, path, isPersisted: true, isDirty: false }));
            }).catch(error => reject(error));
    });
}


/**
 * Update the given file with new content.
 *
 * @param {String} filePath Path of the file
 * @param {String} filePath Path of the file
 *
 * @returns {Promise} Resolves file path or reject with error.
 */
export function update(filePath, content) {
    return new Promise();
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
