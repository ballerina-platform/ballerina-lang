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

import * as React from 'react';
import { storiesOf } from '@storybook/react';
import DetailView from '../src/DetailView';

storiesOf('Detail view', module)
    .add('detail view', () => {
        let trace: any;
        trace = {
            "id": "uuid4",
            "logDate": "2018-09-18T18:51Z",
            "millis": "1537276895575",
            "sequence": "1",
            "logger": "http.tracelog.downstream",
            "level": "FINEST",
            "sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
            "sourceMethod": "trace",
            "thread": "15",
            "rawMessage": "[id: 0x838f2718, correlatedSource: n/a, host:/0:0:0:0:0:0:0:1:9090 - remote:/0:0:0:0:0:0:0:1:46220] ACTIVE",
            "message": {
                "id": "id4",
                "direction": "direction",
                "headers": "headers",
                "httpMethod": "POST",
                "path": "/path4",
                "contentType": "application/json",
                "payload": '{"foo": "bar"}',
                "headerType": "headerType",

            }
        };
            return(<DetailView trace = { trace } />);
});
