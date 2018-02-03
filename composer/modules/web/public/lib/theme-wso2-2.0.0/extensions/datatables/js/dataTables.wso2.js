/*
 ~   Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 ~
 ~   Licensed under the Apache License, Version 2.0 (the "License");
 ~   you may not use this file except in compliance with the License.
 ~   You may obtain a copy of the License at
 ~
 ~        http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~   Unless required by applicable law or agreed to in writing, software
 ~   distributed under the License is distributed on an "AS IS" BASIS,
 ~   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~   See the License for the specific language governing permissions and
 ~   limitations under the License.
*/

/*
 ~   DataTables WSO2 Integration
 ~   ©2016 WSO2 Inc. - datatables.net/license
 ~
 ~   DataTables integration for WSO2 Visual Elements. This requires WSO2 Visual Elements 1.0.0 and
 ~   DataTables 1.10 or newer.
 */

(function(window, document, undefined){

    var factory = function($, DataTable) {
        "use strict";
        
        /* Set the defaults for DataTables initialisation */
        $.extend(true, DataTable.defaults, {
            bSortCellsTop: true,
            responsive: false,
            autoWidth: false,
            dom: '<"dataTablesTop"' +
                'f' +
                '<"dataTables_toolbar">' +
                '>' +
                'rt' +
                '<"dataTablesBottom"' +
                'lip' +
                '>',
            language: {
                searchPlaceholder: 'Filter by ...',
                search: ''
            }
        });
        
        var wso2Extend = function(settings, opts) {

            // Sanity check that we are using DataTables 1.10 or newer
            if (!DataTable.versionCheck || !DataTable.versionCheck('1.10.12')) {
                throw 'DataTables Responsive requires DataTables 1.10.12 or newer';
            }

            this.s = {
                dt: new DataTable.Api(settings),
                columns: []
            };

            // Check if responsive has already been initialised on this table
            if (this.s.dt.settings()[0].wso2) {
                return;
            }

            // details is an object, but for simplicity the user can give it as a string
            if (opts && typeof opts.details === 'string') {
                opts.details = { type: opts.details };
            }
            
            this.c = $.extend( true, {}, wso2Extend.defaults, DataTable.defaults.wso2, opts );
            settings.wso2 = this;
            this._constructor();

        };
        
        wso2Extend.prototype = {
            
            _constructor: function(){
                
		        var dt = this.s.dt;
                var elem = $('table', dt.table().container());

                var ROW_SELECTED_CLASS = 'DTTT_selected';

                dt.table().columns().every(function() {

                    var column = this;
                    var filterColumn = $('.filter-row th', elem);

                    //Create & add select/text filters to each column
                    if (filterColumn.eq(column.index()).hasClass('select-filter')) {
                        var select = $('<select class="form-control"><option value="">All</option></select>')
                            .appendTo(filterColumn.eq(column.index()).empty())
                            .on('change', function () {
                                var val = $.fn.dataTable.util.escapeRegex(
                                    $(this).val()
                                );

                                column
                                    .search(val ? '^' + val + '$' : '', true, false)
                                    .draw();
                            });

                        $(column).each(function () {
                            if ($(column.nodes()).attr('data-search')) {
                                var titles = [];
                                column.nodes().unique().sort().each(function (d, j) {
                                    var title = $(d).attr('data-display');
                                    if ($.inArray(title, titles) < 0) {
                                        titles.push(title);
                                        if (title !== undefined) {
                                            select.append('<option value="' + title + '">' + title + '</option>')
                                        }
                                    }
                                });
                            } else {
                                column.data().unique().sort().each(function (d, j) {
                                    select.append('<option value="' + d + '">' + d + '</option>')
                                });
                            }
                        });
                    } else if (filterColumn.eq(column.index()).hasClass('text-filter')) {
                        var title = filterColumn.eq(column.index()).attr('data-for');
                        $(filterColumn.eq(column.index()).empty()).html('<input type="text" class="form-control" placeholder="Search for ' + title + '" />');

                        filterColumn.eq(column.index()).find('input').on('keyup change', function () {
                            column
                                .search($(this).val())
                                .draw();
                        });
                    }

                });

                //Search input default styles override
                var search_input = elem.closest('.dataTables_wrapper').find('div[id$=_filter] input');
                search_input.before('<i class="fw fw-search search-icon"></i>').removeClass('input-sm');

                // Create sorting dropdown menu for list table advance operations
                var dropdownmenu = $('<ul class="dropdown-menu arrow arrow-top-right dark sort-list add-margin-top-2x"><li class="dropdown-header">Sort by</li></ul>');
                $('.sort-row th', elem).each(function () {
                    if (!$(this).hasClass('no-sort')) {
                        dropdownmenu.append('<li><a href="#' + $(this).html() + '" data-column="' + $(this).index() + '">' + $(this).html() + '</a></li>');
                    }
                });

                //Append advance operations to list table toolbar
                $('.dataTable.list-table').closest('.dataTables_wrapper').find('.dataTablesTop .dataTables_toolbar').html('' +
                    '<ul class="nav nav-pills navbar-right remove-margin" role="tablist">' +
                    '<li><button data-click-event="toggle-select" class="btn btn-default btn-primary">Select</li>' +
                    '<li class="select-all-btn" style="display:none;"><button data-click-event="toggle-select-all" class="btn btn-default btn-primary">Select All</li>' +
                    '<li><button data-click-event="toggle-list-view" data-view="grid" class="btn btn-default"><i class="fw fw-grid"></i></button></li>' +
                    '<li><button data-click-event="toggle-list-view" data-view="list" class="btn btn-default"><i class="fw fw-list"></i></button></li>' +
                    '<li><button class="btn btn-default" data-toggle="dropdown"><i class="fw fw-sort"></i></button>' + dropdownmenu[0].outerHTML + '</li>' +
                    '</ul>'
                );

                //Sorting dropdown menu select function
                $('.dataTables_wrapper .sort-list li a').click(function () {
                    $(this).closest('li').siblings('li').find('a').removeClass('sorting_asc').removeClass('sorting_desc');

                    var thisTable = $(this).closest('.dataTables_wrapper').find('.dataTable').dataTable();

                    if (!($(this).hasClass('sorting_asc')) && !($(this).hasClass('sorting_desc'))) {
                        $(this).addClass('sorting_asc');
                        thisTable.fnSort([
                            [$(this).attr('data-column'), 'asc']
                        ]);
                    } else if ($(this).hasClass('sorting_asc')) {
                        $(this).switchClass('sorting_asc', 'sorting_desc');
                        thisTable.fnSort([
                            [$(this).attr('data-column'), 'desc']
                        ]);
                    } else if ($(this).hasClass('sorting_desc')) {
                        $(this).switchClass('sorting_desc', 'sorting_asc');
                        thisTable.fnSort([
                            [$(this).attr('data-column'), 'asc']
                        ]);
                    }
                });

                //Enable/Disable selection on rows
                $('.dataTables_wrapper').off('click', '[data-click-event=toggle-select]');
                $('.dataTables_wrapper').on('click', '[data-click-event=toggle-select]', function () {
                    var button = this,
                        thisTable = $(this).closest('.dataTables_wrapper').find('.dataTable').dataTable();
                    if ($(button).html() == 'Select') {
                        thisTable.api().rows().every(function () {
                            $(this.node()).attr('data-type','selectable');
                        });
                        thisTable.addClass("table-selectable");
                        $(button).addClass("active").html('Cancel');
                        $(button).closest('li').siblings('.select-all-btn').show();
                    } else if ($(button).html() == 'Cancel'){
                        thisTable.api().rows().every(function () {
                            $(this.node()).removeAttr('data-type');
                            $(this.node()).removeClass(ROW_SELECTED_CLASS);
                        });
                        thisTable.removeClass("table-selectable");
                        $(button).removeClass("active").html('Select');
                        $(button).closest('li').siblings('.select-all-btn').hide();
                    }
                });

                //Select/Deselect all rows functions
                $('.dataTables_wrapper').off('click', '[data-click-event=toggle-select-all]');
                $('.dataTables_wrapper').on('click', '[data-click-event=toggle-select-all]', function () {
                    var button = this,
                        thisTable = $(this).closest('.dataTables_wrapper').find('.dataTable').dataTable();
                    if ($(button).html() == 'Select All') {
                        thisTable.api().rows().every(function () {
                            $(this.node()).addClass(ROW_SELECTED_CLASS);
                            $(button).html('Deselect All');
                        });
                    } else if ($(button).html() == 'Deselect All') {
                        thisTable.api().rows().every(function () {
                            $(this.node()).removeClass(ROW_SELECTED_CLASS);
                            $(button).html('Select All');
                        });
                    }
                });


                //Event for row select/deselect
                $('body').on('click', '[data-type=selectable]', function() {
                    $(this).toggleClass(ROW_SELECTED_CLASS);
                    var button = this,
                        thisTable = $(this).closest('.dataTables_wrapper').find('.dataTable').dataTable();

                    thisTable.api().rows().every(function () {
                        if (!$(this.node()).hasClass(ROW_SELECTED_CLASS)) {
                            $(button).closest('.dataTables_wrapper').find('[data-click-event=toggle-selected]').html('Select All');
                        }
                    });
                });

                //list table list/grid view toggle function
                var toggleButton = $('[data-click-event=toggle-list-view]');
                toggleButton.click(function () {
                    if ($(this).attr('data-view') == 'grid') {
                        $(this).closest('.dataTables_wrapper').find('.dataTable').addClass('grid-view');
                        //$(this).closest('li').hide();
                        //$(this).closest('li').siblings().show();
                    } else {
                        $(this).closest('.dataTables_wrapper').find('.dataTable').removeClass('grid-view');
                        //$(this).closest('li').hide();
                        //$(this).closest('li').siblings().show();
                    }
                });

                //delete selected rows
                $('[data-click-event=delete-selected-rows]').click(function () {
                    var thisTable = $(this).closest('.dataTables_wrapper').find('.dataTable').dataTable();
                    thisTable.api().rows('.' + ROW_SELECTED_CLASS).remove().draw(false);
                });

            }
        }
        
        wso2Extend.defaults = {
            bSortCellsTop: true,
            responsive: false,
            autoWidth: false,
            dom: '<"dataTablesTop"' +
                'f' +
                '<"dataTables_toolbar">' +
                '>' +
                'rt' +
                '<"dataTablesBottom"' +
                'lip' +
                '>',
            language: {
                searchPlaceholder: 'Filter by ...',
                search: ''
            }
        }
        
        /**
         * Version information
         *
         * @name wso2Extend.version
         * @static
         */
        wso2Extend.version = '1.0.0';

        $.fn.dataTable.wso2Extend = wso2Extend;
        $.fn.DataTable.wso2Extend = wso2Extend;

        // Attach a listener to the document which listens for DataTables initialisation
        // events so we can automatically initialise
        $(document).on('preInit.dt.dte', function(e, settings, json) {            
            if (e.namespace !== 'dt') {
                return;
            }

//            if ( $(settings.nTable).hasClass('dte') ||
//                 $(settings.nTable).hasClass('dt-extended') ||
//                 settings.oInit.wso2 ||
//                 DataTable.defaults.wso2 ||
//            ) {
                var init = settings.oInit.wso2;

                if (init !== false) {
                    new wso2Extend (settings, $.isPlainObject(init) ? init : {});
                }
            //}
        });
        
        return wso2Extend;

    };

    // Define as an AMD module if possible
    if (typeof define === 'function' && define.amd){
        define(['jquery', 'datatables'], factory);
    }
    else if (typeof exports === 'object'){
        // Node/CommonJS
        factory(require('jquery'), require('datatables'));
    }
    else if (jQuery && !jQuery.fn.dataTable.wso2Extend) {
        // Otherwise simply initialise as normal, stopping multiple evaluation
        factory(jQuery, jQuery.fn.dataTable);
    }

})(window, document);