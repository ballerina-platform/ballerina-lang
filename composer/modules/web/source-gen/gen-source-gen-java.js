/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import fs from 'fs';
import path from 'path';
import _ from 'lodash';
import defaultWS from './defualt-ws.json';

const tab = i => _.repeat('    ', i + 2);
const tab1 = tab(1);
const templatePath = path.join(__dirname, 'SourceGenTemplate.java');
const template = fs.readFileSync(templatePath, 'utf8').split('// auto-gen-code');
const emptyDefaultWS = {};

function join(arr, sep, indent) {
    // const indentLen = indent.lenght;
    const maxLen = 65;
    const str = arr.join(sep);
    let s = 0;
    let lastIndent = -1;
    const x = [];
    for (let i = 1; i <= Math.floor(str.length / maxLen); i++) {
        const p = str.lastIndexOf(sep, i * maxLen);
        if (lastIndent !== p) {
            x.push(str.substring(s, p));
            lastIndent = p;
        }
        s = p;
    }
    x.push(str.substring(s));
    return x.join('\r\n' + indent);
}

function accessJsonObjs(property, objectType){
    let propertyComps = property.split('.');
    let result = "";
    let canBeAPremitive = false;
    let functionCall = false;
    let isArrayType = false;

    if(propertyComps.length > 1) {
        canBeAPremitive = true;
    }

    switch (propertyComps[propertyComps.length -1]) {
        case 'size()':
            functionCall = true;
            isArrayType = true;
            break;
        default:
            functionCall = false;
            isArrayType = false;
            break;
    }

    if(!objectType) {
        if(propertyComps.length === 1) {
            objectType = "get";
        }else {
            objectType = "getAsJsonObject";
        }
    }

    for(let i = 0; i < propertyComps.length; i++){
        if ((propertyComps.length - 1) === i && canBeAPremitive && !functionCall) {
            result += '.get("' + propertyComps[i] + '")';
        } else if((propertyComps.length - 1) === i && functionCall) {
            result += '.'+propertyComps[i];
        } else if((propertyComps.length - 2) === i && functionCall && isArrayType) {
            result += '.getAsJsonArray'  + '("' + propertyComps[i] + '")';
        } else {
            result += '.' + objectType + '("' + propertyComps[i] + '")';
        }
    }
    return result;
}

const stream = fs.createWriteStream(path.join(__dirname, '..','..','..','..','language-server', 'modules',
    'langserver-core','src', 'main','java','org','ballerinalang','langserver' ,'SourceGen.java'));
