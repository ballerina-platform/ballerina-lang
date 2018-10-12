/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
export const REGEX_LETTER = /^([a-z]){1}$/;
export const REGEX_NUMBER = /^(\d){1}$/;
export const REGEX_FUNCTION_KEY = /^(f\d){1}$/;
export const REGEX_SPECIAL_SYMBOLS = /^([\.,`\\[\];=\-/']){1}$/;

 /**
  * Gets the keycode for given key
  * @param {String} key Key in readable form
  */
export function getMonacoKeyCode(key) {
    const KC = monaco.KeyCode;
    let propName = '';
    if (key.match(REGEX_NUMBER) || key.match(REGEX_LETTER)) {
        propName = 'KEY_' + key.toUpperCase();
    } else if (key.match(REGEX_FUNCTION_KEY)) {
        propName = key.toUpperCase();
    } else if (key.match(REGEX_SPECIAL_SYMBOLS)) {
        propName = 'US_';
        switch (key) {
            case '\\' : propName += 'BACKSLASH'; break;
            case '`' : propName += 'BACKTICK'; break;
            case ']' : propName += 'CLOSE_SQUARE_BRACKET'; break;
            case ',' : propName += 'COMMA'; break;
            case '.' : propName += 'DOT'; break;
            case '=' : propName += 'EQUAL'; break;
            case '-' : propName += 'MINUS'; break;
            case '[' : propName += 'OPEN_SQUARE_BRACKET'; break;
            case '\'' : propName += 'QUOTE'; break;
            case ';' : propName += 'SEMICOLON'; break;
            case '/' : propName += 'SLASH'; break;
            default: // Do Nothing
        }
    }
    return KC[propName];
}

/**
 * Calculates keybinding number for monaco for a given
 * key combination string.
 * @param {String} keyCombination Key combination string
 * @returns {number} Key binding
 */
export function getMonacoKeyBinding(keyCombination) {
    const KM = monaco.KeyMod;
    const keys = keyCombination.split('+');
    const keyCodes = [];
    keys.forEach((key) => {
        if (key === 'ctrl' || key === 'command') {
            keyCodes.push(KM.CtrlCmd);
        } else if (key === 'shift') {
            keyCodes.push(KM.Shift);
        } else if (key === 'alt' || key === 'option') {
            keyCodes.push(KM.Alt);
        } else {
            keyCodes.push(getMonacoKeyCode(key));
        }
    });
    return keyCodes.reduce((prev, curr) => { return prev | curr; });
}
