import React from 'react';
import ReactDOM from 'react-dom';
import DocPreview from './components/DocPreview';

export function renderDocPreview(ast, el) {
    ReactDOM.render(<DocPreview ast={ast}/>, el);
}
