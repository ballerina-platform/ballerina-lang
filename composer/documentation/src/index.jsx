import React from 'react';
import ReactDOM from 'react-dom';
import App from './components/App';

export default function render(ast, el) {
    console.log(el)
    ReactDOM.render(<App ast={ast}/>, el);
}
