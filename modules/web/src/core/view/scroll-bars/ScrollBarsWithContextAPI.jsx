import React from 'react';
import PropTypes from 'prop-types';
import { Scrollbars } from 'react-custom-scrollbars';

/**
 * Wraps custom scroll bars and provide scrolling API through context.
 *
 */
export default class ScrollBarsWithContextAPI extends React.Component {
    /**
     * @inheritdoc
     */
    constructor(...args) {
        super(...args);
        this.scrollBarRef = undefined;
    }

    /**
     * @inheritdoc
     */
    getChildContext() {
        return {
            scroller: {
                scrollTop: (top) => {
                    if (this.scrollBarRef) {
                        this.scrollBarRef.scrollTop(top);
                    }
                },
                scrollLeft: (left) => {
                    if (this.scrollBarRef) {
                        this.scrollBarRef.scrollLeft(left);
                    }
                },
                getScrollWidth: () => {
                    return this.scrollBarRef ? this.scrollBarRef.getScrollWidth() : undefined;
                },
                getScrollHeight: () => {
                    return this.scrollBarRef ? this.scrollBarRef.getScrollHeight() : undefined;
                },
                isTopVisible: (top) => {
                    if (this.scrollBarRef) {
                        const { clientHeight, scrollTop } = this.scrollBarRef.getValues();
                        return (clientHeight + scrollTop) > top;
                    }
                    return false;
                },
                isLeftVisible: (left) => {
                    if (this.scrollBarRef) {
                        const { clientWidth, scrollLeft } = this.scrollBarRef.getValues();
                        return (clientWidth + scrollLeft) > left;
                    }
                    return false;
                },
                getValues: () => {
                    return this.scrollBarRef ? this.scrollBarRef.getValues() : undefined;
                },
            },
        };
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <Scrollbars
                {...this.props}
                ref={(ref) => {
                    this.scrollBarRef = ref;
                }}
            >
                {this.props.children}
            </Scrollbars>
        );
    }
}

ScrollBarsWithContextAPI.childContextTypes = {
    scroller: PropTypes.shape({
        scrollTop: PropTypes.func.isRequired,
        scrollLeft: PropTypes.func.isRequired,
        getScrollWidth: PropTypes.func.isRequired,
        getScrollHeight: PropTypes.func.isRequired,
        isTopVisible: PropTypes.func.isRequired,
        isLeftVisible: PropTypes.func.isRequired,
        getValues: PropTypes.func.isRequired,
    }).isRequired,
};

