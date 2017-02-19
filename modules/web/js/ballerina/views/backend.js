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
                if (data.errorMessage) {
                    data = {"error":true, "message":data.errorMessage};
                }
            },
            error: function(xhr, textStatus, errorThrown){
                data = {"error":true, "message":"Unable to render design view due to parser errors."};
            }
        });
        return data;
    };

       /**
        * Does a backend call
        * @param uri resource path
        * @param method http method
        * @param content payload
        * @param queryParams query parameters in [{name: "foo", value: "bar"}, ...]
        */
       Backend.prototype.call = function (uri, method, content, queryParams) {
           var response = {};
           var queryParamsStr = "";
           if (queryParams) {
               try {
                   queryParamsStr = "?" + (queryParams.map(
                           function (elem) {
                               if (!elem.name || !elem.value) {
                                   throw new Error("Invalid query params!");
                               }
                               return encodeURIComponent(elem.name) + "=" + encodeURIComponent(elem.value);
                           }).join("&"));
               } catch (err) {
                   //do nothing
               }
           }

           $.ajax({
                      type: method,
                      context: this,
                      url: this._url + uri + queryParamsStr,
                      data: JSON.stringify(content),
                      contentType: "application/json; charset=utf-8",
                      async: false,
                      dataType: "json",
                      success: function (data) {
                          if(data.errorMessage){
                              response = {"error": true, "message": "Unable to parse source:" + data.errorMessage + "."};
                          } else {
                              response = data;
                          }
                      },
                      error: function (xhr, textStatus, errorThrown) {
                          response = {"error": true, "message": "Unable to render design view due to parser errors."};
                      }
                  });
           return response;
       };

       return Backend;
   });