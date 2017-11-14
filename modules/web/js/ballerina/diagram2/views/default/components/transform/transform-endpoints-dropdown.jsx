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
 *
 */

import _ from 'lodash';
import PropTypes from 'prop-types';
import React from 'react';
import Autosuggest from 'react-autosuggest';

const getSuggestionValue = suggestion => suggestion;

const renderSuggestion = suggestion => (
    <div>
        {suggestion}
    </div>
);

const shouldRenderSuggestions = () => true;

class SuggestionsDropdown extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            value: '',
            suggestions: this.props.suggestionsPool,
        };

        this.onSuggestionsFetchRequested = this.onSuggestionsFetchRequested.bind(this);
        this.onSuggestionsClearRequested = this.onSuggestionsClearRequested.bind(this);
        this.onKeyDown = this.onKeyDown.bind(this);
    }

    onSuggestionsFetchRequested(query) {
        const matches = [];
        const substrRegex = new RegExp(_.escapeRegExp(query.value), 'i');

        this.props.suggestionsPool.forEach((sug) => {
            if (substrRegex.test(sug)) {
                matches.push(sug);
            }
        });
        // matches.push({ type: '-- add new variable --', name: '' });
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

    render() {
        const { placeholder, value, onChange } = this.props;

        const inputProps = {
            placeholder,
            value,
            onChange,
            onKeyDown: this.onKeyDown,
        };

        return (
            <Autosuggest
                suggestions={this.state.suggestions}
                onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
                onSuggestionsClearRequested={this.onSuggestionsClearRequested}
                onSuggestionSelected={this.props.onSuggestionSelected}
                shouldRenderSuggestions={shouldRenderSuggestions}
                getSuggestionValue={getSuggestionValue}
                renderSuggestion={renderSuggestion}
                inputProps={inputProps}
                focusInputOnSuggestionClick={false}
            />
        );
    }
}

SuggestionsDropdown.propTypes = {
    placeholder: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    onSuggestionSelected: PropTypes.instanceOf(Object).isRequired,
    suggestionsPool: PropTypes.instanceOf(Object).isRequired,
    onEnter: PropTypes.func.isRequired,
};

export default SuggestionsDropdown;
