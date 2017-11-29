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

import log from 'log';
import ReactUpdates from 'react-dom/lib/ReactUpdates';
import ReactDefaultBatchingStrategy from 'react-dom/lib/ReactDefaultBatchingStrategy';

// based on: https://engineering.classdojo.com/blog/2016/12/10/catching-react-errors/

let isHandlingError = false;
const ReactTryCatchBatchingStrategy = {
    // this is part of the BatchingStrategy API. simply pass along
    // what the default batching strategy would do.
    get isBatchingUpdates() { return ReactDefaultBatchingStrategy.isBatchingUpdates; },

    batchedUpdates(...args) {
        try {
            ReactDefaultBatchingStrategy.batchedUpdates(...args);
        } catch (e) {
            if (isHandlingError) {
                // our error handling code threw an error. just throw now
                throw e;
            }
            isHandlingError = true;
            try {
                log.error('Error while react render.', e);
            } finally {
                isHandlingError = false;
            }
        }
    },
};

ReactUpdates.injection.injectBatchingStrategy(ReactTryCatchBatchingStrategy);
