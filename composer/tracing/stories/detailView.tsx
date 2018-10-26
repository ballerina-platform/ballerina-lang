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
