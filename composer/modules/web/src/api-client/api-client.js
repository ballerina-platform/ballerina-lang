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
import axios from 'axios';
import $ from 'jquery';
import hardcodedTypeLattice from './hardcoded-type-lattice';
import hardcodedOperatorLattice from './hardcoded-operator-lattice';

const CONTENT_TYPE_JSON_HEADER = {
    'content-type': 'application/json; charset=utf-8',
};

const CONTENT_TYPE_TEXT_PLAIN_HEADER = {
    'content-type': 'text/plain; charset=utf-8',
};

// updating this with endpoints upon initial fetchConfigs()
let endpoints = {};
let pathSeparator = '/'; // Setting default value as '/'. This value will get overriden at fetchConfigs().

/**
 * Gives the endpoint for a paticular backend service
 *
 * @param {string} serviceName Name of the service
 */
export function getServiceEndpoint(serviceName) {
    return endpoints[serviceName].endpoint;
}

/**
 * Fetch information about available API endpoints from config service
 * return A promise that resolves the get request
*/
export function fetchConfigs() {
    // PRODUCTION is a global variable set by webpack DefinePlugin
    // it will be set to "true" in the production build.
    let configUrl = '';
    /* eslint-disable no-undef */
    if (PRODUCTION !== undefined && PRODUCTION) {
    /* eslint-enable no-undef */
        configUrl = '/composer/config';
    } else {
        // following is to support development mode where the config service is on 9091
        configUrl = 'http://localhost:9091/composer/config';
    }
    return new Promise((resolve, reject) => {
        axios(configUrl)
            .then((response) => {
                endpoints = response.data.services;
                pathSeparator = response.data.pathSeparator;
                resolve(response.data);
            }).catch(error => reject(error));
    });
}

/**
 * Invoke parser service for the given file
 * and returns a promise with parsed json
 * @param {File} file
 */
export function parseFile(file) {
    const payload = {
        fileName: file.name + '.' + file.extension,
        filePath: file.path,
        packageName: file.packageName,
        content: file.content,
        includeTree: true,
        includePackageInfo: true,
        includeProgramDir: true,
    };
    const endpoint = getServiceEndpoint('ballerina-parser') + '/file/validate-and-parse';

    return new Promise((resolve, reject) => {
        axios.post(endpoint, payload, { headers: CONTENT_TYPE_JSON_HEADER })
            .then((response) => {
                resolve(response.data);
            }).catch(error => reject(error));
    });
}

/**
 * Invoke parser service for the given content
 * and returns a promise with parsed json
 * @param {string} content
 */
export function parseContent(content) {
    const payload = {
        fileName: 'untitle',
        filePath: '/temp',
        packageName: 'test.package',
        includeTree: true,
        includePackageInfo: true,
        content,
    };
    const endpoint = getServiceEndpoint('ballerina-parser') + '/file/validate-and-parse';

    return new Promise((resolve, reject) => {
        axios.post(endpoint, payload, { headers: CONTENT_TYPE_JSON_HEADER })
            .then((response) => {
                resolve(response.data);
            }).catch(error => reject(error));
    });
}

/**
 * Invoke packages service and returns a promise with available packages
 */
export function getPackages() {
    const endpoint = getServiceEndpoint('ballerina-parser') + '/built-in-packages';

    return new Promise((resolve, reject) => {
        axios.get(endpoint, { headers: CONTENT_TYPE_JSON_HEADER })
            .then((response) => {
                resolve(response.data);
            }).catch(error => reject(error));
    });
}

/**
 * Invoke parser service and returns a promise with available types
 */
export function getBuiltInTypes() {
    const endpoint = getServiceEndpoint('ballerina-parser') + '/built-in-types';

    return new Promise((resolve, reject) => {
        axios.get(endpoint, { headers: CONTENT_TYPE_JSON_HEADER })
            .then((response) => {
                resolve(response.data);
            }).catch(error => reject(error));
    });
}

/**
 * Get FS Roots
 */
export function getFSRoots(extensions) {
    const endpoint = `${getServiceEndpoint('filesystem')}/list/roots`;
    const data = {
        extensions: _.join(extensions, ','),
    };

    return new Promise((resolve, reject) => {
        axios.post(endpoint, data, { headers: CONTENT_TYPE_JSON_HEADER })
            .then((response) => {
                resolve(response.data);
            }).catch(error => reject(error));
    });
}

