import PropTypes from 'prop-types';

export const TOOL = PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    icon: PropTypes.string,
    title: PropTypes.string,
    description: PropTypes.string,
    factoryArgs: PropTypes.objectOf(Object),
    nodeFactoryMethod: PropTypes.func,
});

export const TOOL_GROUP = PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    tools: PropTypes.arrayOf(TOOL),
    order: PropTypes.string,
    gridConfig: PropTypes.bool,
});
