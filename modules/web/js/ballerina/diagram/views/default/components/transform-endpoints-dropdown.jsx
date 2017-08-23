import React from 'react';
import Autosuggest from 'react-autosuggest';

const getSuggestionValue = suggestion => suggestion.name;

const renderSuggestion = suggestion => (
  <div>
    {suggestion.name}<span style={{color: '#a8a8a8'}}>{` ${suggestion.typeName || suggestion.type}`}</span>
  </div>
);

const shouldRenderSuggestions = () => true

export default class SuggestionsDropdown extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            value: '',
            suggestions: this.props.suggestionsPool,
        }

        this.onSuggestionsFetchRequested = this.onSuggestionsFetchRequested.bind(this);
        this.onSuggestionsClearRequested = this.onSuggestionsClearRequested.bind(this);
        this.onKeyDown = this.onKeyDown.bind(this);
    }

    onSuggestionsFetchRequested(query) {
        const matches = [];
        const substrRegex = new RegExp(query.value, 'i');

        this.props.suggestionsPool.forEach((sug) => {
            if (substrRegex.test(sug.name + sug.type)) {
                matches.push(sug);
            }
        });

        this.setState({suggestions: matches});
    }

    onSuggestionsClearRequested() {
        this.setState({
            suggestions: []
        });
    };

    onKeyDown(event) {
        if (event.keyCode === 13) {
            this.props.onEnter(event);
        }
    }

    render() {
        const { suggestions } = this.state;
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
            />
        );
    }
}
