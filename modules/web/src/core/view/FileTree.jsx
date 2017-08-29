import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { Treebeard, theme as DefaultTheme } from 'react-treebeard';
import { getFSRoots, listFiles } from 'api-client/api-client';

const decorators = {
    Loading: (props) => {
        return (
            <div style={props.style}>
                loading...
            </div>
        );
    },
    Header: ({ style, node }) => {
        const icon = node.type !== 'file' ? 'folder' : 'document';
        const marginLeft = node.children ? '0' : '20px';
        return (
            <div className="unseletable-content" style={style.base}>
                <div style={style.title}>
                    <div className="whole-row" />
                    <i className={`fw fw-${icon}`} style={{ marginRight: '5px', marginLeft }} />
                    {node.text}
                </div>
            </div>
        );
    },
};

const theme = DefaultTheme;
const color = '#dedede';
const highlightColor = '#232323';
_.set(theme, 'tree.base.backgroundColor', 'transparent');
_.set(theme, 'tree.node.toggle.arrow.fill', color);
_.set(theme, 'tree.node.activeLink.background', highlightColor);
_.set(theme, 'tree.base.color', color);
_.set(theme, 'tree.base.color', color);

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
        getFSRoots()
            .then((data) => {
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
            if (_.isEmpty(node.children)) {
                node.loading = true;
                listFiles(node.id, ['.bal'])
                    .then((data) => {
                        node.loading = false;
                        node.children = data;
                        this.forceUpdate();
                    });
            }
        }
        this.setState({ cursor: node });
    }

    /**
     * @inheritdoc
     */
    render() {
        const prepareNode = (node) => {
            node.decorators = decorators;
            if (node.children && _.isArray(node.children)) {
                node.children.forEach(prepareNode);
            }
            if (node.children === true) {
                node.children = [];
            } else if (node.children === false) {
                delete node.children;
            }
        };
        this.state.data.forEach(prepareNode);
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
