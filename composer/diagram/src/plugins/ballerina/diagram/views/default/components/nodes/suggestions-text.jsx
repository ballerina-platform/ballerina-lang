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
import Autosuggest from 'react-autosuggest';
import './suggestions-text.css';

/**
 * Class representing the view for inputs with suggestions.
 * @extends React.Component
 */
class SuggestionsText extends React.Component {

    /**
     * Creates SuggestionsText component instance.
     * @param {Object} props - React props.
     */
    constructor(props) {
        super(props);
        this.state = {
            value: '',
            suggestions: [],
        };

        this.onSuggestionsFetchRequested = ({ value }) => {
            this.setState({ suggestions: this.getSuggestions(value) });
        };

        this.onSuggestionsClearRequested = () => {
            this.setState({ suggestions: [] });
        };

        this.renderSuggestionsContainer = this.renderSuggestionsContainer.bind(this);
        this.onChange = this.onChange.bind(this);
        this.onKeyDown = this.onKeyDown.bind(this);
        this.renderSuggestion = this.renderSuggestion.bind(this);
        this.onSelected = this.onSelected.bind(this);
        this.storeInputReference = (autosuggest) => {
            if (autosuggest !== null) {
                this.input = autosuggest.input;
            }
        };
    }

    /**
     * @override
     */
    componentDidMount() {
        this.renderSuggestionsText();
        if (this.input) {
            this.input.focus();
        }
    }

    /**
     * @override
     */
    componentDidUpdate(prevProps) {
        if (prevProps.show || this.props.show) {
            // If its not showing previously and still not showing no need to re-render
            // this also avoids removing other possible html elements from html overlay
            this.renderSuggestionsText();
            if (this.input) {
                this.input.focus();
            }
        }
    }

    /**
     * Called when a value is typed or pasted in the input box
     * @param {Object} event - the input change event
     */
    onChange(event, { newValue }) {
        this.setState({ value: newValue });
    }

    /**
     * Called for the key down event when a value is typed
     * @param {Object} event - the keydown event object
     */
    onKeyDown(event) {
        if (event.keyCode === 13) {
            this.props.onEnter(this.state.value);
            this.setState({
                value: '',
            });
        }
    }

    /**
     * Called after a value is selected from the select box
     * @param {Object} event - the selected event
     */
    onSelected(event, { suggestionValue }) {
        this.setState({
            value: '',
        });
        this.props.onSuggestionSelected(event, { suggestionValue });
    }

    /**
     * Returns the displayed value for the suggestion
     * This function to be passed to react autosuggest component
     * @param {Object} suggestion - the suggestion object
     */
    getSuggestionValue(suggestion) {
        return suggestion.name;
    }

    /**
     * Returns suggestions that contains the given query
     * This function to be passed to react autosuggest component
     * @param {Object} suggestion - the suggestion object
     * @returns {Array} The array of suggestions matching the the given query
     */
    getSuggestions(query) {
        const matches = [];

        const substrRegex = new RegExp(query, 'i');

        this.props.suggestionsPool.forEach((sug) => {
            if (substrRegex.test(sug.name)) {
                matches.push(sug);
            }
        });

        return matches;
    }

    /**
     * Returns the react element regarding a suggestion
     * This function to be passed to react autosuggest component
     * @param {Object} suggestion - the suggestion object
     * @returns {React.Element} The element regarding the suggestion
     */
    renderSuggestion(suggestion) {
        const { value } = this.state;
        const sugName = suggestion.name;
        const parts = sugName.split(value);
        const highlightedString = [];

        // Highlight the typed value in the suggestion
        parts.forEach((p, i) => {
            highlightedString.push(<span key={i}>{p}</span>);
            if (i < parts.length - 1) {
                // if not last
                highlightedString.push(<span key={i + 100}><b>{value}</b></span>);
            }
        });

        return (
            <div>
                {highlightedString}
            </div>
        );
    }

    /**
     * Renders the container for suggestions elements.
     * This function is called by the react autosuggest component
     * @param {Object} - object containing the containerProps and the suggestions elements
     */
    renderSuggestionsContainer({ containerProps, children }) {
        const { x, y, height = 25, width = 100 } = this.props;
        const allProps = {
            style: {
                position: 'absolute',
                top: y + height,
                left: x,
                width,
            },
        };

        Object.assign(allProps, containerProps);

        return (
            <div {...allProps}>
                {children}
            </div>
        );
    }

    /**
     * Renders the react autosuggest element in the html overlay container
     */
    renderSuggestionsText() {
        if (!this.props.show) {
            ReactDOM.render(<noscript />, this.context.getOverlayContainer());
            return;
        }

        const { x, y, height = 25, width = 100 } = this.props;

        const inputStyle = {
            position: 'absolute',
            top: y,
            left: x,
            width,
            height,
        };

        const inputProps = {
            value: this.state.value,
            onChange: this.onChange,
            onKeyDown: this.onKeyDown,
            onBlur: this.props.onBlur,
            style: inputStyle,
        };

        ReactDOM.render(
            <Autosuggest
                suggestions={this.state.suggestions}
                onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
                onSuggestionsClearRequested={this.onSuggestionsClearRequested}
                getSuggestionValue={this.getSuggestionValue}
                renderSuggestion={this.renderSuggestion}
                renderSuggestionsContainer={this.renderSuggestionsContainer}
                onSuggestionSelected={this.onSelected}
                inputProps={inputProps}
                ref={this.storeInputReference}
                shouldRenderSuggestions={() => true}
            />, this.context.getOverlayContainer());
    }

    render() {
        const { x, y, height, width } = this.props;

        const style = {};

        if (!this.props.show) {
            style.display = 'none';
        }

        return <rect x={x} y={y} height={height} width={width} style={style} className='suggestions-text-rect' />;
    }
}

SuggestionsText.contextTypes = {
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
};

SuggestionsText.propTypes = {
    x: PropTypes.number.isRequired,
    y: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
    width: PropTypes.number.isRequired,
    show: PropTypes.bool,
    onEnter: PropTypes.func.isRequired,
    onSuggestionSelected: PropTypes.func,
    onBlur: PropTypes.func,
    suggestionsPool: PropTypes.arrayOf(PropTypes.shape({
        name: PropTypes.string.isRequired,
    })).isRequired,
};

SuggestionsText.defaultProps = {
    show: true,
    onSuggestionSelected: undefined,
    onBlur: undefined,
};

export default SuggestionsText;
