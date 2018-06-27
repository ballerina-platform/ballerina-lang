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
const request = require('request');

const parserUrl = `http://127.0.0.1:9091/composer/ballerina/parser/file/validate-and-parse`;

function findBalFilesInDirSync(dir, filelist=[]) {
    const files = fs.readdirSync(dir);
    files.forEach((file) => {
        if (fs.statSync(path.join(dir, file)).isDirectory()) {
            filelist = findBalFilesInDirSync(path.join(dir, file), filelist);
        } else if (path.extname(file) === '.bal') {
            filelist.push(path.join(dir, file));
        }
    });
    return filelist;
}

function parse(content, file, callback) {
    const parseOpts = {
        content,
        fileName: path.basename(file),
        filePath: path.dirname(file),
        includePackageInfo: true,
        includeProgramDir: true,
        includeTree: true,
    }

    const parseReq = request.post({
        url: parserUrl,
        json: parseOpts,
    }, (err, httpResponse, body) => {
        callback(body.model)
    });
}

module.exports = {
    findBalFilesInDirSync,
    parse,
}