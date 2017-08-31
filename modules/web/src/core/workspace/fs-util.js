import { getServiceEndpoint } from 'api-client/api-client';

/**
 * Reads the content of given file.
 *
 * @param {String} filePath Path of the file
 * @returns {Promise} Resolves file content or reject with error.
 */
export function read(filePath) {
    return new Promise();
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
