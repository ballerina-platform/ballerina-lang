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
import { createElement } from "react";
import { render } from "react-dom";
import DetailView from "./DetailView";
import TraceLogs from "./TraceLogs";

export function renderTraceLogs(
    target: HTMLElement,
    traces: object[],
    onSelected: (record: any) => void,
    clearLogs: () => void) {
    const filters = {
        "logger": "Logger",
        "message.direction": "Inbound/Outbound",
        "message.httpMethod": "Method",
        "message.id": "Activity Id",
        "message.path": "Path",
    };
    const props = {
        clearLogs,
        filters,
        onSelected,
        traces,
    };
    target.classList.add("composer-library");
    const traceLogsElement = createElement(TraceLogs, props);
    render(traceLogsElement, target);
}

export function renderDetailedTrace(target: HTMLElement, trace: string) {
    const props = {
        trace: JSON.parse(decodeURIComponent(trace)),
    };
    target.classList.add("composer-library");
    const traceLogsElement = createElement(DetailView, props);
    render(traceLogsElement, target);
}
