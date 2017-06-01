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
import React from "react";
import PropTypes from 'prop-types';
import Autosuggest from 'react-autosuggest';
import { util } from './../../visitors/sizing-utils';

/**
 * Autosuggest wrapper for html
 * 
 * @class AutoSuggestHtml
 * @extends {React.Component}
 */
class AutoSuggestHtml extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            inputValue: this.props.initialValue || '',
            suggestions: []
        };

        this.storeInputReference = autosuggest => {
            if (autosuggest !== null) {
                this.input = autosuggest.input;
            }
        };
    }

    /**
     * Renderings the autosuggest component.
     * 
     * @returns A JSX of the component.
     * 
     * @memberof AutoSuggestHtml
     */
    render() {
        // Input properties for the package name
        const inputProps = {
            placeholder: this.props.placeholder,
            value: this.state.inputValue,
            onChange: this.onChange.bind(this),
            onKeyDown: this.props.onKeyDown,
            onBlur: this.props.onBlur,
            style: { width: util.getTextWidth(this.state.inputValue, this.props.minWidth, this.props.maxWidth).w + 5 }
        };

        return <Autosuggest
            suggestions={this.state.suggestions}
            onSuggestionsFetchRequested={this.onSuggestionsFetchRequested.bind(this)}
            onSuggestionsClearRequested={this.onSuggestionsClearRequested.bind(this)}
            onSuggestionSelected={this.props.onSuggestionSelected}
            getSuggestionValue={this.getSuggestionValue.bind(this)}
            renderSuggestion={this.renderSuggestion.bind(this)}
            shouldRenderSuggestions={() => true}
            ref={this.storeInputReference}
            inputProps={inputProps} />;
    }

    /**
     * Event when a text is set.
     * 
     * @param {string} value The value of new input value.
     * 
     * @memberof AutoSuggestHtml
     */
    setInputText(value) {
        this.setState({
            inputValue: value
        });
    }

    /**
     * Event for fetching new suggestions
     * 
     * @param {any} { value } Searched keyword to be filtered with.
     * 
     * @memberof AutoSuggestHtml
     */
    onSuggestionsFetchRequested({ value }) {
        this.setState({
            suggestions: this.getSuggestions(value)
        });
    }

    /**
     * Event for changing input value.
     * 
     * @param {any} event The actualy event.
     * @param {any} { newValue } The new value.
     * 
     * @memberof AutoSuggestHtml
     */
    onChange(event, { newValue }) {
        this.setState({
            inputValue: newValue
        });
    }

    /**
     * When the suggest list is cleared
     * 
     * 
     * @memberof AutoSuggestHtml
     */
    onSuggestionsClearRequested() {
        this.setState({
            suggestions: []
        });
    }

    /**
     * Event when the component is updated. Focus on the input element
     * 
     * @param {any} prevProps 
     * 
     * @memberof AutoSuggestHtml
     */
    componentDidUpdate(prevProps) {
        if (prevProps.show) {
            this.input && this.input.focus();
        }
    }

    /**
     * Event when the component is mounted. Focus on the input element
     * 
     * 
     * @memberof AutoSuggestHtml
     */
    componentDidMount() {
        this.input && this.input.focus();
    }

    // https://developer.mozilla.org/en/docs/Web/JavaScript/Guide/Regular_Expressions#Using_Special_Characters
    escapeRegexCharacters(str) {
        return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    /**
     * Filtering the suggestions
     * 
     * @param {any} searchKeyword 
     * @returns 
     * 
     * @memberof AutoSuggestHtml
     */
    getSuggestions(searchKeyword) {
        const escapedValue = this.escapeRegexCharacters(searchKeyword.trim());

        const regex = new RegExp('^' + escapedValue, 'i');
        let itemsMap = this.props.items.map(item => {
            return {
                name: item
            };
        });

        return itemsMap.filter(item => regex.test(item.name));
    }

    getSuggestionValue(suggestion) {
        return suggestion.name;
    }

    renderSuggestion(suggestion) {
        return (
            <span>{suggestion.name}</span>
        );
    }
}

AutoSuggestHtml.propTypes = {
    placeholder: PropTypes.string,
    value: PropTypes.string,
    onChange: PropTypes.func,
    onKeyDown: PropTypes.func,
    onSuggestionSelected: PropTypes.func,
    items: PropTypes.array.isRequired
};

export default AutoSuggestHtml;
