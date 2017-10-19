import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import StatementDecorator from '../decorators/statement-decorator';
import ActiveArbiter from '../decorators/active-arbiter';
import FragmentUtil from '../../../../../utils/fragment-utils';
import TreeBuilder from './../../../../../model/tree-builder';

/**
 * Class for XML namespace.
 * @extends React.Component
 * @class XMLNSNode
 * */
class XmlnsNode extends React.Component {
    /**
     * Constructor for XMLNSNode
     * @param {Object} props - properties passed in to this component.
     * */
    constructor(props) {
        super(props);
        this.editorOptions = {
            propertyType: 'text',
            key: 'XML Namespace',
            model: this.props.model,
            setterMethod: this.handleSetter,
        };

        this.handleSetter = this.handleSetter.bind(this);
    }

    /**
     * Handle setter for XML namespace.
     * @param {string} value - edited value.
     * */
    handleSetter(value) {
        if (!_.isNil(value)) {
            const parsedJson = FragmentUtil.createStatementFragment(value);
            const newXmlNsNode = TreeBuilder.build(
                FragmentUtil.parseFragment(parsedJson), this.parent, this.parent.kind);
            this.parent.replaceStatements(this, newXmlNsNode);
        }
    }

    render() {
        const model = this.props.model;
        const expression = model.viewState.expression;

        return (
            <StatementDecorator
                model={model}
                viewState={model.viewState}
                expression={expression}
                editorOptions={this.editorOptions}
            />);
    }
}

XmlnsNode.PropTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
    model: PropTypes.shape({
        viewState: PropTypes.shape({
            expression: PropTypes.shape({
                expression: PropTypes.string,
            }),
        }).isRequired,
    }).isRequired,
};

XmlnsNode.contextTypes = {
    activeArbiter: PropTypes.instanceOf(ActiveArbiter).isRequired,
    designer: PropTypes.instanceOf(Object).isRequired,
};


export default XmlnsNode;
