import TraceLogs from './TraceLogs';
import { createElement } from 'react';
import DetailView from './DetailView';
import { render } from 'react-dom';

export function renderTraceLogs(target: HTMLElement, traces: Array<Object>,  onSelected: Function) {
    const filters = {
        "message.id": "Activity Id",
        "logger": "Logger",
        "message.path": "Path",
        "message.direction": "Inbound/Outbound",
        "message.httpMethod": "Method"
    };
    const props = {
        traces,
        filters,
        onSelected
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
