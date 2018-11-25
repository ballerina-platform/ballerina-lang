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

import { storiesOf } from "@storybook/react";
import * as React from "react";
/* tslint:disable:no-submodule-imports */
import "semantic-ui-css/semantic.min.css";
import TraceLogs from "../src/TraceLogs";
import "./detailView";
import "./toolbar";
import "./trace-list";

const traces: any = [{
    id: "5cfc3200-b742-44d8-a27b-55deccf91b58",
    level: "FINEST",
    logDate: "2018-10-25T19:45Z",
    logger: "http.tracelog.downstream",
    message: {
        contentType: "",
        direction: "",
        headerType: "",
        headers: "",
        httpMethod: "",
        id: "0xdb0d6c88",
        path: "",
        payload: ""
    },
    millis: "1540476938205",
    rawMessage: "[id: 0xdb0d6c88] REGISTERED",
    sequence: "0",
    sourceClass: "io.netty.util.internal.logging.Slf4JLogger",
    sourceMethod: "trace",
    thread: "19"
}, {
    id: "5cfc3200-b742-44d8-a27b-55deccf91b59",
    level: "FINEST",
    logDate: "2018-10-25T19:45Z",
    logger: "http.tracelog.downstream",
    message: {
        contentType: "",
        direction: "",
        headerType: "",
        headers: "",
        httpMethod: "",
        id: "0xdb0d6c88",
        path: "",
        payload: ""
    },
    millis: "1540476938205",
    rawMessage: "[id: 0xdb0d6c88] REGISTERED",
    sequence: "0",
    sourceClass: "io.netty.util.internal.logging.Slf4JLogger",
    sourceMethod: "trace",
    thread: "19"
}, {
    id: "5cfc3200-b742-44d8-a27b-55deccf91b56",
    level: "FINEST",
    logDate: "2018-10-25T19:45Z",
    logger: "http.tracelog.downstream",
    message: {
        contentType: "",
        direction: "",
        headerType: "",
        headers: "",
        httpMethod: "",
        id: "0xdb0d6c88",
        path: "",
        payload: ""
    },
    millis: "1540476938205",
    rawMessage: "[id: 0xdb0d6c88] REGISTERED",
    sequence: "0",
    sourceClass: "io.netty.util.internal.logging.Slf4JLogger",
    sourceMethod: "trace",
    thread: "19"
}, {
    id: "6cfc3200-b742-44d8-a27b-55deccf91b57",
    level: "FINEST",
    logDate: "2018-10-25T19:45Z",
    logger: "http.tracelog.downstream",
    message: {
        contentType: "",
        direction: "",
        headerType: "",
        headers: "",
        httpMethod: "",
        id: "0xdb0d6c88",
        path: "",
        payload: ""
    },
    millis: "1540476938205",
    rawMessage: "[id: 0xdb0d6c88] REGISTERED",
    sequence: "0",
    sourceClass: "io.netty.util.internal.logging.Slf4JLogger",
    sourceMethod: "trace",
    thread: "19"
}, {
    id: "7cfc3200-b742-44d8-a27b-55deccf91b57",
    level: "FINEST",
    logDate: "2018-10-25T19:45Z",
    logger: "http.tracelog.downstream",
    message: {
        contentType: "",
        direction: "",
        headerType: "",
        headers: "",
        httpMethod: "",
        id: "0xdb0d6c88",
        path: "",
        payload: ""
    },
    millis: "1540476938205",
    rawMessage: "[id: 0xdb0d6c88] REGISTERED",
    sequence: "0",
    sourceClass: "io.netty.util.internal.logging.Slf4JLogger",
    sourceMethod: "trace",
    thread: "19"
}];

const filters = {
    "logger": "Logger",
    "message.direction": "Inbound/Outbound",
    "message.httpMethod": "Method",
    "message.id": "Activity Id",
    "message.path": "Path",
};

storiesOf("trace logs", module)
    .add("Trace Logs toolbar and component", () => {
        return (<TraceLogs traces={traces} filters={filters} clearLogs={() => undefined } />);
    });
