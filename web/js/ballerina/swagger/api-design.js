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
define(['log', 'lodash', 'jquery', '../views/backend'],
       function (log, _, $, Backend) {

           var instance;
           /**
            * @class APIDesigner
            * @constructor
            * @class APIDesigner
            */
           var APIDesigner = function (args) {
               if (!_.has(args, 'apiDoc')) {
                   log.error("apiDoc is not defined.");
                   //continue with defaults
                   this._apiDoc = {
                       "swagger": "2.0",
                       "info": {
                           "version": "1.0.0",
                           "title": "Ballerina Editor Default API",
                           "description": "This specifies a **RESTful API** for **Ballerina** - Editor which is written using [swagger 2.0](http://swagger.io/) specification.\n",
                           "contact": {
                               "name": "WSO2",
                               "url": "http://ballerina.io",
                               "email": "architecture@wso2.com"
                           },
                           "license": {
                               "name": "Apache 2.0",
                               "url": "http://www.apache.org/licenses/LICENSE-2.0.html"
                           }
                       },
                       "schemes": [
                           "https"
                       ],
                       "consumes": [
                           "application/json"
                       ],
                       "produces": [
                           "application/json"
                       ],
                       "paths": {},
                       "definitions": {}
                   }
               }
               this._backend = new Backend({url : "http://localhost:8289/services/convert-service"});
           };

           APIDesigner.prototype.constructor = APIDesigner;

           APIDesigner.prototype.setApiDoc = function (apiDoc) {
               // console.log("setApiDoc called");
               this._apiDoc = apiDoc;
           };

           APIDesigner.prototype.getApiDoc = function () {
               // console.log("getApiDoc called");
               // console.log(this._apiDoc);
               return this._apiDoc;
           };

           APIDesigner.prototype.renderBalSourceToSwagger = function (balSource) {
               // console.log("renderBalSourceToSwagger()");
               // console.log(this._backend.call("POST", {
               //      "name": "CalculatorService",
               //      "description": "null",
               //      "swaggerDefinition": "null",
               //      "ballerinaDefinition" : balSource
               //  }));
           };

           APIDesigner.prototype.renderSwaggerToBalSource = function () {

           };

           return APIDesigner;
       });