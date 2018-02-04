
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
import SimpleVariableRefTreeNode from 'plugins/ballerina/model/tree/simple-variable-ref-node';

/**
 * React component of a b-value that is being used in an annotation attribute.
 *
 * @class AnnotationAttributeBValue
 * @extends {React.Component}
 */
class AnnotationAttributeVariableRefValue extends React.Component {
    /**
     * Creates an instance of AnnotationAttributeVariableRefValue.
     * @param {Object} props React properties.
     * @memberof AnnotationAttributeVariableRefValue
     */
    constructor(props) {
        super(props);
        this.state = {
            isInEdit: this.props.model.getVariableName().getValue() === undefined ||
                                                        this.props.model.getVariableName().getValue().trim() === '',
            bValueText: this.props.model.getVariableName().getValue(),
            focusBValueInput: this.props.model.getVariableName().getValue() === undefined ||
                                                        this.props.model.getVariableName().getValue().trim() === '',
        };

        this.bValueInput = undefined;
        this.onBValueChange = this.onBValueChange.bind(this);
        this.onBValueEditFinished = this.onBValueEditFinished.bind(this);
        this.onEdit = this.onEdit.bind(this);
        this.onBValueKeyPress = this.onBValueKeyPress.bind(this);
    }

    /**
     * @override
     */
    componentDidMount() {
        if (this.bValueInput && this.state.focusBValueInput) {
            this.bValueInput.focus();
        }
    }

    /**
     * @override
     */
    componentDidUpdate() {
        if (this.bValueInput && this.state.focusBValueInput) {
            this.bValueInput.focus();
        }
    }

    /**
     * Event when b-value is changed.
     *
     * @param {Object} e The event.
     * @memberof AnnotationAttributeVariableRefValue
     */
    onBValueChange(e) {
        this.setState({
            bValueText: e.target.value,
        });
    }

    /**
     * Event when b-value editing is finished.
     * @memberof AnnotationAttributeVariableRefValue
     */
    onBValueEditFinished() {
        this.props.model.getVariableName().setValue(this.state.bValueText);
        this.setState({
            isInEdit: false,
            focusBValueInput: false,
        });
        if (this.state.bValueText === undefined || this.state.bValueText.trim() === '') {
            this.props.model.parent.parent.removeValueArray(this.props.model.parent);
        }
    }

    /**
     * Event when enter key is pressed.
     * @param {Object} e The event.
     * @memberof AnnotationAttributeVariableRefValue
     */
    onBValueKeyPress(e) {
        if (e.charCode === 13 || e.key === 'Enter') {
            this.onBValueEditFinished();
        }
    }

    /**
     * Event when b-value starts editing.
     * @memberof AnnotationAttributeVariableRefValue
     */
    onEdit() {
        this.setState({
            isInEdit: true,
            focusBValueInput: true,
        });
    }

    /**
     * Rendering the view for a b-value.
     *
     * @returns {ReactElement} The view.
     * @memberof AnnotationAttributeVariableRefValue
     */
    render() {
        if (this.state.isInEdit) {
            return (<input
                className='annotation-attribute-b-value-input'
                ref={(ref) => { this.bValueInput = ref; }}
                type='text'
                value={this.state.bValueText}
                onChange={this.onBValueChange}
                onBlur={this.onBValueEditFinished}
                onKeyPress={this.onBValueKeyPress}
            />);
        }

        return <span onClick={this.onEdit}>{this.state.bValueText}</span>;
    }
}

AnnotationAttributeVariableRefValue.propTypes = {
    model: PropTypes.instanceOf(SimpleVariableRefTreeNode).isRequired,
};

export default AnnotationAttributeVariableRefValue;
