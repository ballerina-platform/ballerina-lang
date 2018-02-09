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
import PropTypes from 'prop-types';
import React from 'react';
import Autosuggest from 'react-autosuggest';

const getSuggestionValue = suggestion => suggestion;

const renderSuggestion = suggestion => (
    <div>
        { suggestion }
    </div>
);

const shouldRenderSuggestions = () => true;

class VariableTypeDropdown extends React.Component {
    constructor(props, context) {
        super(props, context);
        this.typeChange = props.onChange;
        this.state = {
            value: this.props.value,
            suggestionPool: context.environment.getTypes(),
            suggestions: [],
        };

        this.onSuggestionsFetchRequested = this.onSuggestionsFetchRequested.bind(this);
        this.onSuggestionsClearRequested = this.onSuggestionsClearRequested.bind(this);
        this.onKeyDown = this.onKeyDown.bind(this);
        this.onSelectionChange = this.onSelectionChange.bind(this);
        this.onChange = this.onChange.bind(this);
    }

    onSuggestionsFetchRequested(query) {
        const matches = [];
        const substrRegex = new RegExp(_.escapeRegExp(query.value), 'i');

        this.state.suggestionPool.forEach((sug) => {
            if (substrRegex.test(sug)) {
                matches.push(sug);
            }
        });
        this.setState({ suggestions: matches });
    }

    onSuggestionsClearRequested() {
        this.setState({
            suggestions: [],
        });
    }

    onKeyDown(event) {
        if (event.keyCode === 13) {
            this.props.onEnter(event);
        }
    }

    onChange(e, selectedVal) {
        this.setState({ value: selectedVal.newValue });
        this.typeChange(selectedVal.newValue);
    }

    onSelectionChange(e, selectedVal) {
        this.setState({ value: selectedVal.suggestion });
        this.typeChange(selectedVal.suggestion);
    }

    render() {
        const inputProps = {
            value: this.state.value,
            onChange: this.onChange,
            placeholder: this.props.placeholder,
        };

        return (
            <Autosuggest
                suggestions={this.state.suggestions}
                onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
                onSuggestionsClearRequested={this.onSuggestionsClearRequested}
                onSuggestionSelected={this.onSelectionChange}
                shouldRenderSuggestions={shouldRenderSuggestions}
                getSuggestionValue={getSuggestionValue}
                renderSuggestion={renderSuggestion}
                inputProps={inputProps}
                focusInputOnSuggestionClick={false}
            />
        );
    }
}

VariableTypeDropdown.propTypes = {
    value: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
};

VariableTypeDropdown.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
};

export default VariableTypeDropdown;
