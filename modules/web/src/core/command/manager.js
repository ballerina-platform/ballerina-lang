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
import PropTypes from 'prop-types';
import _ from 'lodash';
import log from 'log';
import Mousetrap from 'mousetrap';
import { PLUGIN_ID } from './constants';
import CommandChannel from './channel';
import Plugin from './../plugin/plugin';
import { derriveShortcut, derriveShortcutLabel } from './shortcut-util';

/**
 * Command Manager
 */
class CommandManager extends Plugin {

    constructor() {
        super();
        this.dispatch = this.dispatch.bind(this);
    }

    /**
     * @inheritdoc
     */
    getID() {
        return PLUGIN_ID;
    }

    /**
     * @inheritdoc
     */
    init(config) {
        super.init(config);
        this.commandChannel = new CommandChannel();
        this.commands = [];
        return {
            dispatch: this.dispatch,
        };
    }

    /**
     * Register a command.
     *
     * @param {Object} cmd command definition
     * @param {String} cmd.id command ID
     * @param {Object} [cmd.shortcut] keyboard shortcuts def for the command
     * @param {Object} cmd.shortcut.default default shortcut
     * @param {String} cmd.shortcut.mac shortcut for mac
     */
    registerCommand(cmd) {
        if (_.isEqual(_.findIndex(this.commands, ['id', cmd.id]), -1)) {
            this.commands.push(cmd);
            log.debug(`Command: ${cmd.id} is registered.`);
            // key bindings
            if (_.has(cmd, 'shortcut')) {
                const { shortcut } = cmd;
                const key = derriveShortcut(shortcut);
                const label = derriveShortcutLabel(key);
                cmd.shortcut.derrived = {
                    key, label,
                };
                console.log(cmd);
                Mousetrap.bind(key, (e) => {
                    this.commandChannel.trigger(cmd);
                    e.preventDefault();
                    e.stopPropagation();
                });
            }
        } else {
            throw new Error(`Command: ${cmd.id} is already registered.`);
        }
    }

    /**
     * Removes a registered command - with all the handlers registered under it.
     *
     * @param {String} cmdID command ID
     *
     */
    unRegisterCommand(cmdID) {
        if (!_.isEqual(_.findIndex(this.commands, ['id', cmdID]), -1)) {
            _.remove(this.commands, ['id', cmdID]);
            // remove all handlers for the command
            this.commandChannel.off(cmdID, null);
            log.debug(`Command: ${cmdID} is unregistered.`);
        } else {
            log.warn(`Command: ${cmdID} cannot be found in registered commands.`);
        }
    }

    /**
     * Register a command handler.
     *
     * @param {String} cmdID command ID
     * @param {Function} handler
     * @param {Object} context this context for the handler, default is undefined
     */
    registerHandler(cmdID, handler, context) {
        if (_.isEqual(_.findIndex(this.commands, ['id', cmdID]), -1)) {
            log.warn(`No such registered command found. Command: ${cmdID}`);
        }
        this.commandChannel.on(cmdID, handler, context);
    }

    /**
     * Removes a registered command handler.
     *
     * @param {String} cmd command ID
     * @param {Function} handler handler function
     */
    unRegisterHandler(cmd, handler) {
        this.commandChannel.off(cmd, handler);
    }

    /**
     * Dispatch a command with given args
     *
     * @param {String} cmdID command ID
     * @param {any[]} args args for the command handler
     *
     */
    dispatch(cmdID, ...args) {
        if (!_.isUndefined(cmdID)) {
            const cmd = _.find(this.commands, ['id', cmdID]);
            if (!_.isNil(cmd)) {
                const argTypes = cmd.argTypes;
                if (!_.isNil(argTypes)) {
                    // validate command args
                    PropTypes.checkPropTypes(argTypes, args, 'command-args', cmd);
                }
                this.commandChannel.trigger(cmdID, args);
            }
        }
    }

    /**
     * Get registered command definitions.
     *
     * @returns {Object[]} command definitions
     */
    getCommands() {
        return this.commands;
    }
}

export default CommandManager;
