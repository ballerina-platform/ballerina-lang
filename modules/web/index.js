// provide global jquery object because wso2 theme expects it
window.$ = window.jQuery = require('jquery');
window.simulate = window.jQuerySimulate = require('./lib/jquery-simulate-1.0.0/jquery.simulate.js');
window.scope = window.location.pathname;

const log = require('log').default;
const _ = require('lodash');
const { fetchConfigs } = require('./js/api-client/api-client');
const Application = require('./js/app').default;
const config = require('./config').default;

// Before start rendering, fetch api endpoint information & other configs from config service
fetchConfigs()
    .then((configs) => {
        // merge existing config and received endpoint configs
        const newConfig = _.merge(configs, config);
        try {
            const app = new Application(newConfig);
            app.render();
            app.displayInitialView();
            window.composer = app;
        } catch (ex) {
            throw Error(ex.message + '. ' + ex.stack);
        }
        return Promise.resolve();
    })
    .catch(error => log.error('Error while starting app. ' + error.message));

