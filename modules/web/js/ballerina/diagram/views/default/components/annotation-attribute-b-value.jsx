
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
import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';
import BValueAST from './../ast/b-value';

/**
 * React component of a b-value that is being used in an annotation attribute.
 *
 * @class AnnotationAttributeBValue
 * @extends {React.Component}
 */
class AnnotationAttributeBValue extends React.Component {
    /**
     * Creates an instance of AnnotationAttributeBValue.
     * @param {Object} props React properties.
     * @memberof AnnotationAttributeBValue
     */
    constructor(props) {
        super(props);
        if (_.trim(props.model.getStringValue(), '"') === '') {
            this.state = {
                isInEdit: true,
                bValueText: '',
                focusBValueInput: false,
            };
        } else {
            this.state = {
                isInEdit: false,
                bValueText: this.props.model.getStringValue(),
                focusBValueInput: false,
            };
        }

        this.onBValueChange = this.onBValueChange.bind(this);
        this.onBValueEditFinished = this.onBValueEditFinished.bind(this);
        this.onEdit = this.onEdit.bind(this);
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
     * @memberof AnnotationAttributeBValue
     */
    onBValueChange(e) {
        this.setState({
            bValueText: e.target.value,
        });
    }

    /**
     * Event when b-value editing is finished.
     * @memberof AnnotationAttributeBValue
     */
    onBValueEditFinished() {
        this.props.model.setStringValue(this.state.bValueText);
        this.setState({
            isInEdit: false,
            focusBValueInput: false,
        });
    }

    /**
     * Event when b-value starts editing.
     * @memberof AnnotationAttributeBValue
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
     * @memberof AnnotationAttributeBValue
     */
    render() {
        if (this.state.isInEdit) {
            return (<input
                ref={(ref) => { this.bValueInput = ref; }}
                type='text'
                value={this.state.bValueText}
                onChange={this.onBValueChange}
                onBlur={this.onBValueEditFinished}
            />);
        }

        return <span onClick={this.onEdit}>{this.state.bValueText}</span>;
    }
}

AnnotationAttributeBValue.propTypes = {
    model: PropTypes.instanceOf(BValueAST).isRequired,
};

export default AnnotationAttributeBValue;
