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
 
export default class Traces {
    
    traces: Array<any>;
    constructor() {
        this.traces = [];
    }

    addTrace (trace: any) {
        this.traces.push(trace);
    }

    getTraces(){
        const newTraces = [];
        if (this.traces.length < 2) {
            return this.traces;
        }
        for (let index = 0; index < this.traces.length; index++) {
            const trace = this.traces[index];
            if (!trace.message.payload && trace.message.direction) {
                trace.message.payload = this.getPayLoad(index, trace);
            }
            if (trace.message.headers && trace.message.direction) {
                newTraces.push(trace);
            }
            
        }
        return newTraces;
    };
    getPayLoad(index: number, trace: any): string {
        let nextTraces = this.traces.slice(index, this.traces.length);
        const filteredNextTraces = nextTraces.filter((tmpTrace)=>{
            return tmpTrace.message.payload 
                && trace.message.id === tmpTrace.message.id
                && trace.message.direction === tmpTrace.message.direction
                && trace.logger === tmpTrace.logger;
        });
        if (!filteredNextTraces.length) {
            return "";
        }
        return filteredNextTraces[0].message.payload;
    }
}