import React, { PropTypes } from 'react';

const propTypes = {};

const defaultProps = {};

class Foo extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div className="foo">
                Bar
            </div>
        );
    }
}

Foo.propTypes = propTypes;
Foo.defaultProps = defaultProps;

export default Foo;