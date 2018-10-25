import React from 'react';
import ReactDOM from 'react-dom';
import App from './components/App';

export default function render(ast, el) {
    ReactDOM.render(<App ast={ast}/>, el);
}
