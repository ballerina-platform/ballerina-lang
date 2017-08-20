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
import axios from 'axios';
import { getLangServerClientInstance } from './../langserver/lang-server-client-controller';

// updating this with endpoints upon initial fetchConfigs()
let endpoints = {};

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
    if (PRODUCTION !== undefined && PRODUCTION) {
        configUrl = '/config';
    } else {
        // following is to support development mode where the config service is on 9091
        configUrl = 'http://localhost:9091/config';
    }
    return new Promise((resolve, reject) => {
        axios(configUrl)
            .then((response) => {
                endpoints = response.data.services;
                resolve(response.data);
            }).catch(error => reject(error));
    });
}

/**
 * Invoke validate service for the given file
 * and returns a promise with found errors
 * @param {File} file
 */
export function validateFile(file) {
    const payload = {
        fileName: file.getName(),
        filePath: file.getPath(),
        packageName: file.getPackageName(),
        content: file.getContent(),
    };
    const endpoint = getServiceEndpoint('validator');
    const headers = {
        'content-type': 'application/json; charset=utf-8',
    };

    return new Promise((resolve, reject) => {
        axios.post(endpoint, payload, { headers })
            .then((response) => {
                resolve(response.data.errors);
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
        fileName: file.getName(),
        filePath: file.getPath(),
        packageName: file.getPackageName(),
        content: file.getContent(),
    };
    const endpoint = getServiceEndpoint('parser');
    const headers = {
        'content-type': 'application/json; charset=utf-8',
    };

    return new Promise((resolve, reject) => {
        axios.post(endpoint, payload, { headers })
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
        content,
    };
    const endpoint = getServiceEndpoint('parser');
    const headers = {
        'content-type': 'application/json; charset=utf-8',
    };

    return new Promise((resolve, reject) => {
        axios.post(endpoint, payload, { headers })
            .then((response) => {
                resolve(response.data);
            }).catch(error => reject(error));
    });
}

/**
 * Returns a promise with program packages of the given file
 * 
 * @param {File} file
 */
export function getProgramPackages(file) {
    const fileOptions = {
        fileName: file.getName(),
        filePath: file.getPath(),
        packageName: file.getPackageName(),
        content: file.getContent(),
        isDirty: file.isDirty(),
    };

    return new Promise((resolve, reject) => {
        getLangServerClientInstance()
            .then((langserverClient) => {
                langserverClient.getProgramPackages(fileOptions, (data) => {
                    resolve(data);
                });
            })
            .catch(error => reject(error));
    });
}

/**
 * Returns a promise that resolves built in packages
 */
export function getBuiltInPackages() {
    return new Promise((resolve, reject) => {
        getLangServerClientInstance()
            .then((langserverClient) => {
                langserverClient.getBuiltInPackages()
                    .then((data) => {
                        if (!data.error && data.result) {
                            resolve(data.result.packages);
                        } else {
                            reject(data);
                        }
                    })
                    .catch(reject);
            })
            .catch(reject);
    });
}

/**
 * Invoke packages service and returns a promise with available packages
 */
export function getPackages() {
    const endpoint = getServiceEndpoint('packages');
    const headers = {
        'content-type': 'application/json; charset=utf-8',
    };

    return new Promise((resolve, reject) => {
        axios.get(endpoint, { headers })
            .then((response) => {
                resolve(response.data);
            }).catch(error => reject(error));
    });
}

export function getSwaggerDefinition(ballerinaSource, serviceName) {
    const endpoint = `${getServiceEndpoint('swagger')}/ballerina-to-swagger?serviceName=${serviceName}`;
    const headers = {
        'content-type': 'application/json; charset=utf-8',
    };
    const payload = {
        ballerinaDefinition: ballerinaSource,
    };

    return new Promise((resolve, reject) => {
        axios.post(endpoint, payload, { headers })
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
    const endpoint = getServiceEndpoint('typeLattice');
    const headers = {
        'content-type': 'application/json; charset=utf-8',
    };

    return new Promise((resolve, reject) => {
        axios.get(endpoint, { headers })
            .then((response) => {
                resolve(response.data);
            }).catch(error => reject(error));
    });
}
