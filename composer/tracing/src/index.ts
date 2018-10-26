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
 */
import TraceLogs from './TraceLogs';
import { createElement } from 'react';
import DetailView from './DetailView';
import { render } from 'react-dom';

export function renderTraceLogs(target: HTMLElement, traces: Array<Object>,  onSelected: Function, clearLogs: Function) {
    const filters = {
        "message.id": "Activity Id",
        "logger": "Logger",
        "message.path": "Path",
        "message.direction": "Inbound/Outbound",
        "message.httpMethod": "Method"
    };
    const props = {
        traces: filterEmptyLogs(mergeRelatedMessages(traces)),
        filters,
        onSelected,
        clearLogs,
    };
    target.classList.add('composer-library');
    const traceLogsElement = createElement(TraceLogs, props);
    render(traceLogsElement, target);
}

export function renderDetailedTrace(target: HTMLElement, trace: string) {
    const props = {
        trace: JSON.parse(decodeURIComponent(trace)),
    };
    target.classList.add('composer-library');
    const traceLogsElement = createElement(DetailView, props);
    render(traceLogsElement, target);
}

function mergeRelatedMessages(traces: Array<any>) {
    const newTraces = [];
    if (traces.length < 2) {
        return traces;
    }
    for (let index = 0; index < traces.length; index++) {
        let record1 = traces[index];
        let record2 = traces[index + 1];
        if (record1.message.headerType.startsWith('DefaultHttpRequest')
            && record2
            && record1.thread === record2.thread
            && (record2.message.headerType.startsWith('DefaultLastHttpContent')
                || record2.message.headerType.startsWith('EmptyLastHttpContent'))) {

            record1.message.payload = record2.message.payload;
            newTraces.push(record1);
        } else if (record1.message.headerType.startsWith('DefaultLastHttpContent') ||
            record1.message.headerType.startsWith('EmptyLastHttpContent')) {
            // do nothing
        } else {
            newTraces.push(record1);
        }
    }
    return newTraces;
}

function filterEmptyLogs(traces: Array<any>) {
    return traces.filter((trace: any) => {
        if (trace.message.headers.trim() === "" && trace.message.payload.trim() === "") {
            return false;
        }
        const direction = trace.message.direction || "";
        return direction.length > 0;
    });
}