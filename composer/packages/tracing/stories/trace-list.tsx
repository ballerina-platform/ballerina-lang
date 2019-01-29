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
import TraceList from "../src/TraceList";

/* tslint:disable:max-line-length */
const traces = [{
    id: "uuid1",
    level: "FINEST",
    logDate: "2018-09-18T18:51Z",
    logger: "http.tracelog.downstream",
    message: {
        contentType : "contentType",
        direction : "direction",
        headerType : "headerType",
        headers : "headers",
        httpMethod : "GET",
        id : "id1",
        path : "/path",
        payload : "payload",
    },
    millis: "1537276895575",
    rawMessage: "[id: 0x838f2718, correlatedSource: n/a, host:/0:0:0:0:0:0:0:1:9090 - remote:/0:0:0:0:0:0:0:1:46220] ACTIVE",
    sequence: "1",
    sourceClass: "io.netty.util.internal.logging.Slf4JLogger",
    sourceMethod: "trace",
    thread: "15",
},
{
    id: "uuid2",
    logDate: "2018-09-18T18:51Z",

    level: "FINEST",
    logger: "http.tracelog.downstream",
    message: {
        contentType : "contentType",
        direction : "direction",
        headerType : "headerType",
        headers : "headers",
        httpMethod : "POST",
        id : "id2",
        path : "/path2",
        payload : "payload",
    },
    millis: "1537276895575",
    rawMessage: "[id: 0x838f2718, correlatedSource: n/a, host:/0:0:0:0:0:0:0:1:9090 - remote:/0:0:0:0:0:0:0:1:46220] ACTIVE",
    sequence: "1",
    sourceClass: "io.netty.util.internal.logging.Slf4JLogger",
    sourceMethod: "trace",
    thread: "15",
},
{
    id: "uuid3",
    logDate: "2018-09-18T18:51Z",

    level: "FINEST",
    logger: "http.tracelog.downstream",
    message: {
        contentType : "contentType",
        direction : "direction",
        headerType : "headerType",
        headers : "headers",
        httpMethod : "POST",
        id : "id3",
        path : "/path3",
        payload : "payload",
    },
    millis: "1537276895575",
    rawMessage: "[id: 0x838f2718, correlatedSource: n/a, host:/0:0:0:0:0:0:0:1:9090 - remote:/0:0:0:0:0:0:0:1:46220] ACTIVE",
    sequence: "1",
    sourceClass: "io.netty.util.internal.logging.Slf4JLogger",
    sourceMethod: "trace",
    thread: "15",
},
{
    id: "uuid4",
    logDate: "2018-09-18T18:51Z",

    level: "FINEST",
    logger: "http.tracelog.downstream",
    message: {
        contentType : "contentType",
        direction : "direction",
        headerType : "headerType",
        headers : "headers",
        httpMethod : "POST",
        id : "id4",
        path : "/path4",
        payload : "payload",
    },
    millis: "1537276895575",
    rawMessage: "[id: 0x838f2718, correlatedSource: n/a, host:/0:0:0:0:0:0:0:1:9090 - remote:/0:0:0:0:0:0:0:1:46220] ACTIVE",
    sequence: "1",
    sourceClass: "io.netty.util.internal.logging.Slf4JLogger",
    sourceMethod: "trace",
    thread: "15",
},
];

storiesOf("trace list", module)
  .add("list traces", () => {
    return (<TraceList traces={traces} onSelected={() => undefined } />);
  })
  .add("list traces selected", () => {
    return (<TraceList traces={traces} onSelected={() => undefined } />);
  });
