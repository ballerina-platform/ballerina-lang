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
 */
define(['log', 'lodash', 'jquery', 'event_channel'],
    function(log, _, $, EventChannel ) {

    /**
     * @class Backend
     * @param {Object} args - Arguments for creating the view.
     * @constructor
     */
    var Backend = function (args) {
        this._options = args;
        if(!_.has(args, 'url')){
            log.error('url is not given for backend.');
        }else{
            this._url = _.get(args, 'url');
        }        
    };

    /**
     * validate source
     * @param functionDefinition
     */
    Backend.prototype.parse = function (source) {
        var content = { "content": source };  
        var data = {};
        $.ajax({
            type: "POST",
            context: this,
            url: this._url,
            data: JSON.stringify(content),
            contentType: "application/json; charset=utf-8",
            async: false,
            dataType: "json",
            success: function (response) {
                data = response;
            },
            error: function(xhr, textStatus, errorThrown){
                data = {"error":true, "message":"Unable to render design view due to parser errors."};
            }
        });
        return data;
    };

    Backend.prototype.searchPackage = function(query, exclude){
        var packages = [{
                name: "org.wso2.http.OAuth",
                connectors: [
                    {
                        id: "oauth-connector",
                        name: "OAuthConnector",
                        icon: "images/tool-icons/http-connector.svg",
                        title: "OAuthConnector",
                        actions:[
                            {
                                id: "authorize",
                                name: "Authorize",
                                icon: "images/tool-icons/http.svg",
                                title: "Authorize",
                                meta: {
                                    action: "authorize"
                                },
                            }
                        ]
                    }
                ],
                functions: "",
                structs: ""
            },{
                name: "org.wso2.salesforce",
                connectors: [
                    {
                        id: "sf-connector",
                        name: "Salesforce",
                        icon: "images/tool-icons/http-connector.svg",
                        title: "Salesforce",
                        actions:[
                            {
                                id: "add",
                                name: "Add",
                                icon: "images/tool-icons/http.svg",
                                title: "Add",
                                meta: {
                                    action: "add"
                                },
                            }
                        ]
                    }
                ],
                functions: "",
                structs: ""
            }];

        var search_text = query;
        var exclude_packages = exclude;        
        var result = _.filter(packages, function(o){
            var existing = _.filter(exclude_packages,function(ex){ return o.name == ex; });
            return (existing.length == 0) && (_.includes(o.name.toUpperCase() , search_text.toUpperCase()));
        });
        return result;
    };

    return Backend;
});