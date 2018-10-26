import * as React from 'react';
import { storiesOf } from '@storybook/react';
import 'semantic-ui-css/semantic.min.css';
import './toolbar';
import './trace-list';
import './detailView';
import TraceLogs from '../src/TraceLogs';

const traces:any = [{
	"id": "5cfc3200-b742-44d8-a27b-55deccf91b58",
	"level": "FINEST",
	"logDate": "2018-10-25T19:45Z",
	"logger": "http.tracelog.downstream",
	"message": {
		"contentType": "",
		"direction": "",
		"headerType": "",
		"headers": "",
		"httpMethod": "",
		"id": "0xdb0d6c88",
		"path": "",
		"payload": ""
	},
	"millis": "1540476938205",
	"rawMessage": "[id: 0xdb0d6c88] REGISTERED",
	"sequence": "0",
	"sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
	"sourceMethod": "trace",
	"thread": "19"
}, {
	"id": "5cfc3200-b742-44d8-a27b-55deccf91b59",
	"level": "FINEST",
	"logDate": "2018-10-25T19:45Z",
	"logger": "http.tracelog.downstream",
	"message": {
		"contentType": "",
		"direction": "",
		"headerType": "",
		"headers": "",
		"httpMethod": "",
		"id": "0xdb0d6c88",
		"path": "",
		"payload": ""
	},
	"millis": "1540476938205",
	"rawMessage": "[id: 0xdb0d6c88] REGISTERED",
	"sequence": "0",
	"sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
	"sourceMethod": "trace",
	"thread": "19"
}, {
	"id": "5cfc3200-b742-44d8-a27b-55deccf91b56",
	"level": "FINEST",
	"logDate": "2018-10-25T19:45Z",
	"logger": "http.tracelog.downstream",
	"message": {
		"contentType": "",
		"direction": "",
		"headerType": "",
		"headers": "",
		"httpMethod": "",
		"id": "0xdb0d6c88",
		"path": "",
		"payload": ""
	},
	"millis": "1540476938205",
	"rawMessage": "[id: 0xdb0d6c88] REGISTERED",
	"sequence": "0",
	"sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
	"sourceMethod": "trace",
	"thread": "19"
}, {
	"id": "6cfc3200-b742-44d8-a27b-55deccf91b57",
	"level": "FINEST",
	"logDate": "2018-10-25T19:45Z",
	"logger": "http.tracelog.downstream",
	"message": {
		"contentType": "",
		"direction": "",
		"headerType": "",
		"headers": "",
		"httpMethod": "",
		"id": "0xdb0d6c88",
		"path": "",
		"payload": ""
	},
	"millis": "1540476938205",
	"rawMessage": "[id: 0xdb0d6c88] REGISTERED",
	"sequence": "0",
	"sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
	"sourceMethod": "trace",
	"thread": "19"
}, {
	"id": "7cfc3200-b742-44d8-a27b-55deccf91b57",
	"level": "FINEST",
	"logDate": "2018-10-25T19:45Z",
	"logger": "http.tracelog.downstream",
	"message": {
		"contentType": "",
		"direction": "",
		"headerType": "",
		"headers": "",
		"httpMethod": "",
		"id": "0xdb0d6c88",
		"path": "",
		"payload": ""
	},
	"millis": "1540476938205",
	"rawMessage": "[id: 0xdb0d6c88] REGISTERED",
	"sequence": "0",
	"sourceClass": "io.netty.util.internal.logging.Slf4JLogger",
	"sourceMethod": "trace",
	"thread": "19"
}];

const filters = {
    "message.id": "Activity Id",
    "logger": "Logger",
    "message.path": "Path",
    "message.direction": "Inbound/Outbound",
    "message.httpMethod": "Method"
};

storiesOf('trace logs', module)
    .add('Trace Logs toolbar and component', () => {
        return (<TraceLogs traces={traces} filters={filters} clearLogs={()=>{}}/>);
    });
