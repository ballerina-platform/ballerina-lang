import axios from 'axios';

/**
 * Fetch information about available API endpoints from config service
 */
export function fetchAPIEndpointConfigs() {
    // PRODUCTION is a global variable set by webpack DefinePlugin
    // it will be set to "true" in the production build.
    let configUrl = '';
    if (PRODUCTION !== undefined && PRODUCTION) {
        configUrl = '/config';
    } else {
        // following is to support development mode where the config service is on 9091
        configUrl = 'http://localhost:9091/config';
    }
    return axios(configUrl);
}
