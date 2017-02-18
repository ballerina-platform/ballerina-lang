/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
define(['require', 'jquery', 'lodash'], function (require, $, _) {
    var environment_content = {};

    /**
     * Get the packages from the package list api.
     * */
    environment_content.getPackages = function () {
        var data = [];
        $.ajax({
            type: "GET",
            url: requirejs.s.contexts._.config.package_listing,
            contentType: "application/json; charset=utf-8",
            async: false,
            dataType: "json",
            success: function (response) {
                data = response;
            },
            error: function (xhr, textStatus, errorThrown) {
                data = {"error": true, "message": "Unable to retrieve packages."};
            }
        });
        return data;
    };
    return environment_content;
});