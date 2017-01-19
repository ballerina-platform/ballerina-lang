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
define(['lib/module-b'], function(moduleB){
  return {

var workspaceServiceURL = "http://localhost:8289/service/workspace";
var saveServiceURL = workspaceServiceURL + "/read";
var defaultView = {configLocation: location.val()};

var saveServiceURL = workspaceServiceURL + "/read";

var path = defaultView.configLocation;
$.ajax({
    url: saveServiceURL,
    type: "POST",
    data: path,
    contentType: "text/plain; charset=utf-8",
    async: false,
    success: function (data, textStatus, xhr) {
    if (xhr.status == 200) {
        var pathArray = _.split(path, self.app.getPathSeperator()),
        fileName = _.last(pathArray),
        folderPath = _.join(_.take(pathArray, pathArray.length -1), self.app.getPathSeperator());

        var file = new File({
            name: fileName,
            path: folderPath,
            content: data.content,
            isPersisted: true
        });
        app.commandManager.dispatch("create-new-tab", {tabOptions: {file: file}});
        alertSuccess();
    } else {
        alertError();
    }
   },
    error: function (res, errorCode, error) {
        alertError();
    }
    });

});