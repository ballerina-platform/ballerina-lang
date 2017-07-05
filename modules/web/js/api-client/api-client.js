import axios from 'axios';
// updating this with endpoints upon initial fetchConfigs()
let endpoints = {};

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
 * Invoke parser service for the given file
 * and returns a promise with parsed json
 * @param {File} file
 */
export function parseFile(file) {
    const payload = {
        fileName: file.getName(),
        filePath: file.getPath(),
        packageName: 'test.package',
        content: file.getContent(),
    };
    const endpoint = endpoints.parser.endpoint;
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
