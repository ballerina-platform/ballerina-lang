/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React from 'react';
import PropTypes from 'prop-types';
import {COMMANDS} from '../../constants';

class DefaultDecorator extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            message: props.message.message,
            value: props.message.message,
        };
    }

    focusTextInput(element, text) {
        // Focus the text input using the raw DOM API
        if (element) {
            element.innerHTML = text;
            element.focus();
            if (typeof window.getSelection !== "undefined"
                && typeof document.createRange !== "undefined") {
                let range = document.createRange();
                range.selectNodeContents(element);
                range.collapse(false);
                let sel = window.getSelection();
                sel.removeAllRanges();
                sel.addRange(range);
            } else if (typeof document.body.createTextRange !== "undefined") {
                let textRange = document.body.createTextRange();
                textRange.moveToElementText(element);
                textRange.collapse(false);
                textRange.select();
            }
        }
    };

    handleKeyDown(event) {
        let key = event.key || event.keyCode;
        let currentValue = event.target.innerText;
        if (key === 'Enter' || key === 13) {
            let userInput = currentValue.replace(this.state.message, '');
            this.props.command.dispatch(COMMANDS.INPUT, userInput);
            event.preventDefault();
        }
    }

    handleKeyUp(event) {
        let key = event.key || event.keyCode;
        if ((key === 'Enter' || key === 13)) {
            return;
        }
        let currentValue = event.target.innerText;
        let userInput = currentValue.replace(this.state.message, '');
        if ((!currentValue.startsWith(this.state.message) || currentValue.length < this.state.message)) {
            this.focusTextInput(event.target, '<pre>' + this.state.message + '<pre>');
            // Ignore event
            event.preventDefault();
        } else {
            this.focusTextInput(event.target, '<pre>' + this.state.message +
                                      '<span style="color: palegreen"><i>' + userInput + '</i><span>' + '<pre>');
        }
    }

    /**
     * Renders the message.
     * @returns {ReactElement} The view.
     * @memberof DefaultDecorator
     */
    render() {
        let message = this.props.message;
        let contentEditable = message.type === 'DATA' && this.props.isLast;
        return (<div className={message.type} contentEditable={contentEditable}
                     suppressContentEditableWarning={contentEditable}
                     onKeyDown={(event) => this.handleKeyDown(event)}
                     onKeyUp={(event) => this.handleKeyUp(event)}>
            <pre>{this.state.value}</pre>
        </div>);
    }
}

DefaultDecorator.propTypes = {
    message: PropTypes.instanceOf(Object).isRequired,
};

export default DefaultDecorator;
