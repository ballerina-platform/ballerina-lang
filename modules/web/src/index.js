
import log from 'log';
import _ from 'lodash';
import { fetchConfigs } from 'api-client/api-client';
import Application from './core/app';
import defaultConfig from './config';

// importing for side effects only
import 'jquery-ui/ui/widgets/draggable';
import 'jquery-ui/themes/base/draggable.css';
import 'mcustom_scroller/jquery.mCustomScrollbar.css';
import 'bootstrap';
import 'theme_wso2';
import '../css/jstree.css';


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