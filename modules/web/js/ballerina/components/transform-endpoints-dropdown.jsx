import React from 'react';
import Autosuggest from 'react-autosuggest';

const getSuggestionValue = suggestion => suggestion.name;

const renderSuggestion = suggestion => (
  <div>
    {suggestion.name}<span style={{color: '#a8a8a8'}}>{` ${suggestion.type}`}</span>
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

        this.onChange = this.onChange.bind(this);
        this.onSuggestionsFetchRequested = this.onSuggestionsFetchRequested.bind(this);
        this.onSuggestionsClearRequested = this.onSuggestionsClearRequested .bind(this);
    }

    onChange(event, { newValue }) {
        this.setState({
            value: newValue
        });
    };

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

    render() {
        const { value, suggestions } = this.state;
        const { placeholder } = this.props;

        const inputProps = {
            placeholder,
            value,
            onChange: this.onChange,
        };

        return (
            <Autosuggest
                suggestions={this.state.suggestions}
                onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
                onSuggestionsClearRequested={this.onSuggestionsClearRequested}
                shouldRenderSuggestions={shouldRenderSuggestions}
                getSuggestionValue={getSuggestionValue}
                renderSuggestion={renderSuggestion}
                inputProps={inputProps}
            />
        );
    }
}
