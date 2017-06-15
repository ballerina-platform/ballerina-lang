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
import _ from 'lodash';
import log from 'log';
import Mousetrap from 'mousetrap';
import EventChannel from './../event/channel';

// command manager constructor
/**
 * Arg: application instance
 */
class CommandManager {

    constructor(application) {
        this.app = application;
        this.commandBus = new EventChannel();
        this.commands = [];
    }

    /**
     * Register a command.
     *
     * @param cmd  String command ID
     * @param options key-bindings etc.
     */
    registerCommand(cmd, options) {
        if (_.isEqual(_.findIndex(this.commands, ['id', cmd.id]), -1)) {
            const command = { id: cmd };
            if (_.has(options, 'shortcuts')) {
                _.set(command, 'shortcuts', _.get(options, 'shortcuts'));
            }
            this.commands.push(command);
            log.debug(`Command: ${cmd
                } is registered.`);
            // do shortcut key bindings
            if (_.has(options, 'shortcuts')) {
                const shortcuts = _.get(options, 'shortcuts');
                const key = this.app.isRunningOnMacOS() ? shortcuts.mac.key : shortcuts.other.key;
                const commandBus = this.commandBus;
                Mousetrap.bind(key, (e) => {
                    commandBus.trigger(cmd);
                    e.preventDefault();
                    e.stopPropagation();
                });
            }
        } else {
            log.error(`Command: ${cmd
                } is already registered. `);
        }
    }

    /**
     * Removes a registered command - with all the handlers registered under it.
     *
     * @param cmd  String command ID
     *
     */
    unRegisterCommand(cmd) {
        if (!_.isEqual(_.findIndex(this.commands, ['id', cmd.id]), -1)) {
            _.remove(this.commands, ['id', cmd.id]);
            // remove all handlers for the command
            this.commandBus.off(cmd, null, this.app);
            log.debug(`Command: ${cmd
                } is unregistered.`);
        } else {
            log.warn(`Command: ${cmd
                } cannot be found in registered commands. `);
        }
    }

    /**
     * Register a command handler.
     *
     * @param cmd  String command ID
     * @param handler
     * @param context this context for the handler, default is app instance
     */
    registerHandler(cmd, handler, context) {
        if (_.isEqual(_.findIndex(this.commands, ['id', cmd]), -1)) {
            const message = `No such registered command found. Command: ${cmd}`;
            log.debug(message);
        }
        this.commandBus.on(cmd, handler, context || this.app);
    }

    /**
     * Removes a registered command handler.
     *
     * @param cmd  String command ID
     * @param handler
     */
    unRegisterHandler(cmd, handler) {
        this.commandBus.off(cmd, handler, this.app);
    }

    /**
     * Dispatch a command with give args
     *
     * @param cmdID  String command ID **** always 1st arg
     * @param args  0..* args for the command handler
     *
     */
    dispatch(...args) {
        const cmdID = args[0];
        if (!_.isUndefined(cmdID)) {
            // get only needed args for trigger - right now all are wanted
            const triggerArgs = _.takeRight(args, args.length);
            const triggerFn = this.commandBus.trigger;
            triggerFn.apply(this.commandBus, triggerArgs);
        }
    }

    getCommands() {
        return this.commands;
    }
}

export default CommandManager;
