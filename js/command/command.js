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
define(['lodash', 'backbone', 'logger'], function (_, Backbone, log) {

    // command manager constructor
    /**
     * Arg: application instance
     */
    return function(app) {

        // not reinventing the wheel - reusing event-bus impl provided in backbone models
        var CommandBus = Backbone.Model.extend({}),
            commandBus = new CommandBus(),
            commands = {},
            manager = {};

        /**
         * Register a command.
         *
         * @param cmd  String command ID
         * @param options key-bindings etc.
         */
        manager.registerCommand = function (cmd, options) {
            if (!_.has(commands, cmd)) {
                _.set(commands, cmd, options);
                log.debug("Command: " + cmd +
                    " is registered.");
                //TODO: create key-bindings etc. for the command
            } else {
                log.error("Command: " + cmd +
                    " is already registered. ");
            }
        };

        /**
         * Removes a registered command - with all the handlers registered under it.
         *
         * @param cmd  String command ID
         *
         */
        manager.unRegisterCommand = function (cmd) {
            if (_.has(commands, cmd)) {
                _.unset(commands, cmd);
                //remove all handlers for the command
                commandBus.off(cmd, null, app);
                log.debug("Command: " + cmd +
                    " is unregistered.");
            } else {
                log.warn("Command: " + cmd +
                    " cannot be found in registered commands. ");
            }
        };

        /**
         * Register a command handler.
         *
         * @param cmd  String command ID
         * @param handler
         */
        manager.registerHandler = function (cmd, handler) {
            if(!_.has(commands, cmd)){
                var error = "No such registered command found. Command: " + cmd;
                log.error(error);
                throw error;
            }
            commandBus.on(cmd, handler, app);
        };

        /**
         * Removes a registered command handler.
         *
         * @param cmd  String command ID
         * @param handler
         */
        manager.unRegisterHandler = function (cmd, handler) {
            commandBus.off(cmd, handler, app);
        };

        /**
         * Dispatch a command with give args
         *
         * @param cmdID  String command ID **** always 1st arg
         * @param args  0..* args for the command handler
         *
         * ****IMPORTANT*****
         * using implicit arguments object instead of traditional
         * function parameter defs to enable dynamic number of args
         */
        manager.dispatch = function () {
            var cmdID = arguments[0];
            if (!_.isUndefined(cmdID)) {
                // get only needed args for trigger - right now all are wanted
                var triggerArgs = _.takeRight(arguments, arguments.length);
                var triggerFn = commandBus.trigger;
                triggerFn.apply(commandBus, triggerArgs);
            }
        };

        return manager;
    }
});

