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
            editing: false
        };

        this.renderTextBox = this.renderTextBox.bind(this);
    }

    renderTextBox() {
        const {x, y, width, height = 25, onChange, onBlur, children, className = 'text-input', placeHolder} = this.props;
        const inputStyle = {
            position: 'absolute',
            top: y - height / 2,
            left: x,
            width: width,
            height: height
        };

        if (!this.props.editing) {
            inputStyle.display = 'none';
        }

        const inputElement = (
            <input
                className={className}
                ref={input => {
                    if (input != null) {
                        input.focus();
                    }
                }}
                style={inputStyle}
                onChange={onChange}
                onBlur={onBlur}
                value={children}
                placeholder={placeHolder}
            />
        );
        ReactDOM.render(inputElement, this.context.overlay);
    }

    componentDidUpdate(prevProps) {
        const editingJustFinished = prevProps.editing && !this.props.editing
        if(this.props.editing || editingJustFinished) {
            this.renderTextBox();
        }
    }

    componentDidMount() {
        this.renderTextBox();
    }

    componentWillUnmount() {
        ReactDOM.render(<noscript/>, this.context.overlay);
    }

    render() {
        let {x, y, onClick} = this.props;
        const textProps = {x, y, onClick};
        textProps.style = {
            dominantBaseline: 'central'
        }

        if (this.props.placeHolder) {
            return (
                <text {...textProps} className="panel-label">{this.props.placeHolder}</text>
            )
        } else {

            return (
                <text {...textProps} className="panel-label">{this.props.children}</text>
            )
        }

    }
}

EditableText.contextTypes = {
    overlay: PropTypes.instanceOf(Object).isRequired,
};

export default EditableText;
