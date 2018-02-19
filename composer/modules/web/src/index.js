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
 *
 */

 // importing for side effects only
import 'font-ballerina/css/font-ballerina.css';

import log from 'log';
import _ from 'lodash';
import { fetchConfigs } from 'api-client/api-client';
import Application from './core/app';
import defaultConfig from './config';

import './ballerina-theme/semantic.less';

// Before start rendering, fetch api endpoint information & other configs from config service
fetchConfigs()
    .then((configs) => {
        // merge existing config and received configs
        const newConfig = _.merge(defaultConfig, configs);
        try {
            log.initAjaxAppender(newConfig.services.logging.endpoint);
            const app = new Application(newConfig);
            app.render();
        } catch (ex) {
            throw Error(ex.message + '. ' + ex.stack);
        }
    })
    .catch(error => log.error('Error while starting app. ' + error.message));