/**
 * Get File List
 */
export function listFiles(path, extensions) {
    const endpoint = `${getServiceEndpoint('filesystem')}/list/files`;
    const data = {
        path,
        extensions: _.join(extensions, ','),
    };

    return new Promise((resolve, reject) => {
        axios.post(endpoint, data, { headers: CONTENT_TYPE_JSON_HEADER })
            .then((response) => {
                resolve(response.data);
            }).catch(error => reject(error));
    });
}


export function getSwaggerDefinition(ballerinaSource, serviceName) {
    const endpoint = `${getServiceEndpoint('ballerina-to-swagger')}/ballerina-to-swagger?serviceName=${serviceName}`;
    const payload = {
        ballerinaDefinition: ballerinaSource,
    };

    return new Promise((resolve, reject) => {
        axios.post(endpoint, payload, { headers: CONTENT_TYPE_JSON_HEADER })
            .then((response) => {
                resolve(response.data.swaggerDefinition);
            }).catch(error => reject(error));
    });
}

/**
 * Get the type lattice
 * @export
 * @returns type lattice response
 */
export function getTypeLattice() {
    // const endpoint = getServiceEndpoint('typeLattice');
    // const headers = {
        // 'content-type': 'application/json; charset=utf-8',
    // };

    // Hard coding type lattice temporary
    return Promise.resolve(hardcodedTypeLattice);

    // TODO: Uncomment when typeLattice endpoint starts working
    // return new Promise((resolve, reject) => {
    //     axios.get(endpoint, { headers })
    //         .then((response) => {
    //             resolve(response.data);
    //         }).catch(error => reject(error));
    // });
}

/**
 * Get the type lattice
 * @export
 * @returns type lattice response
 */
export function getOperatorLattice() {
    // TODO: add operator lattice to endpoints
    // const endpoint = getServiceEndpoint('operatorLattice');
    // const headers = {
    //     'content-type': 'application/json; charset=utf-8',
    // };

    // Hard coding type lattice temporary
    return Promise.resolve(hardcodedOperatorLattice);

    // TODO: Uncomment when typeLattice endpoint starts working
    // return new Promise((resolve, reject) => {
    //     axios.get(endpoint, { headers })
    //         .then((response) => {
    //             resolve(response.data);
    //         }).catch(error => reject(error));
    // });
}

/**
 * parse fragment.
 *
 * @param {string} fragment - source fragment.
 * @return {object} fragment details to be sent to fragment parser.
 * */

// TODO: Use axios and Promises for api call
export function parseFragment(fragment) {
    let data = {};
    $.ajax({
        type: 'POST',
        context: this,
        url: getServiceEndpoint('ballerina-parser') + '/model/parse-fragment',
        data: JSON.stringify(fragment),
        contentType: 'application/json; charset=utf-8',
        async: false,
        dataType: 'json',
        success(response) {
            data = response;
        },
        error() {
            data = { error: 'Unable to call fragment parser Backend.' };
        },
    });
    return data;
}

/**
 * Returns native path seperator of backend
 */
export function getPathSeperator() {
    return pathSeparator;
}

/**
 * Invokes the try-it proxy.
 * @export
 * @param {Object} tryItPayload The request body.
 * @returns {Object} The response.
 */
export function invokeTryIt(tryItPayload, protocol) {
    const endpoint = getServiceEndpoint('try-it') + '/' + protocol;

    return new Promise((resolve, reject) => {
        axios.post(endpoint, tryItPayload, { headers: CONTENT_TYPE_TEXT_PLAIN_HEADER })
            .then((response) => {
                resolve(response.data);
            }).catch(error => reject(error));
    });
}

/**
 * Get the url used for try-it executions.
 * @export
 * @returns {Object} The object.
 */
export function getTryItUrl() {
    const endpoint = `${getServiceEndpoint('try-it')}/url`;
    return new Promise((resolve, reject) => {
        axios.get(endpoint, {})
            .then((response) => {
                resolve(response.data.urls);
            }).catch(error => reject(error));
    });
}

/**
 * Gets user home from backend
 *
 * @returns {Promise} Resolves string path
 */
export function getUserHome() {
    const endpoint = `${getServiceEndpoint('filesystem')}/user/home`;
    return new Promise((resolve, reject) => {
        axios.get(endpoint, {})
            .then((response) => {
                resolve(response.data);
            }).catch(error => reject(error));
    });
}
