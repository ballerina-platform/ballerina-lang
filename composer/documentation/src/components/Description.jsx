import React from 'react';
import ReactMarkdown from 'react-markdown/with-html';
import htmlParser from 'react-markdown/plugins/html-parser';
import { diffChars } from 'diff';
import escape from 'escape-html';
import './Description.css';

export default class Description extends React.Component {
    constructor(props) {
        super(props);
        this.diff = [{
            value: props.source,
        }];
    }

    componentWillReceiveProps(newProps) {
        this.diff = diffChars(this.props.source, newProps.source);
    }

    render() {
        let source = '';
        this.diff.forEach((part) => {
            if (part.removed) {
                return;
            }

            const value = escape(part.value);
            if(part.added) {
                source += `<span class="__added">${value}</span>`;
            } else {
                source += value;
            }
        })

        return <ReactMarkdown 
            source={source}
            escapeHtml={false}
            className={this.props.className}
            renderers={{
                inlineCode: block => {
                    const value = block.value.replace(
                        new RegExp('<span class="__added">((?:.|\s)*)<\/span>'), '$1');
                    return <code>{value}</code>
                }
            }}
        />
    }
}