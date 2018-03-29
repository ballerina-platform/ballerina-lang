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
import ReactDOM from 'react-dom';
import PropTypes from 'prop-types';

class EditableText extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            editing: false,
        };

        const textBoxPadding = 3;
        const textBoxBorderSz = 2;

        this.inputStyles = {
            position: 'absolute',
            paddingLeft: textBoxPadding,
            borderWidth: textBoxBorderSz,
        };

        this.renderTextBox = this.renderTextBox.bind(this);
    }

    componentDidMount() {
        this.renderTextBox();
    }

    componentDidUpdate(prevProps) {
        const editingJustFinished = prevProps.editing && !this.props.editing;
        if (this.props.editing || editingJustFinished) {
            this.renderTextBox();
        }
    }

    componentWillUnmount() {
        const overlay = this.context.getOverlayContainer();
        if (overlay) {
            ReactDOM.render(<noscript />, overlay);
        }
    }

    renderTextBox() {
        const {
            x, y, width, height = 25, onChange, onBlur, onKeyDown, children = '',
            inputClass = 'editable-text-input', placeholder,
        } = this.props;

        const inputPositionStyles = {
            top: (y - height / 2) - 4,
            left: x,
            width,
            height,
        };

        const styles = {};

        if (!this.props.editing) {
            styles.display = 'none';
        }

        Object.assign(styles, this.inputStyles, inputPositionStyles);

        const inputProps = {
            ref: (input) => {
                if (input !== null) {
                    input.focus();
                }
            },
            style: styles,
            onChange,
            onKeyDown,
            onBlur,
            placeholder,
            value: children,
        };

        const inputElement = (<input {...inputProps} className={inputClass} />);

        ReactDOM.render(inputElement, this.context.getOverlayContainer());
    }

    render() {
        let {
            x, y, onClick, labelClass = 'editable-text-label', displayText, children,
        } = this.props;

        // This takes the label to left so that when the textInput is rendered over it
        // it does not make it look like the text just jumped to the right
        // x += this.inputStyles.borderWidth + this.inputStyles.paddingLeft;

        const textProps = { x, y, onClick };
        textProps.style = {
            dominantBaseline: 'left',
        };

        return (
            <text {...textProps} className={labelClass}>{ displayText || children }</text>
        );
    }
}

EditableText.contextTypes = {
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
};

export default EditableText;
