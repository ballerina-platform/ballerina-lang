import React from 'react';
import ReactDOM from 'react-dom';
import DocPreview from './components/App';

export function renderDocPreview(ast, el) {
    ReactDOM.render(<DocPreview ast={ast}/>, el);
}
