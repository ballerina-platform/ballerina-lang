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
define(['log', 'jquery', 'lodash'], function (log, $, _) {

    var AlertsManager = {};

    /**
     * Shows success alert.
     * Usage: success("Successfully done"), success({message: "Successfully done", timeout: 7000}).
     * @param {string|Object} args - If "string" is passed, it is considered as the message for the alert.
     * @param {string} args.message - The message of the alert when args is an Object type.
     * @param {number} [args.timeout=5000] - The amount of time the alert being shown between slide up and slide down
     * when args is an Object type.
     * @param {string} [args.boldMessage="Success!"] - The bold message of the alert when args is an Object type.
     * @static
     */
    AlertsManager.success = function (args) {
        var message = typeof args === "string" ? args : _.get(args, "message");
        var timeout = _.get(args, "timeout", 5000);
        var boldMessage = _.get(args, "boldMessage", "Success!");

        var notificationContainer = $("#notification-container");
        var successAlertWrapper = $("#alert-manager-success").finish();
        if (successAlertWrapper.length > 0) {
            $("#alert-manager-success-bold-message").text(boldMessage);
            $("#alert-manager-success-message").text(message);
        } else {
            // Creating main alert wrapper
            successAlertWrapper = $("<div/>", {
                id: "alert-manager-success",
                class: 'alert-manager-wrapper alert alert-success'
            }).appendTo(notificationContainer);

            // Adding icon.
            $("<i/>", {
                class: "icon fw fw-success"
            }).appendTo(successAlertWrapper);

            $("<strong/>", {
                id: "alert-manager-success-bold-message",
                text: boldMessage
            }).appendTo(successAlertWrapper);

            $("<span/>", {
                id: "alert-manager-success-message",
                class: "alert-manager-message",
                text: message
            }).appendTo(successAlertWrapper);
        }

        $(successAlertWrapper).slideDown(400).delay(timeout).slideUp(400);
    };

    /**
     * Shows info alert.
     * Usage: info("Updated view"), info({message: "Updated view", timeout: 7000}).
     * @param {string|Object} args - If "string" is passed, it is considered as the message for the alert.
     * @param {string} args.message - The message of the alert when args is an Object type.
     * @param {number} [args.timeout=5000] - The amount of time the alert being shown between slide up and slide down
     * when args is an Object type.
     * @param {string} [args.boldMessage="Info!"] - The bold message of the alert when args is an Object type.
     * @static
     */
    AlertsManager.info = function (args) {
        var message = typeof args === "string" ? args : _.get(args, "message");
        var timeout = _.get(args, "timeout", 5000);
        var boldMessage = _.get(args, "boldMessage", "Info!");

        var notificationContainer = $("#notification-container");
        var infoAlertWrapper = $("#alert-manager-info").finish();
        if (infoAlertWrapper.length > 0) {
            $("#alert-manager-info-bold-message").text(boldMessage);
            $("#alert-manager-info-message").text(message);
        } else {
            // Creating main alert wrapper
            infoAlertWrapper = $("<div/>", {
                id: "alert-manager-info",
                class: 'alert-manager-wrapper alert alert-info'
            }).appendTo(notificationContainer);

            // Adding icon.
            $("<i/>", {
                class: "icon fw fw-warning"
            }).appendTo(infoAlertWrapper);

            $("<strong/>", {
                id: "alert-manager-info-bold-message",
                text: boldMessage
            }).appendTo(infoAlertWrapper);

            $("<span/>", {
                id: "alert-manager-info-message",
                class: "alert-manager-message",
                text: message
            }).appendTo(infoAlertWrapper);
        }

        $(infoAlertWrapper).slideDown(400).delay(timeout).slideUp(400);
    };

    /**
     * Shows warning alert.
     * Usage: warn("Disk space low"), warn({message: "Disk space low", timeout: 7000}).
     * @param {string|Object} args - If "string" is passed, it is considered as the message for the alert.
     * @param {string} args.message - The message of the alert when args is an Object type.
     * @param {number} [args.timeout=5000] - The amount of time the alert being shown between slide up and slide down
     * when args is an Object type.
     * @param {string} [args.boldMessage="Warning!"] - The bold message of the alert when args is an Object type.
     * @static
     */
    AlertsManager.warn = function (args) {
        var message = typeof args === "string" ? args : _.get(args, "message");
        var timeout = _.get(args, "timeout", 5000);
        var boldMessage = _.get(args, "boldMessage", "Warning!");

        var notificationContainer = $("#notification-container");
        var warningAlertWrapper = $("#alert-manager-warning").finish();
        if (warningAlertWrapper.length > 0) {
            $("#alert-manager-warning-bold-message").text(boldMessage);
            $("#alert-manager-warning-message").text(message);
        } else {
            // Creating main alert wrapper
            warningAlertWrapper = $("<div/>", {
                id: "alert-manager-warning",
                class: 'alert-manager-wrapper alert alert-warning'
            }).appendTo(notificationContainer);

            // Adding icon.
            $("<i/>", {
                class: "icon fw fw-warning"
            }).appendTo(warningAlertWrapper);

            $("<strong/>", {
                id: "alert-manager-warning-bold-message",
                text: boldMessage
            }).appendTo(warningAlertWrapper);

            $("<span/>", {
                id: "alert-manager-warning-message",
                class: "alert-manager-message",
                text: message
            }).appendTo(warningAlertWrapper);
        }

        $(warningAlertWrapper).slideDown(400).delay(timeout).slideUp(400);
    };

    /**
     * Shows error alert.
     * Usage: error("Error occurred"), error({message: "Error occurred", timeout: 7000}).
     * @param {string|Object} args - If "string" is passed, it is considered as the message for the alert.
     * @param {string} args.message - The message of the alert when args is an Object type.
     * @param {number} [args.timeout=5000] - The amount of time the alert being shown between slide up and slide down
     * when args is an Object type.
     * @param {string} [args.boldMessage="Error!"] - The bold message of the alert when args is an Object type.
     * @static
     */
    AlertsManager.error = function (args) {
        var message = typeof args === "string" ? args : _.get(args, "message");
        var timeout = _.get(args, "timeout", 5000);
        var boldMessage = _.get(args, "boldMessage", "Error!");

        var notificationContainer = $("#notification-container");
        var errorAlertWrapper = $("#alert-manager-error").finish();
        if (errorAlertWrapper.length > 0) {
            $("#alert-manager-error-bold-message").text(boldMessage);
            $("#alert-manager-error-message").text(message);
        } else {
            // Creating main alert wrapper
            errorAlertWrapper = $("<div/>", {
                id: "alert-manager-error",
                class: 'alert-manager-wrapper alert alert-danger'
            }).appendTo(notificationContainer);

            // Adding icon.
            $("<i/>", {
                class: "icon fw fw-error"
            }).appendTo(errorAlertWrapper);

            $("<strong/>", {
                id: "alert-manager-error-bold-message",
                text: boldMessage
            }).appendTo(errorAlertWrapper);

            $("<span/>", {
                id: "alert-manager-error-message",
                class: "alert-manager-message",
                text: message
            }).appendTo(errorAlertWrapper);
        }

        $(errorAlertWrapper).slideDown(400).delay(timeout).slideUp(400);
    };

    return AlertsManager;
});