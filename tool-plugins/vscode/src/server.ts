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
import { sync } from 'glob';
import { log } from './logger';
import { ServerOptions } from 'vscode-languageclient';
import { getPluginConfig } from './config';

const libPath: string = '/bre/lib/*';
const jrePath: string = '/bre/lib/jre*';
const langServerLibPath: string = '/lib/tools/lang-server/lib/*';
const main: string = 'org.ballerinalang.langserver.launchers.stdio.Main';

function getClassPath(ballerinaHome: string): string {
    const config = getPluginConfig();
    const customClassPath: string | undefined = config.classpath;
	// in windows class path seperated by ';'
	const sep = process.platform === 'win32' ? ';' : ':';
    let classpath = path.join(ballerinaHome, langServerLibPath) + sep + path.join(ballerinaHome, libPath);

    if (customClassPath) {
        classpath =  customClassPath + sep + classpath;
    }
    return classpath;
}

function getExcecutable(ballerinaHome: string) : string {
    let excecutable : string = 'java';
    const { JAVA_HOME } = process.env;
    const files = sync(path.join(ballerinaHome, jrePath));

    if (files[0]) {
        log(`Using java from ballerina.home: ${files[0]}`);
        excecutable = path.join(files[0], 'bin', 'java');
    } else if (JAVA_HOME) {
        log(`Using java from JAVA_HOME: ${JAVA_HOME}`);
        excecutable = path.join(JAVA_HOME, 'bin', 'java');
    }
    return excecutable;
}

export function getServerOptions(ballerinaHome: string) : ServerOptions {
    const config = getPluginConfig();
    const args: string[] = ['-cp', getClassPath(ballerinaHome)];
    if (process.env.LSDEBUG === "true") {
        log('LSDEBUG is set to "true". Services will run on debug mode');
        args.push('-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005,quiet=y');
    }

    const balHomeSysProp = `-Dballerina.home=${ballerinaHome}`;
    const balDebugLogSysProp = `-Dballerina.debugLog=${config.debugLog}`;
    return {
        command: getExcecutable(ballerinaHome),
        args: [balHomeSysProp, balDebugLogSysProp, ...args, main],
        options: {
        }
    };
}