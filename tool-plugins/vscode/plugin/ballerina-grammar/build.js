/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

const fs = require('fs');
const path = require('path');
const yaml = require('js-yaml');
const plist = require('plist');

const inFile = 'syntaxes/ballerina.YAML-tmLanguage';
const outFile = 'syntaxes/ballerina.tmLanguage';

const inText = fs.readFileSync(inFile, 'utf-8');
const grammar = yaml.safeLoad(inText);

const variables = Object.keys(grammar.variables);

variables.forEach(name => {
    setVariable(grammar, {name, value: grammar.variables[name]});
});

const outText = plist.build(grammar);
fs.writeFileSync(outFile, outText);

function setVariable(grammar, variable) {
    const rules = Object.keys(grammar.repository);
    rules.forEach(rule => {
        replace(grammar.repository[rule], variable);
    });
}

function replace(rule, variable) {
    const properties = Object.keys(rule);
    properties.forEach(prop => {
        if (typeof rule[prop] === 'string') {
            const newR = rule[prop].replace(new RegExp(`{{${variable.name}}}`, "gim"), variable.value);
            rule[prop] = newR;
        } 

        if (typeof rule[prop] === 'object') {
            replace(rule[prop], variable);
        }
    });
}
