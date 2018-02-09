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
 *
 */

import _ from 'lodash';
import { isClientOnMacOS } from './../utils/client-info';

const isMacClient = isClientOnMacOS();

export const MAC_SYMBOLS = {
    COMMAND: '\u2318',
    OPTION: '\u2325',
    SHIFT: '\u21E7',
    CTRL: '\u2303',
};

export const KEYS = {
    CTRL: 'ctrl',
    ALT: 'alt',
    MAC: {
        COMMAND: 'command',
        OPTION: 'option',
    },
};

/**
 * derive final shortcut from shortcut def.
 * Eg: convert ctrl to cmd on mac, etc.
 *
 * @param {Object} shorcutDef Shortcut definition from command
 * @param {Object} shorcutDef.default default shortcut
 * @param {String} shorcutDef.mac shortcut for mac
 *
 * @returns {String} derived shortcut
 */
export function deriveShortcut(shorcutDef) {
    const shortcut = isMacClient && shorcutDef.mac ? shorcutDef.mac : shorcutDef.default;
    const keys = shortcut.split('+');
    const derivedKeys = [];
    keys.forEach((key, index) => {
        let derivedKey;
        switch (key) {
        case KEYS.CTRL:
            derivedKey = isMacClient && index === 0 ? KEYS.MAC.COMMAND : key;
            break;
        case KEYS.ALT:
            derivedKey = isMacClient ? KEYS.MAC.OPTION : key;
            break;
        default:
            derivedKey = key;
        }
        derivedKeys.push(derivedKey);
    });
    return derivedKeys.join('+');
}

/**
 * Capitalize first letter if a valid char found,
 * otherwise return same string.
 *
 * @param {String} str 
 * @returns {String}
 */
function getCapitalizedString(str) {
    if (str.match(/[a-zA-Z]/i) || str.charAt(0).match(/[a-zA-Z]/i)) {
        return _.capitalize(str);
    }
    return str;
}

/**
 * derive shortcut key label
 *
 * @param {String} shortcut Eg: Ctrl+Alt+N
 *
 * @returns {String} derived label
 */
export function deriveShortcutLabel(shortcut) {
    const keys = shortcut.split('+');
    let label = '';
    keys.forEach((key, index) => {
        if (index > 0) {
            label += '+';
        }
        if (isMacClient) {
            switch (key) {
            case KEYS.MAC.COMMAND:
                label += MAC_SYMBOLS.COMMAND;
                break;
            case KEYS.MAC.OPTION:
                label += MAC_SYMBOLS.OPTION;
                break;
            case KEYS.SHIFT:
                label += MAC_SYMBOLS.SHIFT;
                break;
            default:
                label += getCapitalizedString(key);
            }
        } else {
            label += getCapitalizedString(key);
        }
    });
    return label;
}

