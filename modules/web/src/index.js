
import log from 'log';
import _ from 'lodash';
import { fetchConfigs } from 'api-client/api-client';
import Application from './core/app';
import defaultConfig from './config';

// importing for side effects only
import 'bootstrap';
import 'theme_wso2';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/css/bootstrap-theme.min.css';
import 'font-ballerina/css/font-ballerina.css';


// Before start rendering, fetch api endpoint information & other configs from config service
fetchConfigs()
    .then((configs) => {
        // merge existing config and received configs
        const newConfig = _.merge(defaultConfig, configs);
        try {
            const app = new Application(newConfig);
            app.render();
        } catch (ex) {
            throw Error(ex.message + '. ' + ex.stack);
        }
    })
    .catch(error => log.error('Error while starting app. ' + error.message));