stream.once('open', () => {
    const gPath = path.join(__dirname, 'tree.g');
const grammar = fs.readFileSync(gPath, 'utf8');
const spited = grammar.split(/^\s*$/gm).map(rs => _.compact(rs.split(/\n^ +[;:|] */gm).map(s => s.trim())));

const write = (line) => {
    console.log(line);
    stream.write(line + '\n');
};

write(template[0]);

for (let i = 0; i < spited.length; i++) {
    const kind = spited[i].shift().trim();
    emptyDefaultWS[kind] = {};
    const rules = spited[i].map(s => s.split(/\s+/g));
    write(tab(0) + 'case "' + kind + '":');
    const nodeWS = defaultWS[kind] || {};
    for (let j = 0; j < rules.length; j++) {
        const rule = rules[j];
        const js = [];
        const condition = [];

        const wQuoted = (g, after) => {
            emptyDefaultWS[kind]['before ' + g] = nodeWS['before ' + g] || '';
            emptyDefaultWS[kind]['after ' + g] = nodeWS['after ' + g] || '';
            let defaultWs;
            if (after) {
                defaultWs = nodeWS['after ' + g];
            } else {
                defaultWs = nodeWS['before ' + g];
            }
            return (defaultWs ? '"' + defaultWs + '"' : '""');
        };
        const wWrapped = g => 'w(' + wQuoted(g) + ') + ';
        const wAfterWrapped = g => wQuoted(g, true) && ' + a(' + wQuoted(g, true) + ')';
        const pushWithWS = (jsArr, getter, wsGetter) => {
            wsGetter = wsGetter || getter;
            jsArr.push(wWrapped(wsGetter) + getter + wAfterWrapped(wsGetter));
        };

        for (let k = 0; k < rule.length; k++) {
            const p = rule[k];
            if (p.match(/^<.*>$/)) {
                let getter = p.slice(1, -1);
                if (getter.match(/\.value$/)) {
                    const getterWithoutVal = getter.slice(0, -6);
                    const propAccess = 'node' +accessJsonObjs( getter + 'WithBar')+'.getAsString()';
                    js.push(wWrapped(getterWithoutVal) + propAccess + wAfterWrapped(getterWithoutVal));
                    getter += 'WithBar';
                } else if (getter.match(/\.source$/)) {
                    getter = getter.replace(/\.source$/, '');
                    if (wQuoted(getter)) {
                        js.push('a(' + wQuoted(getter) + ')');
                    }
                    js.push('getSourceOf(node' + accessJsonObjs(getter,'getAsJsonObject') + ', pretty, replaceLambda)');
                } else if (getter.match(/^[^?]+\?[^?]+$/)) {
                    const parts = getter.split('?');
                    getter = parts[0];
                    js.push('(node' + accessJsonObjs(getter) + '.getAsBoolean() ? ' + wWrapped(parts[1]) +
                        '"' + parts[1] + '"' +
                        wAfterWrapped(parts[1]) + ' : "")');
                    getter = null;
                } else if (getter.match(/^[^?]+\?$/)) {
                    getter = getter.slice(0, -1);
                } else {
                    pushWithWS(js, 'node'+accessJsonObjs(getter)+'.getAsString()', getter);
                }

                if (getter) {
                    condition.push(getter);
                }
            } else if (p.match(/^<.*>[*+]$/)) {
                const getter = p.slice(1, -2).split('-')[0];
                const params = ['node' + accessJsonObjs(getter,'getAsJsonArray'), 'pretty', 'replaceLambda',
                    'w('+(wQuoted(getter) || '""')+ ')'];
                const hasSuffix = p.indexOf('suffixed-by') >= 0;
                if (p.indexOf('joined-by') >= 0 || hasSuffix) {
                    params.push('"' + p.substr(-3, 1) + '"');
                }else {
                    params.push('null');
                }
                if (hasSuffix) {
                    params.push('true');
                }else {
                    params.push('false');
                }

                js.push('join(' + params.join(', ') + ')');
                condition.push(getter);
                if (p.slice(-1) === '+') {
                    condition.push(getter + '.size()');
                }
            } else {
                if (p === '}' && kind !== 'RecordLiteralExpr') {
                    // HACK
                    js.push('outdent()');
                }
                js.push(wWrapped(p) + '\"' + p + '\"' + wAfterWrapped(p));
                // HACK
                if ((p === '{' && kind !== 'RecordLiteralExpr')) {
                    if (kind === 'If') {
                        // HACK
                        // We need to spacial case If to handle 'else if' case correctly.
                        js.unshift('(node.getAsJsonObject("parent").get("kind").getAsString() ' +
                            '.equals(\"If\") ? \"\" : dent())');
                    } else if (rule[0] === '<annotationAttachments>*') {
                        js.splice(1, 0, 'dent()');
                    } else {
                        js.unshift('dent()');
                    }
                    js.push('indent()');
                } else if (p === ';') {
                    js.unshift('dent()');
                }
            }
        }
        if (nodeWS.dent) {
            emptyDefaultWS[kind].dent = true;
            js.unshift('dent()');
        }
        const conditionStr = join(condition.map((s) => {
            let propertyComps = s.split('.');
            let result = 'node' + accessJsonObjs(s);
            if (propertyComps[propertyComps.length - 1] === 'lambda' ||
                propertyComps[propertyComps.length -1] === 'endpoint' ||
                propertyComps[propertyComps.length -1] === 'root' ||
                propertyComps[propertyComps.length -1] === 'forkJoinedSend' ||
                propertyComps[propertyComps.length -1] === 'global' ||
                propertyComps[propertyComps.length -1] === 'anonStruct' ||
                propertyComps[propertyComps.length -1] === 'inTemplateLiteral' ||
                propertyComps[propertyComps.length -1] === 'actionInvocation' ||
                propertyComps[propertyComps.length -1] === 'ladderParent' ||
                propertyComps[propertyComps.length -1] === 'builtin' ||
                propertyComps[propertyComps.length -1] === 'userDefinedAlias') {
                let nullCheck = result + ' != null';
                let valueCheck = result + ' .getAsBoolean()';
                result = nullCheck + ' && ' + valueCheck;
            } else if (propertyComps[propertyComps.length -1] === 'valueWithBar'){
                let nullCheck = result + ' != null';
                let valueCheck = '!'+result + '.getAsString().isEmpty()';
                result = nullCheck + ' && ' + valueCheck;
            } else if (propertyComps[propertyComps.length -1] === 'size()'){
                result += ' > 0';
            } else {
                result += ' != null';
            }
            return result;
        }),' && ', tab(4));
        const retrunSt = 'return ' + (join(js, ' + ', tab(2)) || '\"\"') + ';';
        if (rules.length > 1) {
            if (j === 0) {
                write(tab1 + 'if (' + conditionStr + ') {');
            } else if (j === rules.length - 1) {
                write(tab1 + '} else {');
            } else {
                write(tab1 + '} else if (' + conditionStr + ') {');
            }
            write(tab(2) + retrunSt);
        } else {
            write(tab(1) + retrunSt);
        }
    }
    if (rules.length === 0) {
        write(tab1 + 'return \"\";');
    } else if (rules.length > 1) {
        write(tab1 + '}');
    }
}
write(template[1]);
console.log(JSON.stringify(emptyDefaultWS));
});
