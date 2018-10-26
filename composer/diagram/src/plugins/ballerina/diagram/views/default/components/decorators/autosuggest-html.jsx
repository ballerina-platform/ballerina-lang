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
import Autosuggest from 'react-autosuggest';
import { util } from './../../sizing-util';

/**
 * AutoSuggest wrapper for html
 *
 * @class AutoSuggestHtml
 * @extends {React.Component}
 */
class AutoSuggestHtml extends React.Component {
    /**
     * Constructor for AutoSuggestHtml.
     *
     * @param {object} props - props for AutoSuggestHtml component.
     * */
    constructor(props) {
        super(props);
        this.state = {
            inputValue: this.props.initialValue || '',
            suggestions: [],
            showAllAtStart: this.props.showAllAtStart,
        };

        this.storeInputReference = (autosuggest) => {
            if (autosuggest !== null) {
                this.input = autosuggest.input;
            }
        };

        this.onChange = this.onChange.bind(this);
        this.onSuggestionsFetchRequested = this.onSuggestionsFetchRequested.bind(this);
        this.onSuggestionsClearRequested = this.onSuggestionsClearRequested.bind(this);
        this.getSuggestionValue = this.getSuggestionValue.bind(this);
        this.renderSuggestion = this.renderSuggestion.bind(this);
    }

    /**
     * Event when the component is mounted. Focus on the input element
     *
     * @return {boolean} is input mounted
     * @memberOf AutoSuggestHtml
     */
    componentDidMount() {
        if (!this.props.disableAutoFocus) {
            return this.input && this.input.focus();
        }
        return this.input;
    }

    /**
     * Event when the component is updated. Focus on the input element
     *
     * @param {any} prevProps - Previous props
     * @return {object} input
     * @memberOf AutoSuggestHtml
     */
    componentDidUpdate(prevProps) {
        let input = null;
        if (prevProps.show) {
            input = this.input && this.input.focus();
        }
        return input;
    }

    /**
     * Event for fetching new suggestions
     *
     * @param {any} value  Searched keyword to be filtered with.
     * @return {object} state
     * @memberOf AutoSuggestHtml
     */
    onSuggestionsFetchRequested({ value }) {
        return this.setState({
            suggestions: this.getSuggestions(value),
        });
    }

    /**
     * Event for changing input value.
     *
     * @param {any} event The actualy event.
     * @param {any} newValue  The new value.
     * @return {object} state.
     * @memberOf AutoSuggestHtml
     */
    onChange(event, { newValue }) {
        this.setState({
            inputValue: newValue,
            showAllAtStart: false,
        });
        this.props.onChange(event, { newValue });
    }

    /**
     * When the suggest list is cleared
     *
     * @return {object} state
     * @memberOf AutoSuggestHtml
     */
    onSuggestionsClearRequested() {
        return this.setState({
            suggestions: [],
        });
    }

    /**
     * Event when a text is set.
     *
     * @param {string} value The value of new input value.
     * @return {object} state
     * @memberOf AutoSuggestHtml
     */
    setInputText(value) {
        return this.setState({
            inputValue: value,
        });
    }

    /**
     * Filtering the suggestions
     *
     * @param {any} searchKeyword - key word to invoke the search on.
     * @returns {collection} filtered items.
     * @memberOf AutoSuggestHtml
     */
    getSuggestions(searchKeyword) {
        if (this.state.showAllAtStart) {
            const itemsMap = this.props.items.map(item => ({
                name: item,
            }));
            return itemsMap;
        }

        const escapedValue = this.escapeRegexCharacters(searchKeyword.trim());
        const itemsMap = this.props.items.map(item => ({
            name: item,
        }));
        return itemsMap.filter(item => item.name.toLowerCase().includes(escapedValue.toLowerCase()));
    }

    /**
     * Get suggestion value.
     * @param {string} suggestion - suggestion key word.
     * @return {string} complete suggestion.
     *
     * @memberOf AutoSuggestHtml
     * */
    getSuggestionValue(suggestion) {
        return suggestion.name;
    }

    /**
     * escape regex characters.
     * https://developer.mozilla.org/en/docs/Web/JavaScript/Guide/Regular_Expressions#Using_Special_Characters
     *
     * @param {string} str - string to format.
     * @return {string} formatted string.
     *
     * @memberOf AutoSuggestHtml
     * */
    escapeRegexCharacters(str) {
        return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    /**
     * Render Suggestions.
     *
     * @param {object} suggestion - suggestion
     * @return {object} react component.
     *
     * @memberOf AutoSuggestHtml
     * */
    renderSuggestion(suggestion) {
        const value = this.state.inputValue;
        const sugName = suggestion.name;
        const parts = sugName.split(value);
        const highlightedString = [];

        parts.forEach((p, i) => {
            highlightedString.push(<span key={i}>{p}</span>);
            if (i < parts.length - 1) {
                highlightedString.push(<span key={i + 100}><b>{value}</b></span>);
            }
        });

        return (
            <span>
                {highlightedString}
            </span>
        );
    }

    /**
     * Renderings the AutoSuggest component.
     *
     * @returns {object} A JSX of the component.
     *
     * @memberOf AutoSuggestHtml
     */
    render() {
        // Input properties for the package name
        const inputProps = {
            placeholder: this.props.placeholder,
            value: this.state.inputValue,
            onChange: this.onChange,
            onKeyDown: (e) => {
                if (this.props.onKeyDown) {
                    this.props.onKeyDown(e);
                }
                this.setState({
                    showAllAtStart: false,
                });
            },
            onClick: (e) => { e.stopPropagation(); },
            onBlur: (e) => {
                if (this.props.onBlur) {
                    this.props.onBlur(e);
                }
                if (this.props.showAllAtStart === true) {
                    this.setState({
                        showAllAtStart: true,
                    });
                }
            },
        };

        return (
            <Autosuggest
                suggestions={this.state.suggestions}
                onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
                onSuggestionsClearRequested={this.onSuggestionsClearRequested}
                onSuggestionSelected={(event, { suggestionValue }) => {
                    this.props.onSuggestionSelected(event, { suggestionValue });
                    if (this.props.showAllAtStart === true) {
                        this.setState({
                            showAllAtStart: true,
                        });
                    }
                }
                }
                getSuggestionValue={this.getSuggestionValue}
                renderSuggestion={this.renderSuggestion}
                shouldRenderSuggestions={() => true}
                ref={this.storeInputReference}
                inputProps={inputProps}
                renderInputComponent={this.props.renderInputComponent}
            />
        );
    }
}

AutoSuggestHtml.propTypes = {
    placeholder: PropTypes.string,
    onKeyDown: PropTypes.func,
    onSuggestionSelected: PropTypes.func,
    onBlur: PropTypes.func,
    items: PropTypes.arrayOf(PropTypes.string.isRequired).isRequired,
    initialValue: PropTypes.string,
    minWidth: PropTypes.number,
    maxWidth: PropTypes.number,
    showAllAtStart: PropTypes.bool,
    disableAutoFocus: PropTypes.bool,
    renderInputComponent: PropTypes.func,
    onChange: PropTypes.func,
};

AutoSuggestHtml.defaultProps = {
    placeholder: '',
    onKeyDown: null,
    onSuggestionSelected: null,
    onBlur: null,
    initialValue: '',
    minWidth: 0,
    maxWidth: 120,
    showAllAtStart: false,
    disableAutoFocus: false,
    renderInputComponent: undefined,
    onChange: () => {},
};

export default AutoSuggestHtml;
