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
define(['lodash', 'backbone', 'log', 'mousetrap'], function (_, Backbone, log, Mousetrap) {

    // command manager constructor
    /**
     * Arg: application instance
     */
    return function(app) {

        // not reinventing the wheel - reusing event-bus impl provided in backbone models
        var CommandBus = Backbone.Model.extend({}),
            commandBus = new CommandBus(),
            commands = [];

        /**
         * Register a command.
         *
         * @param cmd  String command ID
         * @param options key-bindings etc.
         */
        this.registerCommand = function (cmd, options) {
            if (_.isEqual(_.findIndex(commands,  ['id', cmd.id]), -1)) {
                var command = {id: cmd};
                if(_.has(options, 'shortcuts')){
                    _.set(command, 'shortcuts', _.get(options, 'shortcuts'))
                }
                commands.push(command);
                log.debug("Command: " + cmd +
                    " is registered.");
                // do shortcut key bindings
                if(_.has(options, 'shortcuts')){
                    var shortcuts = _.get(options, 'shortcuts'),
                        key = app.isRunningOnMacOS() ? shortcuts.mac.key : shortcuts.other.key;
                    Mousetrap.bind(key, function(e) {
                        commandBus.trigger(cmd);
                        e.preventDefault();
                        e.stopPropagation();
                    });
                }
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
        this.unRegisterCommand = function (cmd) {
            if (!_.isEqual(_.findIndex(commands,  ['id', cmd.id]), -1)) {
                _.remove(commands, ['id', cmd.id]);
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
         * @param context this context for the handler, default is app instance
         */
        this.registerHandler = function (cmd, handler, context) {
            if(_.isEqual(_.findIndex(commands,  ['id', cmd]), -1)){
                var message = "No such registered command found. Command: " + cmd;
                log.debug(message);
            }
            commandBus.on(cmd, handler, context || app);
        };

        /**
         * Removes a registered command handler.
         *
         * @param cmd  String command ID
         * @param handler
         */
        this.unRegisterHandler = function (cmd, handler) {
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
        this.dispatch = function () {
            var cmdID = arguments[0];
            if (!_.isUndefined(cmdID)) {
                // get only needed args for trigger - right now all are wanted
                var triggerArgs = _.takeRight(arguments, arguments.length);
                var triggerFn = commandBus.trigger;
                triggerFn.apply(commandBus, triggerArgs);
            }
        };

        this.getCommands = function(){
            return commands;
        }
    }
});

