/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the 'License'); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

const _ = require('lodash');
const chalk = require('chalk');

const subScriptChars = ['₀', '₁', '₂', '₃', '₄', '₅', '₆', '₇', '₈', '₉'];

function debugPrint(node, name = '', l = 1) {
    const wsSimbols = { '"': '', ' ': '\u2022', '\n': '\u21B5', '\t': '\u21E5' };
    const wsStr = (node.ws || []).map(ws => (ws.static ? chalk.gray : chalk.magenta)((ws.ws ?
        ws.ws.replace(/\s/g, c => (wsSimbols[c]) || c.codePointAt(0)) :
        '\u26AC') +
        JSON.stringify(ws.text).slice(1, -1) +
        String(ws.i).split('')
            .map(i => subScriptChars[Number(i)])
            .join('')))
        .join(' ');

    const children = _.groupBy([...node], ([_, c]) => c && c.kind === 'Identifier');
    console.log(new Array(l).join(' '),
        chalk.green(name),
        chalk.blue(node.kind),
        node.entries().map(([key, val]) => {
            let valPritty = val;
            if (val === true) {
                valPritty = '\u2713';
            } else if (val === false) {
                return chalk.green.dim.strikethrough(key);
            }
            return chalk.green(key) + ':' + chalk.cyan(_.isObject(valPritty) ? JSON.stringify(valPritty) : valPritty);
        }).join(),
        (children.true || []).map(([key, val]) => {
            return val.value && chalk.green(key) + ':' + chalk.cyan.bold(val.value);
        }).join(),
        wsStr);
    for (const [childName, child] of children.false || []) {
        debugPrint(child, childName, l + 2);
    }
}

module.exports = debugPrint;