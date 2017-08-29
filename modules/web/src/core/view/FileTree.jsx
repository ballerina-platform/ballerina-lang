import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { Treebeard } from 'react-treebeard';
import { getFSRoots } from 'api-client/api-client';

const decorators = {
    Header: ({ style, node }) => {
        const icon = node.children ? 'folder' : 'document';
        return (
            <div className="unseletable-content" style={style.base}>
                <div style={style.title}>
                    <i className={`fw fw-${icon}`} style={{ marginRight: '5px' }} />
                    {node.text}
                </div>
            </div>
        );
    },
};

/**
 * File Tree
 */
class FileTree extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            data: [],
        };
        this.onToggle = this.onToggle.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        const setDecorator = (node) => {
            node.decorators = decorators;
            if (node.children && _.isArray(node.children)) {
                node.children.forEach(setDecorator);
            }
        };
        getFSRoots()
            .then((data) => {
                data.forEach(setDecorator);
                this.setState({
                    data,
                });
            });
    }

    /**
     * On Node Toggole
     * @param {Object} node node object
     * @param {Boolean} toggled toggled state
     */
    onToggle(node, toggled) {
        if (this.state.cursor) {
            this.state.cursor.active = false;
        }
        node.active = true;
        if (node.children) {
            node.toggled = toggled;
        }
        this.setState({ cursor: node });
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div className="file-tree">
                <Treebeard
                    data={this.state.data}
                    onToggle={this.onToggle}
                />
            </div>
        );
    }
}

FileTree.propTypes = {
    root: PropTypes.string.isRequired,
};

export default FileTree;
