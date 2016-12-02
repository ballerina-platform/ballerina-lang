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
define(['log', 'lodash', 'event_channel', './service-definition'],

    function(log, _, EventChannel, ServiceDefinition){

    var Package = function(args){
        this.addServiceDefinitions(_.get(args, 'serviceDefinitions', []));
        this._functionDefinitions = _.get(args, 'functionDefinitions', []);
        this._connectorDefinitions = _.get(args, 'connectorDefinitions', []);
        this._typeDefinitions = _.get(args, 'typeDefinitions', []);
        this._typeConverterDefinitions = _.get(args, 'typeConverterDefinitions', []);
        this._constantDefinitions = _.get(args, 'constantDefinitions', []);
    };

    Package.prototype = Object.create(EventChannel.prototype);
    Package.prototype.constructor = Package;

    /**
     * Add service defs
     * @param serviceDefinitions - can be an array of serviceDefs or a single serviceDef
     */
    Package.prototype.addServiceDefinitions = function(serviceDefinitions){
        var err;
        if(!_.isArray(serviceDefinitions) && !(serviceDefinitions instanceof  ServiceDefinition)){
            err = "Adding service def failed. Not an instance of ServiceDefinition" + serviceDefinitions;
            log.error(err);
            throw err;
        }
        if(_.isArray(serviceDefinitions)){
            if(!_.isEmpty(serviceDefinitions)){
                _.each(serviceDefinitions, function(serviceDefinition){
                    if(!(serviceDefinition instanceof  ServiceDefinition)){
                        err = "Adding service def failed. Not an instance of ServiceDefinition" + serviceDefinition;
                        log.error(err);
                        throw err;
                    }
                });
            }
        }
        this._serviceDefinitions = this._serviceDefinitions || [];
        _.concat(this._serviceDefinitions , serviceDefinitions);
        /**
         * fired when new service defs are added to the package.
         * @event Package#service-defs-added
         * @type {[ServiceDefinition]}
         */
        this.trigger("service-defs-added", serviceDefinitions);
    };

    /**
     * Set service defs
     *
     * @param serviceDefs
     */
    Package.prototype.setServiceDefinitions = function(serviceDefs){
        this._serviceDefinitions = null;
        this.addServiceDefinitions(serviceDefs);
    };

    /**
     *
     * @returns {[ServiceDefinition]}
     */
    Package.prototype.getServiceDefinitions = function() {
        return this._serviceDefinitions;
    }

});