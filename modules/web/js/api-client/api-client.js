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
        content: content,
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
 * Invoke packages service for the given file
 * and returns a promise with packages
 * @param {File} file
 */
export function getProgramPackages(file) {
    const payload = {
        fileName: file.getName(),
        filePath: file.getPath(),
        packageName: file.getPackageName(),
        content: file.getContent(),
    };
    const endpoint = getServiceEndpoint('programPackages');
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

/**
 * Gives the endpoint for a paticular backend service
 * 
 * @param {string} serviceName Name of the service
 */
export function getServiceEndpoint(serviceName) {
    return endpoints[serviceName].endpoint;
}
