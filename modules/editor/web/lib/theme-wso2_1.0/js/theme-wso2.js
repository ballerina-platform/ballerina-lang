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

/**
 * @description Check jQuery
 * @throw  {String}  throw an exception message if jquery is not loaded
 */
if (typeof(jQuery) === 'undefined') {
    throw 'jQuery is required.';
}

var responsiveTextRatio = 0.2,
    responsiveTextSleector = ".icon .text";

(function($) {

    /**
     * @description Dependancy injection function
     * @param  {String}     File    Name of the dependancy
     * @param  {String}     Type    Dependancy type
     * @return {Null}
     */
    $.required = function(file, filetype) {
        var markup = 'undefined';

        if (filetype == 'js') { //if filename is a external JavaScript file
            markup = document.createElement('script');
            markup.setAttribute("type", "text/javascript");
            markup.setAttribute("src", file);
        } else if (filetype == 'css') { //if filename is an external CSS file
            markup = document.createElement('link');
            markup.setAttribute("rel", "stylesheet");
            markup.setAttribute("type", "text/css");
            markup.setAttribute("href", file);
        }

        if (typeof markup != 'undefined') {
            if (filetype == 'js') {
                $('html script[src*="theme-wso2.js"]').before(markup);
            } else if (filetype == 'css') {
                $('head link[href*="main.less"]').before(markup);
            }
        }
    };

    /**
     * @description Attribute toggle function
     * @param  {String} attr    Attribute Name
     * @param  {String} val     Value to be matched
     * @param  {String} val2    Value to be replaced with
     */
    $.fn.toggleAttr = function(attr, val, val2) {
        return this.each(function() {
            var self = $(this);
            if (self.attr(attr) == val)
                self.attr(attr, val2);
            else
                self.attr(attr, val);
        });
    };


    /**
     * A function to add data attributes to HTML about the user agent
     * @return {Null}
     */
    $.browser_meta = function() {
        $('html')
            .attr('data-useragent', navigator.userAgent)
            .attr('data-platform', navigator.platform)
            .addClass(((!!('ontouchstart' in window) || !!('onmsgesturechange' in window)) ? ' touch' : ''));
    };


    /**
     * Cross browser file input controller
     * @return {Node} DOM Node
     */
    $.file_input = function() {
        var elem = '.file-upload-control';

        return $(elem).each(function() {

            //Input value change function
            $(elem + ' :file').change(function() {
                var input = $(this),
                    numFiles = input.get(0).files ? input.get(0).files.length : 1,
                    label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
                input.trigger('fileselect', [numFiles, label]);
            });

            //Button click function
            $(elem + ' .browse').click(function() {
                $(this).parents('.input-group').find(':file').click();
            });

            //File select function
            $(elem + ' :file').on('fileselect', function(event, numFiles, label) {
                var input = $(this).parents('.input-group').find(':text'),
                    log = numFiles > 1 ? numFiles + ' files selected' : label;

                if (input.length) {
                    input.val(log);
                } else {
                    if (log) {
                        alert(log);
                    }
                }
            });

        });
    };


    /**
     * @description Auto resize icons and text on browser resize
     * @param  {Number}     Compression Ratio
     * @param  {Object}     Object containing the values to override defaults
     * @return {Node}       DOM Node
     */
    $.fn.responsive_text = function(compress, options) {

        // Setup options
        var compressor = compress || 1,
            settings = $.extend({
                'minFontSize': Number.NEGATIVE_INFINITY,
                'maxFontSize': Number.POSITIVE_INFINITY
            }, options);

        return this.each(function() {

            //Cache object for performance
            var $this = $(this);

            //resize items based on the object width devided by the compressor
            var resizer = function() {
                $this.css('font-size', Math.max(Math.min($this.width() / (compressor * 10), parseFloat(settings.maxFontSize)), parseFloat(settings.minFontSize)));
            };

            //Init method
            resizer();

            //event bound to browser window to fire on window resize
            $(window).on('resize.fittext orientationchange.fittext', resizer);

        });

    };
    
    /**
    * @description Data Loader function
    * @param  {String}     Action of the loader
    */
    $.fn.loading = function(action) {

        return $(this).each(function() {

          var loadingText = ($(this).attr('data-loading-text') === undefined) ? 'LOADING' : $(this).attr('data-loading-text');

          var html = '<div class="loading-animation">' +
              '<div class="logo">' +
              '<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"' +
              'viewBox="0 0 14 14" enable-background="new 0 0 14 14" xml:space="preserve">' +
              '<path class="circle" stroke-width="1.4" stroke-miterlimit="10" d="M6.534,0.748C7.546,0.683,8.578,0.836,9.508,1.25 c1.903,0.807,3.339,2.615,3.685,4.654c0.244,1.363,0.028,2.807-0.624,4.031c-0.851,1.635-2.458,2.852-4.266,3.222 c-1.189,0.25-2.45,0.152-3.583-0.289c-1.095-0.423-2.066-1.16-2.765-2.101C1.213,9.78,0.774,8.568,0.718,7.335 C0.634,5.866,1.094,4.372,1.993,3.207C3.064,1.788,4.76,0.867,6.534,0.748z"/>' +
              '<path class="pulse-line" stroke-width="0.55" stroke-miterlimit="10" d="M12.602,7.006c-0.582-0.001-1.368-0.001-1.95,0 c-0.491,0.883-0.782,1.4-1.278,2.28C8.572,7.347,7.755,5.337,6.951,3.399c-0.586,1.29-1.338,3.017-1.923,4.307 c-1.235,0-2.38-0.002-3.615,0"/>' +
              '</svg>' +
              '<div class="signal"></div>' +
              '</div>' +
              '<p>' + loadingText + '</p>' +
              '</div>' +
              '<div class="loading-bg"></div>';

          if (action === 'show') {
            $(this).prepend(html).addClass('loading');
          }
          if (action === 'hide') {
            $(this).removeClass('loading');
            $('.loading-animation, .loading-bg', this).remove();
          }
        });

    };

    /**
     * @description Random background color generator for thumbs
     * @param  {range}      Color Range Value
     * @return {Node}       DOM Node
     */
    $.fn.random_background_color = function(range) {

        if (!range) {
            range = 9;
        }

        return this.each(function() {

            var color = '#' + Math.random().toString(range).substr(-6);
            $(this).css('background', color);

        });

    };

    /**
     * Data tables extended functionality wrapper
     * @param  {Object} settings Settings to override the defaults data tables behaviour
     * @return {Null}
     */
    $.fn.datatables_extended = function(settings) {

        if(typeof $.fn.DataTable != 'function'){
            console.warn('Warning : Dependency missing - tooltip Library');
        }

        var elem = $(this);

        $(elem).DataTable(
            $.extend({}, {
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
                },
                initComplete: function() {

                    var ROW_SELECTED_CLASS = 'DTTT_selected';

                    this.api().columns().every(function() {

                        var column = this;
                        var filterColumn = $('.filter-row th', elem);

                        //Create & add select/text filters to each column
                        if (filterColumn.eq(column.index()).hasClass('select-filter')) {
                            var select = $('<select class="form-control"><option value="">All</option></select>')
                                .appendTo(filterColumn.eq(column.index()).empty())
                                .on('change', function() {
                                    var val = $.fn.dataTable.util.escapeRegex(
                                        $(this).val()
                                    );

                                    column
                                        .search(val ? '^' + val + '$' : '', true, false)
                                        .draw();
                                });

                            $(column).each(function() {
                                if ($(column.nodes()).attr('data-search')) {
                                    var titles = [];
                                    column.nodes().unique().sort().each(function(d, j) {
                                        var title = $(d).attr('data-display');
                                        if ($.inArray(title, titles) < 0) {
                                            titles.push(title);
                                            if (title !== undefined) {
                                                select.append('<option value="' + title + '">' + title + '</option>')
                                            }
                                        }
                                    });
                                } else {
                                    column.data().unique().sort().each(function(d, j) {
                                        select.append('<option value="' + d + '">' + d + '</option>')
                                    });
                                }
                            });
                        } else if (filterColumn.eq(column.index()).hasClass('text-filter')) {
                            var title = filterColumn.eq(column.index()).attr('data-for');
                            $(filterColumn.eq(column.index()).empty()).html('<input type="text" class="form-control" placeholder="Search for ' + title + '" />');

                            filterColumn.eq(column.index()).find('input').on('keyup change', function() {
                                column
                                    .search($(this).val())
                                    .draw();
                            });
                        }

                    });

                    //Search input default styles override
                    var search_input = $(this).closest('.dataTables_wrapper').find('div[id$=_filter] input');
                    search_input.before('<i class="fw fw-search search-icon"></i>').removeClass('input-sm');

                    // Create sorting dropdown menu for list table advance operations 
                    var dropdownmenu = $('<ul class="dropdown-menu arrow arrow-top-right dark sort-list add-margin-top-2x"><li class="dropdown-header">Sort by</li></ul>');
                    $('.sort-row th', elem).each(function() {
                        if (!$(this).hasClass('no-sort')) {
                            dropdownmenu.append('<li><a href="#' + $(this).html() + '" data-column="' + $(this).index() + '">' + $(this).html() + '</a></li>');
                        }
                    });

                    //Append advance operations to list table toolbar
                    $('.dataTable.list-table').closest('.dataTables_wrapper').find('.dataTablesTop .dataTables_toolbar').html('' +
                        '<ul class="nav nav-pills navbar-right remove-margin" role="tablist">' +
                        '<li><button data-click-event="toggle-selected" class="btn btn-default btn-primary">Select All</li>' +
                        '<li><button data-click-event="toggle-list-view" data-view="grid" class="btn btn-default"><i class="fw fw-grid"></i></button></li>' +
                        '<li><button data-click-event="toggle-list-view" data-view="list" class="btn btn-default"><i class="fw fw-list"></i></button></li>' +
                        '<li><button class="btn btn-default" data-toggle="dropdown"><i class="fw fw-sort"></i></button>' + dropdownmenu[0].outerHTML + '</li>' +
                        '</ul>'
                    );

                    //Sorting dropdown menu select function
                    $('.dataTables_wrapper .sort-list li a').click(function() {
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

                    //Select/Deselect all rows functions
                    $('.dataTables_wrapper [data-click-event=toggle-selected]').click(function() {
                        var button = this,
                            thisTable = $(this).closest('.dataTables_wrapper').find('.dataTable').dataTable();

                        if ($(button).html() == 'Select All') {
                            thisTable.api().rows().every(function() {
                                $(this.node()).addClass(ROW_SELECTED_CLASS);
                                $(button).html('Deselect All');
                            });
                        } else if ($(button).html() == 'Deselect All') {
                            thisTable.api().rows().every(function() {
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

                        thisTable.api().rows().every(function() {
                            if (!$(this.node()).hasClass(ROW_SELECTED_CLASS)) {
                                $(button).closest('.dataTables_wrapper').find('[data-click-event=toggle-selected]').html('Select All');
                            }
                        });
                    });

                    //list table list/grid view toggle function
                    var toggleButton = $('[data-click-event=toggle-list-view]');
                    toggleButton.click(function() {
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
                    $('[data-click-event=delete-selected-rows]').click(function() {
                        var thisTable = $(this).closest('.dataTables_wrapper').find('.dataTable').dataTable();
                        thisTable.api().rows('.' + ROW_SELECTED_CLASS).remove().draw(false);
                    });

                    $('.random-thumbs .thumbnail.icon').random_background_color();

                }
            }, settings)
        );

    };


    /**
     * Tree view function
     * @return {Null}
     */
    $.fn.tree_view = function() {
        var tree = $(this);
        tree.find('li').has("ul").each(function() {
            var branch = $(this); //li with children ul
            branch.prepend('<i class="icon"></i>');
            branch.addClass('branch');
            branch.on('click', function(e) {
                if (this == e.target) {
                    var icon = $(this).children('i:first');
                    icon.closest('li').toggleAttr('aria-expanded', 'true', 'false');
                }
            });
        });

        tree.find('.branch .icon').each(function() {
            $(this).on('click', function() {
                $(this).closest('li').click();
            });
        });

        tree.find('.branch > a').each(function() {
            $(this).on('click', function(e) {
                $(this).closest('li').click();
                e.preventDefault();
            });
        });

        tree.find('.branch > button').each(function() {
            $(this).on('click', function(e) {
                $(this).closest('li').click();
                e.preventDefault();
            });
        });
    };


    /**
     * Sidebar function
     * @return {Null}
     */
    $.sidebar_toggle = function(action, target, container) {
        var elem = '[data-toggle=sidebar]',
            button,
            container,
            conrainerOffsetLeft,
            conrainerOffsetRight,
            target,
            targetOffsetLeft,
            targetOffsetRight,
            targetWidth,
            targetSide,
            relationship,
            pushType,
            buttonParent,
            complete;
        
        var sidebar_window = {
            update: function(target, container, button){
                conrainerOffsetLeft = $(container).data('offset-left') ? $(container).data('offset-left') : 0,
                conrainerOffsetRight = $(container).data('offset-right') ? $(container).data('offset-right') : 0,
                targetOffsetLeft = $(target).data('offset-left') ? $(target).data('offset-left') : 0,
                targetOffsetRight = $(target).data('offset-right') ? $(target).data('offset-right') : 0,
                targetWidth = $(target).data('width'),
                targetSide = $(target).data("side"),
                pushType = $(container).parent().is('body') == true ? 'padding' : 'margin';
                
                if(button !== undefined){
                    relationship = button.attr('rel') ? button.attr('rel') : '',
                    buttonParent = $(button).parent();
                }
            },
            show: function(){
                
                $(target).trigger('show.sidebar');
                
                $(target).addClass('toggled');
                
                if(button !== undefined){
                    if(relationship !== ''){
                        // Removing active class from all relative buttons
                        $(elem+'[rel='+relationship+']:not([data-handle=close])').removeClass("active");
                        $(elem+'[rel='+relationship+']:not([data-handle=close])').attr('aria-expanded', 'false');
                    }

                    // Adding active class to button
                    if(button.attr('data-handle') !== 'close'){
                        button.addClass("active");
                        button.attr('aria-expanded', 'true');
                    }

                    if(buttonParent.is('li')) {
                        if(relationship !== ''){
                            $(elem+'[rel='+relationship+']:not([data-handle=close])').parent().removeClass("active");
                            $(elem+'[rel='+relationship+']:not([data-handle=close])').parent().attr('aria-expanded', 'false');
                        }
                        buttonParent.addClass("active");
                        buttonParent.attr('aria-expanded', 'true');
                    }
                }

                // Sidebar open function
                if (targetSide == 'left'){
                    if((button !== undefined) && (button.attr('data-container-divide'))){
                        $(container).css(pushType+'-'+targetSide, targetWidth + targetOffsetLeft);
                    }
                    $(target).css(targetSide, targetOffsetLeft);
                }
                else if (targetSide == 'right'){
                    if((button !== undefined) && (button.attr('data-container-divide'))){
                        $(container).css(pushType+'-'+targetSide, targetWidth + targetOffsetRight);
                    }
                    $(target).css(targetSide, targetOffsetRight);
                }
                
                $(target).trigger('shown.sidebar');
            },
            hide: function(){
                
                $(target).trigger('hide.sidebar');
                
                $(target).removeClass('toggled');
                
                if(button !== undefined){
                    if(relationship !== ''){
                        // Removing active class from all relative buttons
                        $(elem+'[rel='+relationship+']:not([data-handle=close])').removeClass("active");
                        $(elem+'[rel='+relationship+']:not([data-handle=close])').attr('aria-expanded', 'false');
                    }
                    // Removing active class from button
                    if(button.attr('data-handle') !== 'close'){
                        button.removeClass("active");
                        button.attr('aria-expanded', 'false');
                    }

                    if($(button).parent().is('li')){
                        if(relationship !== ''){
                            $(elem+'[rel='+relationship+']:not([data-handle=close])').parent().removeClass("active");
                            $(elem+'[rel='+relationship+']:not([data-handle=close])').parent().attr('aria-expanded', 'false');
                        }
                    }
                }

                // Sidebar close function
                if (targetSide == 'left'){
                    if((button !== undefined) && (button.attr('data-container-divide'))){
                        $(container).css(pushType+'-'+targetSide, targetOffsetLeft);
                    }
                    $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetLeft));
                }
                else if (targetSide == 'right'){
                    if((button !== undefined) && (button.attr('data-container-divide'))){
                        $(container).css(pushType+'-'+targetSide, targetOffsetRight);
                    }
                    $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetRight));
                }
                
                $(target).trigger('hidden.sidebar');
            }
        };

        if (action === 'show') {
            sidebar_window.update(target, container);
            sidebar_window.show();
        }
        if (action === 'hide') {
            sidebar_window.update(target, container);
            sidebar_window.hide();
        }
        
        // binding click function
        $('body').off('click', elem);
        $('body').on('click', elem, function(e) {
            e.preventDefault();

            button = $(this);
            container = button.data('container');
            target = button.data('target');

            sidebar_window.update(target, container, button);

            /**
             * Sidebar function on data container divide
             * @return {Null}
             */
            if(button.attr('aria-expanded') == 'false'){
                sidebar_window.show();
            }
            else if (button.attr('aria-expanded') == 'true') {
                sidebar_window.hide();
            }

        });
    };


}(jQuery));


(function($) {

    /**
     * Mutationobserver wrapper function
     * @usage  $(el).MutationObserverWrapper({
     *             config: {
     *                 childList : true
     *             },
     *             callback : function(mutationObj){
     *
     *             }
     *         })
     * @return {Null}
     */
    $.fn.MutationObserverWrapper = function(options) {

        if (isSupported() === false) {
            throw 'Mutation observer is not supported by your browser.'
        }

        var defaults = {
            config: {
                childList: true,
                attributes: true,
                characterData: true,
                subtree: true,
                attributeOldValue: true,
                characterDataOldValue: true,
                attributeFilter: []
            },
            callback: function(mutations) {
                mutations.forEach(function(mutation) {
                    console.log(mutation);
                })
            }
        };

        var target = $(this)[0];
        var plugin = $.fn.MutationObserverWrapper;
        var options = $.extend({}, defaults, options);

        var observer = new MutationObserver(options.callback);

        observer.observe(target, options.config);

    };

    function isSupported() {
        var prefixes = ['WebKit', 'Moz', 'O', 'Ms', ''];
        for (var i = 0; i < prefixes.length; i++) {
            if (prefixes[i] + 'MutationObserver' in window) {
                return window[prefixes[i] + 'MutationObserver'];
            }
        }
        return false;
    }

}(jQuery));

$(document).ready(function() {

    $('.tree-view').tree_view();
    $.file_input();
    $.sidebar_toggle();

    if(typeof $.fn.tooltip == 'function'){
        $('[data-toggle="tooltip"]').tooltip();
    }else{
        console.warn('Warning : Dependency missing - tooltip Library');
    }

    $('[data-state="loading"]').loading('show');

    $('.dropdown-menu input').click(function(e) {
        e.stopPropagation();
    });

    $(responsiveTextSleector).responsive_text(responsiveTextRatio);

    if(typeof $.fn.select2 == 'function'){
        $('.select2').select2();
    }else{
        console.warn('Warning : Dependency missing - Select2 Library');
    }

});

$(window).scroll(function() {
    $(responsiveTextSleector).responsive_text(responsiveTextRatio);
});

$(document).bind('click', function() {
    $(responsiveTextSleector).responsive_text(responsiveTextRatio);
});

$(function() {

    /***********************************************************
     *  if body element change call responsive text function
     ***********************************************************/
    var target = document.querySelector('body');
    var observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
            $(responsiveTextSleector).responsive_text(responsiveTextRatio);
        });
    });
    var config = {
        attributes: true,
        childList: true,
        characterData: true
    };
    observer.observe(target, config);

    if(typeof $.fn.tooltip == 'function'){
        $('[data-toggle="tooltip"]').tooltip();
    }else{
        console.warn('Warning : Dependency missing - tooltip Library');
    }

});