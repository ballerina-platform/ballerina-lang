import * as React from 'react';
import { storiesOf } from '@storybook/react';
import 'semantic-ui-css/semantic.min.css';
import TraceList from '../src/TraceList';

const traces = [{
    "id": "uuid1",
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
        "id" : "id1",
        "direction" : "direction",
        "headers" : "headers",
        "httpMethod" : "GET",
        "path" : "/path",
        "contentType" : "contentType",
        "payload" : "payload",
        "headerType" : "headerType",
    }
    
},
{
    "id": "uuid2",
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
        "id" : "id2",
        "direction" : "direction",
        "headers" : "headers",
        "httpMethod" : "POST",
        "path" : "/path2",
        "contentType" : "contentType",
        "payload" : "payload",
        "headerType" : "headerType",
    }
},
{
    "id": "uuid3",
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
        "id" : "id3",
        "direction" : "direction",
        "headers" : "headers",
        "httpMethod" : "POST",
        "path" : "/path3",
        "contentType" : "contentType",
        "payload" : "payload",
        "headerType" : "headerType",
    }
},
{
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
        "id" : "id4",
        "direction" : "direction",
        "headers" : "headers",
        "httpMethod" : "POST",
        "path" : "/path4",
        "contentType" : "contentType",
        "payload" : "payload",
        "headerType" : "headerType",
    }
}
];

storiesOf('trace list', module)
  .add('list traces', () => {
    return (<TraceList traces={traces} selected="" />);
  })
  .add('list traces selected', () => {
    return (<TraceList traces={traces} selected='uuid1' />);
  });

