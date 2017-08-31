import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { Treebeard, decorators as Decorators } from 'react-treebeard';
import { getFSRoots, listFiles } from 'api-client/api-client';

const decorators = {
    Container: (props) => {
        return (
            <div className="file-tree-item">
                <div className="whole-row" />
                <Decorators.Container {...props} />
            </div>
        );
    },
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
            <div className="unseletable-content file-tree-header" style={style.base}>
                <div style={style.title}>
                    <i className={`fw fw-${icon}`} style={{ marginRight: '5px', marginLeft }} />
                    {node.text}
                </div>
            </div>
        );
    },
};

// A symbol to represent file system root
const FS_ROOT = '#';
const color = '#c7c7c7';
const highlightColor = '#232323';
const theme = {
    tree: {
        base: {
            listStyle: 'none',
            backgroundColor: 'transparent',
            margin: 0,
            padding: 0,
            color,
            fontFamily: 'lucida grande ,tahoma,verdana,arial,sans-serif',
            fontSize: '14px',
        },
        node: {
            base: {
                position: 'relative',
            },
            link: {
                cursor: 'pointer',
                position: 'relative',
                padding: '0px 5px',
                display: 'block',
            },
            activeLink: {
                background: highlightColor,
            },
            toggle: {
                base: {
                    position: 'relative',
                    display: 'inline-block',
                    verticalAlign: 'top',
                    marginLeft: '-5px',
                    height: '24px',
                    width: '24px',
                },
                wrapper: {
                    position: 'absolute',
                    top: '50%',
                    left: '50%',
                    margin: '-7px 0 0 -7px',
                    height: '14px',
                },
                height: 14,
                width: 14,
                arrow: {
                    fill: color,
                    strokeWidth: 0,
                },
            },
            header: {
                base: {
                    display: 'inline-block',
                    verticalAlign: 'top',
                    color,
                },
                connector: {
                    width: '2px',
                    height: '12px',
                    borderLeft: 'solid 2px black',
                    borderBottom: 'solid 2px black',
                    position: 'absolute',
                    top: '0px',
                    left: '-21px',
                },
                title: {
                    lineHeight: '24px',
                    verticalAlign: 'middle',
                },
            },
            subtree: {
                listStyle: 'none',
                paddingLeft: '19px',
            },
            loading: {
                color,
            },
        },
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
        const isFSRoot = this.props.root === FS_ROOT;
        const loadData = isFSRoot ? getFSRoots() : listFiles(this.props.root);
        // wrap custom roots with a single root node
        const wrapWithCustomRoot = (children) => {
            return [{
                text: this.props.root,
                id: this.props.root,
                type: 'folder',
                children,
            }];
        };
        loadData
            .then((tree) => {
                const data = isFSRoot ? tree : wrapWithCustomRoot(tree);
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
                        if (_.isEmpty(data)) {
                            delete node.children;
                            node.toggled = false;
                        } else {
                            node.children = data;
                        }
                        this.forceUpdate();
                    });
            }
        }
        this.setState({ cursor: node });
        this.props.onSelect(node);
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
                    style={theme}
                    data={this.state.data}
                    onToggle={this.onToggle}
                />
            </div>
        );
    }
}

FileTree.propTypes = {
    onSelect: PropTypes.func,
    root: PropTypes.string,
};

FileTree.defaultProps = {
    onSelect: () => {},
    root: FS_ROOT,
};

export default FileTree;
