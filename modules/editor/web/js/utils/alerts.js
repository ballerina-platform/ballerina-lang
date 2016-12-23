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
define(['log', 'jquery', 'lodash', 'backbone'], function (log, $, _, Backbone) {

    var AlertsManager = Backbone.View.extend(
    /** @lends AlertsManager.prototype */
    {
        /**
         * @augments Backbone.View
         * @constructs
         * @class AlertsManager handles application alerts to be displayed to user.
         * @param {Object} config configuration options
         */
        initialize: function (config) {
            if(_.has(config, 'container')){
                var alertArea = $(_.get(config, 'container'));
                if(alertArea.length > 0){
                    this._alertArea = alertArea;
                }
            }
            if(_.isUndefined(this._alertArea)){
                var error = "unable to initialize alerts manager with provided config: " + _.toString(config);
                log.error(error);
                throw  error;
            }

            // set default config
            if(!_.has(config, 'class.error')){
                _.set(config, 'class.error', 'alert-danger');
            }
            if(!_.has(config, 'class.warning')){
                _.set(config, 'class.warning', 'alert-warning');
            }
            if(!_.has(config, 'class.info')){
                _.set(config, 'class.info', 'alert-success');
            }
            if(!_.has(config, 'fadeTo.duration')){
                _.set(config, 'fadeTo.duration', 4000);
            }
            if(!_.has(config, 'fadeTo.opacity')){
                _.set(config, 'fadeTo.opacity', 500);
            }
            if(!_.has(config, 'slideUp.duration')){
                _.set(config, 'slideUp.duration', 500);
            }

            this._config = config;
        },

        alertInfo: function(message){
            var alertArea = this._alertArea;
            alertArea.text(msg);
            alertArea.removeClass(this._config.class.error);
            alertArea.removeClass(this._config.class.warning);
            alertArea.addClass(this._config.class.info);
            this.fadeAndSlideUp();
        },

        alertError: function(message){
            var alertArea = this._alertArea;
            alertArea.text(msg);
            alertArea.removeClass(this._config.class.info);
            alertArea.removeClass(this._config.class.warning);
            alertArea.addClass(this._config.class.error);
            this.fadeAndSlideUp();
        },

        alertWarning: function(message){
            var alertArea = this._alertArea;
            alertArea.text(msg);
            alertArea.removeClass(this._config.class.error);
            alertArea.removeClass(this._config.class.info);
            alertArea.addClass(this._config.class.warning);
            this.fadeAndSlideUp();
        },

        fadeAndSlideUp: function(){
            var self = this;
            this._alertArea.fadeTo(this._config.fadeTo.duration, this._config.fadeTo.opacity)
                .slideUp(this._config.slideUp.duration, function () {
                    self._alertArea.slideUp(self._config.slideUp.duration);
                });
        }

    });

    return AlertsManager;
});