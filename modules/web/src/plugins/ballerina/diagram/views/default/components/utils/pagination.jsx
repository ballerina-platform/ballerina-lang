/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import './properties-form.css';

/**
 * Pagination component
 */
class Pagination extends React.Component {
    constructor(props) {
        super(props);
        this.state = { pager: {} };
    }

    componentWillMount() {
        if (this.props.items && this.props.items.length) {
            this.setPage(this.props.initialPage);
        }
    }

    /**
     * Set the page number and its items
     * @param page
     */
    setPage(page) {
        const items = this.props.items;
        let pager = this.state.pager;

        if (page < 1 || page > pager.totalPages) {
            return;
        }

        pager = this.getPager(items.length, page);
        const pageOfItems = items.slice(pager.startIndex, pager.endIndex + 1);

        this.setState({ pager });
        this.props.onChangePage(pageOfItems);
    }

    /**
     * Get info about a single page
     * @param totalItems
     * @param currentPage
     * @param pageSize
     * @returns {{totalItems: *, currentPage: (*|number), pageSize: (*|number), totalPages: number, startPage: *, endPage: *, startIndex: number, endIndex: number, pages}}
     */
    getPager(totalItems, currentPage, pageSize) {
        currentPage = currentPage || 1;
        pageSize = pageSize || 4;

        const totalPages = Math.ceil(totalItems / pageSize);

        let startPage,
            endPage;
        if (totalPages <= 10) {
            startPage = 1;
            endPage = totalPages;
        } else {
            if (currentPage <= 6) {
                startPage = 1;
                endPage = 10;
            } else if (currentPage + 4 >= totalPages) {
                startPage = totalPages - 9;
                endPage = totalPages;
            } else {
                startPage = currentPage - 5;
                endPage = currentPage + 4;
            }
        }

        const startIndex = (currentPage - 1) * pageSize;
        const endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);
        const pages = _.range(startPage, endPage + 1);
        return {
            totalItems,
            currentPage,
            pageSize,
            totalPages,
            startPage,
            endPage,
            startIndex,
            endIndex,
            pages,
        };
    }

    /**
     * Renders the pagination view
     * @returns {*}
     */
    render() {
        const pager = this.state.pager;
        if (!pager.pages || pager.pages.length <= 1) {
            return null;
        }

        return (<div className="paginationProgress">
            {pager.currentPage !== 1 &&
            <div className="prevNext done">
                <span className='label'
                      onClick={() => this.setPage(pager.currentPage - 1)}>Previous</span>
            </div> }
            {pager.pages.map((page, index) =>
                (<div key={index} className={pager.currentPage === page ? 'circle active' : 'circle done'}>
                        <span className="label" onClick={() => this.setPage(page)}>{page}</span>
                    </div>
                ),
            )}
            {pager.currentPage !== pager.totalPages &&
            <div className="prevNext done">
                <span  className='label'
                       onClick={() => this.setPage(pager.currentPage + 1)}>Next</span>
            </div>}
        </div>);
    }
}

Pagination.propTypes = {
    items: PropTypes.array.isRequired,
    onChangePage: PropTypes.func.isRequired,
    initialPage: PropTypes.number,
};
Pagination.defaultProps = {
    initialPage: 1,
};

export default Pagination;
