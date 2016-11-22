/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['jquery', 'lodash', 'backbone', 'log'], function ($, _, Backbone, log) {

    var File = Backbone.View.extend(
        {
            initialize: function (options) {
                var errMsg, fileMode, fileContent;
                // FIXME
                _.set(this, 'id', this.cid);
                if (!_.has(options, 'mode')){
                    errMsg = 'unable to find file mode ' + _.toString(options);
                    log.error(errMsg);
                    throw errMsg;
                }
                fileMode = $(_.get(options, 'mode')) || [];
                if (!_.has(options, 'content')){
                    errMsg = 'unable to find file content of mode ' + _.get(options, 'mode');
                    log.error(errMsg);
                    throw errMsg;
                }
                fileContent = $(_.get(options,'content')) || [];
                // file mode : browserStored/fileSystem
                this._mode = fileMode;
                this.options = options;
                this._content = fileContent;
            },

            updateFileMode: function(mode){
                this._mode = mode;
            }

        });

    return File;


});