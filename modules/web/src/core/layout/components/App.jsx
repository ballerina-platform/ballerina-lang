import React from 'react';
import PropTypes from 'prop-types';
import GLRegion from './GLRegion';
import Header from './Header';
import ActivityBar from './ActivityBar';
import { REGIONS } from './../constants';

/**
 * React component for App.
 *
 * @class App
 * @extends {React.Component}
 */
class App extends React.Component {

    /**
     * @inheritdoc
     */
    render() {
        const headerViews = [];
        this.props.layout[REGIONS.HEADER].forEach((view) => {
            const { component, propsProvider } = view;
            const View = component;
            headerViews.push(
                <View {...propsProvider()} />
            );
        });
        return (
            <div className="">
                <Header>
                    {headerViews}
                </Header>
                <ActivityBar />
                <GLRegion />
            </div>
        );
    }
}

App.propTypes = {
    layout: PropTypes.objectOf(Object).isRequired,
};

export default App;
