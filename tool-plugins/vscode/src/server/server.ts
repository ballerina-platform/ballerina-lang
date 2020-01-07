'use strict';
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
import * as path from 'path';
import { debug } from '../utils/logger';
import { ServerOptions, ExecutableOptions } from 'vscode-languageclient';

export function getServerOptions(ballerinaCmd: string, experimental: boolean, debugLogsEnabled: boolean, traceLogsEnabled: boolean) : ServerOptions {
    debug(`Using Ballerina CLI command '${ballerinaCmd}' for Language server.`);
    let cmd = ballerinaCmd;
    let args = ["start-language-server"];
  
    let opt: ExecutableOptions = {};
    opt.env = Object.assign({}, process.env);
    if (process.env.LSDEBUG === "true") {
        debug('Language Server is starting in debug mode.');
        opt.env.BAL_DEBUG_OPTS = "-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005,quiet=y";
    }
    if (debugLogsEnabled || traceLogsEnabled) {
        let str = [];
        if (debugLogsEnabled) {
            str.push('debug');
            args.push('--debug-log');
        }
        if (traceLogsEnabled) {
            str.push('trace');
            args.push('--trace-log');
        }
        debug('Language Server ' + str.join(', ') +' logs enabled.');
    }
    if (process.env.LS_CUSTOM_CLASSPATH) {
        args.push('--classpath', process.env.LS_CUSTOM_CLASSPATH);
    }
    if (experimental) {
        args.push('--experimental');
    }

    return {
        command: cmd,
        args,
        options: opt
    };
}

export function getOldServerOptions(ballerinaHome: string, experimental: boolean, debugLogsEnabled: boolean, traceLogsEnabled: boolean) : ServerOptions {
    // Fallback into old way, TODO: Remove this in a later verison
    debug(`Using Ballerina installation at '${ballerinaHome}' for Language server.`);

    let cmd;
    const cwd = path.join(ballerinaHome, 'lib', 'tools', 'lang-server', 'launcher');
    let args = [];
    if (process.platform === 'win32') {
        cmd = path.join(cwd, 'language-server-launcher.bat');
    } else {
        cmd = 'sh';
        args.push(path.join(cwd, 'language-server-launcher.sh'));
    }

    let opt: ExecutableOptions = {cwd: cwd};
    opt.env = Object.assign({}, process.env);
    if (process.platform === "darwin") {
        opt.env.BALLERINA_HOME = ballerinaHome;
    }
    if (process.env.LSDEBUG === "true") {
        debug('Language Server is starting in debug mode.');
        args.push('--debug');
    }
    if (debugLogsEnabled || traceLogsEnabled) {
        let str = [];
        if (debugLogsEnabled) {
            str.push('debug');
            opt.env.DEBUG_LOG = debugLogsEnabled;
        }
        if (traceLogsEnabled) {
            str.push('trace');
            opt.env.TRACE_LOG = traceLogsEnabled;
        }
        debug('Language Server ' + str.join(', ') +' logs enabled.');
    }
    if (process.env.LS_CUSTOM_CLASSPATH) {
        args.push('--classpath', process.env.LS_CUSTOM_CLASSPATH);
    }
    if (experimental) {
        args.push('--experimental');
    }

    return {
        command: cmd,
        args,
        options: opt
    };
}
