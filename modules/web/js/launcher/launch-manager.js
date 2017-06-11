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
 */

import _ from 'lodash';
import EventChannel from 'event_channel';
import LaunchChannel from './launch-channel';
import Console from 'console';

class LaunchManager extends EventChannel {
    constructor() {
        super();
        this.enable = false;
        this.channel = undefined;
        this.active = false;
    }

    runApplication(file) {
        this.channel = new LaunchChannel({ endpoint : this.endpoint, launcher: this });
        this.openConsole();
        this.channel.on('connected',_.bindKey(this,'sendRunApplicationMessage',file));
    }

    runService(file) {
        this.channel = new LaunchChannel({ endpoint : this.endpoint, launcher: this });
        this.openConsole();
        this.channel.on('connected', () => { this.sendRunServiceMessage(file); });
    }

    debugApplication(file) {
        this.channel = new LaunchChannel({ endpoint : this.endpoint, launcher: this });
        this.openConsole();
        this.channel.on('connected', () => { this.sendDebugApplicationMessage(file); });
    }

    debugService(file) {
        this.channel = new LaunchChannel({ endpoint : this.endpoint, launcher: this });
        this.openConsole();
        this.channel.on('connected', () => { this.sendDebugServiceMessage(file); });
    }

    sendRunApplicationMessage(file) {
        var message = {
            'command': 'RUN_PROGRAM',
            'fileName' : file.getName(),
            'filePath' : file.getPath(),
            'commandArgs': this.getApplicationConfigs(file)
        };
        this.channel.sendMessage(message);
    }

    sendRunServiceMessage(file) {
        var message = {
            'command': 'RUN_SERVICE',
            'fileName' : file.getName(),
            'filePath' : file.getPath()
        };
        this.channel.sendMessage(message);
    }

    sendDebugApplicationMessage(file) {
        var message = {
            'command': 'DEBUG_PROGRAM',
            'fileName' : file.getName(),
            'filePath' : file.getPath(),
            'commandArgs': this.getApplicationConfigs(file)
        };
        this.channel.sendMessage(message);
    }

    sendDebugServiceMessage(file) {
        var message = {
            'command': 'DEBUG_SERVICE',
            'fileName' : file.getName(),
            'filePath' : file.getPath()
        };
        this.channel.sendMessage(message);
    }

    processMesssage(message) {
        if(message.code === 'OUTPUT'){
            if(_.endsWith(message.message, this.debugPort)){
                this.trigger('debug-active',this.debugEndpoint);
                return;
            }
        }
        if(message.code === 'EXECUTION_STARTED'){
            this.active = true;
            this.trigger('execution-started');
        }
        if(message.code === 'EXECUTION_STOPED' || message.code === 'EXECUTION_TERMINATED'){
            this.active = false;
            this.trigger('execution-ended');
        }
        if(message.code === 'DEBUG_PORT'){
            this.debugPort = message.port;
            return;
        }
        if(message.code === 'EXIT'){
            this.active = false;
            this.trigger('session-ended');
        }
        Console.println(message);
    }

    openConsole() {
        Console.clear();
        Console.show();
    }

    init(options) {
        this.endpoint = _.get(options, 'application.config.services.launcher.endpoint');
        this.debugEndpoint = _.get(options, 'application.config.services.debugger.endpoint');
        this.enable = true;
        this.application = options.application;
        Console.setApplication(this.application);
    }

    stopProgram() {
        var message = {
            'command': 'TERMINATE',
        };
        this.channel.sendMessage(message);
    }

    getApplicationConfigs(file) {
        var args = this.application.browserStorage.get(`launcher-app-configs-${file.id}`);
        return args || '';
    }
}

export default new LaunchManager();
