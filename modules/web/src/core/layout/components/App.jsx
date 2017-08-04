import React from 'react';
import PropTypes from 'prop-types';
import GLRegion from './GLRegion';
import Header from './Header';
import ActivityBar from './ActivityBar';

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
        return (
            <div className="">
                <Header />
                <ActivityBar />
                <GLRegion />
            </div>
        );
    }
}

export default App;
