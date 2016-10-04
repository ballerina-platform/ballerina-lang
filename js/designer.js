$(function () {
    var dashboard,
        page,
        pageType,
        activeComponent,
        breadcrumbs = [],
        storeCache = {
            gadget: [],
            widget: [],
            layout: []
        },
        nonCategoryKeyWord = "null",
        designerScrollTop = 0,
        dashboardsApi = ues.utils.tenantPrefix() + 'apis/dashboards',
        dashboardsUrl = ues.utils.tenantPrefix() + 'dashboards',
        resolveURI = ues.dashboards.resolveURI,
        findPage = ues.dashboards.findPage,
        lang = navigator.languages ? navigator.languages[0] : (navigator.language || navigator.userLanguage || navigator.browserLanguage),
        COMPONENTS_PAGE_SIZE = 20,
        DEFAULT_DASHBOARD_VIEW = 'default',
        ANONYMOUS_DASHBOARD_VIEW = 'anon',
        FULL_COMPONENT_VIEW = 'full',
        DEFAULT_COMPONENT_VIEW = 'default';

    /**
     * Show HTML modal
     * @param {String} content      HTML content
     * @param {function} beforeShow Function to be invoked just before showing the modal
     * @return {null}
     * @private
     */
    var showHtmlModal = function (content, beforeShow) {
        var el = $('#designerModal');
        el.find('.modal-content').html(content);
        if (beforeShow && typeof beforeShow === 'function') {
            beforeShow();
        }

        el.modal();
    };

    /**
     * Show confirm message with yes/no buttons
     * @param {String} title    Title of the confirmation box
     * @param {String} message  HTML content
     * @param {function} ok     Callback function for yes button
     * @return {null}
     * @private
     */
    var showConfirm = function (title, message, ok) {
        var content = modalConfirmHbs({title: title, message: message});
        showHtmlModal(content, function () {
            var el = $('#designerModal');
            el.find('#ues-modal-confirm-yes').on('click', function () {
                if (ok && typeof ok === 'function') {
                    ok();
                    el.modal('hide');
                }
            });
        });
    };

    /**
     * Get Gridstack object
     * @return {Object}
     * @private
     */
    var getGridstack = function () {
        return $('.grid-stack').data('gridstack');
    }

    /**
     * Show the information message with ok button.
     * @param1 title {String}
     * @param2 message {String}
     * @private
     * */
    var showInformation = function (title, message) {
        var content = modalInfoHbs({title: title, message: message});
        showHtmlModal(content, null);
    };

    /**
     * Clone JSON object
     * @param {Object} o    Object to be cloned
     * @returns {Object}
     * @private
     */
    var clone = function (o) {
        return JSON.parse(JSON.stringify(o));
    };

    /**
     * Precompiling Handlebar templates
     */
    var layoutsListHbs = Handlebars.compile($("#ues-layouts-list-hbs").html() || '');

    var designerHeadingHbs = Handlebars.compile($('#ues-designer-heading-hbs').html() || '');

    var componentsListHbs = Handlebars.compile($("#ues-components-list-hbs").html() || '');

    var noComponentsHbs = Handlebars.compile($("#ues-no-components-hbs").html() || '');

    var componentToolbarHbs = Handlebars.compile($("#ues-component-toolbar-hbs").html() || '');

    var pageOptionsHbs = Handlebars.compile($("#ues-page-properties-hbs").html() || '');

    var componentPropertiesHbs = Handlebars.compile($("#ues-component-properties-hbs").html() || '');

    var pagesListHbs = Handlebars.compile($("#ues-pages-list-hbs").html() || '');

    var bannerHbs = Handlebars.compile($('#ues-dashboard-banner-hbs').html() || '');

    var componentBoxListHbs = Handlebars.compile($("#ues-component-box-list-hbs").html() || '');

    var componentBoxContentHbs = Handlebars.compile($('#ues-component-box-content-hbs').html() || '');

    var noPagesHbs = Handlebars.compile($('#ues-no-pages-hbs').html() || '');

    var modalConfirmHbs = Handlebars.compile($('#ues-modal-confirm-hbs').html() || '');

    var modalInfoHbs = Handlebars.compile($('#ues-modal-info-hbs').html() || '');

    var newBlockHbs = Handlebars.compile($("#ues-new-block-hbs").html() || '');

    /**
     * Generates a random ID
     * @return {String}
     * @private
     */
    var randomId = function () {
        return Math.random().toString(36).slice(2);
    };

    /**
     * Initialize the nano scroller
     * @return {null}
     * @private
     * */
    var initNanoScroller = function () {
        $(".nano").nanoScroller();
    };

    /**
     * Update component properties panel and save
     * @param {Object} sandbox JQuery wrapped sandbox HTML element
     * @return {null}
     * @private
     */
    var updateComponentProperties = function (sandbox) {
        var notifiers = {},
            options = {},
            settings = {},
            styles = {},
            id = sandbox.data('component');

        saveOptions(sandbox, options);
        saveSettings(sandbox, settings);
        saveStyles(sandbox, styles, id);
        saveNotifiers(sandbox, notifiers);

        saveComponentProperties(id, {
            options: options,
            settings: settings,
            styles: styles,
            notifiers: notifiers
        });
    };

    /**
     * Renders the component properties panel
     * @param {Object} component Component object
     * @return {null}
     * @private
     */
    var renderComponentProperties = function (component) {
        var ctx = buildPropertiesContext(component, page),
            propertiesContainer = $('.ues-component-properties-container');

        propertiesContainer
            .html(componentPropertiesHbs(ctx))
            .on('change', 'input, select, textarea', function () {
                updateComponentProperties($(this).closest('.ues-component-properties'));
            })
            .on('keypress', 'input, select, textarea', function (e) {
                if (e.which === 13) {
                    updateComponentProperties($(this).closest('.ues-component-properties'));
                    e.preventDefault();
                    return;
                }
            });

        propertiesContainer
            .find('.ues-localized-title')
            .on("keypress", function (e) {
                return sanitizeOnKeyPress(this, e, /[^a-z0-9-\s]/gim);
            })
            .on('change', function (e) {
                if ($.trim($(this).val()) == '') {
                    $(this).val('');
                }
            });
    };

    /**
     * Render maximized view of a gadget
     * @param {Object} component The component to be rendered
     * @param {String} view      Component view mode
     * @return {null}
     * @private
     */
    var renderMaxView = function (component, view) {
        component.viewOption = view;
        ues.components.update(component, function (err, block) {
            if (err) {
                throw err;
            }
        });
    };

    /**
     * Find an asset of the given type from the store cache
     * @param {String} type The cache type
     * @param {String} id   Asset ID
     * @return {Object}
     * @private
     */
    var findStoreCache = function (type, id) {
        var i;
        var item;
        var items = storeCache[type];
        var length = items.length;
        for (i = 0; i < length; i++) {
            item = items[i];
            if (item.id === id) {
                return clone(item);
            }
        }
    };

    /**
     * Find a given component in the current page
     * @param {String} id   The component ID
     * @return {Object}
     * @private
     */
    var findComponent = function (id) {
        var i;
        var length;
        var area;
        var component;
        var components;
        pageType = pageType ? pageType : DEFAULT_DASHBOARD_VIEW;
        var content = page.content[pageType];
        for (area in content) {
            if (content.hasOwnProperty(area)) {
                components = content[area];
                length = components.length;
                for (i = 0; i < length; i++) {
                    component = components[i];
                    if (component.id === id) {
                        return component;
                    }
                }
            }
        }
    };

    /**
     * Save component properties
     * @param {String} id   The component ID
     * @param {Object} data Component properties data
     * @return {null}
     * @private
     */
    var saveComponentProperties = function (id, data) {
        var o;
        var opt;
        var block = findComponent(id);
        var content = block.content;

        //save options
        var options = content.options;
        var opts = data.options;
        for (opt in opts) {
            if (opts.hasOwnProperty(opt)) {
                o = options[opt] || (options[opt] = {});
                o.value = opts[opt];
            }
        }

        //save settings
        content.settings = data.settings;

        //save styles
        content.styles = data.styles;

        //save wiring
        var event;
        var listener;
        var notifiers = data.notifiers;
        var listen = content.listen;
        for (event in notifiers) {
            if (notifiers.hasOwnProperty(event)) {
                listener = listen[event];
                listener.on = notifiers[event];
            }
        }

        ues.dashboards.rewire(page, pageType);
        updateComponent(id);
        saveDashboard();
    };

    /**
     * Removes and destroys the given component from the page
     * @param {Object} component    The component to be removed
     * @param {function} done       Callback function
     * @return {null}
     * @private
     */
    var removeComponent = function (component, done) {
        destroyComponent(component, function (err) {
            if (err) {
                return done(err);
            }
            var container = $('#' + component.id);
            var area = container.closest('.ues-component-box').attr('id');
            pageType = pageType ? pageType : DEFAULT_DASHBOARD_VIEW;
            var content = page.content[pageType];
            area = content[area];
            var index = area.indexOf(component);
            area.splice(index, 1);
            container.remove();

            var compId = $('.ues-component-properties').data('component');
            if (compId !== component.id) {
                return done();
            }
            $('.ues-component-properties .ues-component-properties-container').empty();
            done();
        });
    };

    /**
     * Destroys the given component
     * @param {Object} component    Component to be destroyed
     * @param {function} done       Callback function
     * @return {null}
     * @private
     */
    var destroyComponent = function (component, done) {
        ues.components.destroy(component, function (err) {
            if (err) {
                return err;
            }
            done(err);
        });
    };

    /**
     * Destroys a given list of components of an area
     * @param {Object[]} components Components to be removed
     * @param {function} done       Callback function
     * @return {null}
     * @private
     */
    var destroyArea = function (components, done) {
        var i;
        var length = components.length;
        var tasks = [];
        for (i = 0; i < length; i++) {
            tasks.push((function (component) {
                return function (done) {
                    destroyComponent(component, function (err) {
                        done(err);
                    });
                };
            }(components[i])));
        }
        async.parallel(tasks, function (err, results) {
            done(err);
        });
    };

    /**
     * Destroys all areas in a given page
     * @param {Object} page     The page object
     * @param {String} pageType Type of the page
     * @param {function} done   Callback function
     * @return {null}
     * @private
     */
    var destroyPage = function (page, pageType, done) {
        var area;
        pageType = pageType || DEFAULT_DASHBOARD_VIEW;

        var content = page.content[pageType];
        var tasks = [];
        for (area in content) {
            if (content.hasOwnProperty(area)) {
                tasks.push((function (area) {
                    return function (done) {
                        destroyArea(area, function (err) {
                            done(err);
                        });
                    };
                }(content[area])));
            }
        }
        async.parallel(tasks, function (err, results) {
            $('.gadgets-grid').empty();
            if (!done) {
                return;
            }

            done(err);
        });
    };

    /**
     * Remove and destroys a given page
     * @param {String} pid      Page ID
     * @param {type}            Type of the page
     * @param {function} done   Callback function
     * @return {null}
     * @private
     */
    var removePage = function (pid, type, done) {
        var p = findPage(dashboard, pid);
        var pages = dashboard.pages;
        var index = pages.indexOf(p);
        pages.splice(index, 1);
        if (page.id !== pid) {
            return done(false);
        }
        destroyPage(p, type, done);
    };

    /**
     * Pops up the dashboard preview page
     * @param {Object} page     The page object
     * @return {null}
     * @private
     */
    var previewDashboard = function (page) {
        var addingParam = ues.global.type.toString().localeCompare(ANONYMOUS_DASHBOARD_VIEW) == 0 ? '?isAnonView=true' : '';
        var pageURL = dashboard.landing !== page.id ? page.id : '';
        var url = dashboardsUrl + '/' + dashboard.id + '/' + pageURL + addingParam;
        window.open(url, '_blank');
    };

    /**
     * Generate Noty Messages as to the content given parameters
     * @param {String} text     The message
     * @param {function} ok     The OK function
     * @param {function} cancel The Cancel function
     * @param {String} type     Type of the message
     * @param {String} layout   The layout
     * @param {Number} timeout  Timeout
     * @return {Object}
     * @private
     * */
    var generateMessage = function (text, ok, cancel, type, layout, timeout, close) {

        var properties = {};
        properties.text = text;
        if (ok || cancel) {
            properties.buttons = [
                {
                    addClass: 'btn btn-primary', text: 'Ok', onClick: function ($noty) {
                    $noty.close();
                    if (ok) {
                        ok();
                    }
                }
                },
                {
                    addClass: 'btn btn-danger', text: 'Cancel', onClick: function ($noty) {
                    $noty.close();
                    if (cancel) {
                        cancel();
                    }
                }
                }
            ];
        }

        if (timeout) {
            properties.timeout = timeout;
        }

        if (close) {
            properties.closeWith = close;
        }

        properties.layout = layout;
        properties.theme = 'wso2';
        properties.type = type;
        properties.dismissQueue = true;
        properties.killer = true;
        properties.maxVisible = 1;
        properties.animation = {
            open: {height: 'toggle'},
            close: {height: 'toggle'},
            easing: 'swing',
            speed: 500
        };

        return noty(properties);
    };

    /**
     * Saves the dashboard content
     * @return {null}
     * @private
     */
    var saveDashboard = function () {
        var method = 'PUT',
            url = dashboardsApi + '/' + dashboard.id,
            isRedirect = false;

        $.ajax({
            url: url,
            method: method,
            data: JSON.stringify(dashboard),
            contentType: 'application/json'
        }).success(function (data) {
            generateMessage("Dashboard saved successfully", null, null, "success", "topCenter", 2000, null);
            if (isRedirect) {
                isRedirect = false;
                window.location = dashboardsUrl + '/' + dashboard.id + "?editor=true";
            }
        }).error(function (xhr, status, err) {
            if (xhr.status === 403) {
                window.location.reload();
                return;
            }

            generateMessage("Error saving the dashboard", null, null, "error", "topCenter", 2000, null);
            console.log('error saving dashboard');
        });
    };

    /**
     * Initializes the component toolbar
     * @return {null}
     * @private
     */
    var initComponentToolbar = function () {
        var designer = $('.gadgets-grid');

        designer.on('click', '.ues-component-box .ues-component-full-handle', function () {

            var id = $(this).closest('.ues-component').attr('id'),
                component = findComponent(id),
                componentContainer = $(this).closest('.ues-component-box'),
                gsBlock = componentContainer.parent(),
                componentContainerId = componentContainer.attr('id'),
                componentBody = componentContainer.find('.ues-component-body'),
                gsContainer = $('.grid-stack'),
                trashButton = componentContainer.find('.ues-component-actions .ues-trash-handle');

            if (component.fullViewPoped) {
                // rendering normal view

                getGridstack().enable();

                // restore the size of the gridstack
                gsContainer.height(gsContainer.attr('data-orig-height')).removeAttr('data-orig-height');

                trashButton.show();
                $('.grid-stack-item').show();
                
                // restore the previous data-height value and remove the backup value
                componentContainer
                    .attr('data-height', componentContainer.attr('data-original-height'))
                    .removeAttr('data-original-height');

                //minimize logic
                gsBlock
                    .removeClass('ues-component-fullview')
                    .css('height', '')
                    .on('transitionend webkitTransitionEnd oTransitionEnd otransitionend MSTransitionEnd', function () {
                        componentBody.show();
                        designer.scrollTop(designerScrollTop);
                        renderMaxView(component, DEFAULT_COMPONENT_VIEW);
                        component.fullViewPoped = false;
                    });

                $(this)
                    .attr('title', $(this).data('maximize-title'))
                    .find('i.fw')
                    .removeClass('fw-contract')
                    .addClass('fw-expand');

                componentBody.hide();

            } else {
                // rendering full view

                var pageEl = $('.page-content');

                // backup the scroll position
                designerScrollTop = designer.scrollTop();

                getGridstack().disable();

                // backup the size of gridstack and reset element size
                gsContainer.attr('data-orig-height', gsContainer.height()).height('auto');

                trashButton.hide();
                $('.grid-stack-item:not([data-id=' + componentContainerId + '])').hide();
                
                // replace the data-height value with new container height (and backup previous one)
                componentContainer
                    .attr('data-original-height', componentContainer.attr('data-height'))
                    .attr('data-height', pageEl.height() - 120);
                
                //maximize logic
                gsBlock
                    .addClass('ues-component-fullview')
                    .css('height', pageEl.height() - 120)
                    .on('transitionend webkitTransitionEnd oTransitionEnd otransitionend MSTransitionEnd', function () {
                        componentBody.show();
                        renderMaxView(component, FULL_COMPONENT_VIEW);
                        component.fullViewPoped = true;
                    });

                $(this)
                    .attr('title', $(this).data('minimize-title'))
                    .find('i.fw')
                    .removeClass('fw-expand')
                    .addClass('fw-contract');

                componentBody.hide();
            }
            initNanoScroller();
        });

        designer.on('click', '.ues-component-box .ues-component-properties-handle', function () {

            var id = $(this).closest('.ues-component').attr('id');
            renderComponentProperties(findComponent(id));
        });

        designer.on('click', '.ues-component-box .ues-trash-handle', function () {

            var that = $(this),
                hbs = Handlebars.compile($('#ues-modal-confirm-delete-block-hbs').html() || ''),
                hasComponent = false;
            
            if (that.closest('.ues-component-box').find('.ues-component').attr('id')) {
                hasComponent = true;
            }

            showHtmlModal(hbs({ hasComponent: hasComponent }), function () {

                var designerModal = $('#designerModal');
                designerModal.find('#btn-delete').on('click', function () {

                    var action = designerModal.find('.modal-body input[name="delete-option"]:checked').val(),
                        componentBox = that.closest('.ues-component-box'),
                        id = componentBox.find('.ues-component').attr('id'),
                        removeBlock = (action == 'block');

                    if (id) {
                        removeComponent(findComponent(id), function (err) {
                            if (err) {
                                removeBlock = false;
                                console.error(err);
                            }
                            saveDashboard();
                        });
                    }

                    if (removeBlock) {
                        getGridstack().remove_widget(componentBox.parent());
                        updateLayout();
                    } else {
                        componentBox.html(componentBoxContentHbs());
                    }

                    designerModal.modal('hide');
                });
            });
        });
    };

    /**
     * Return the ID of the page
     * @return {String}     ID of the page
     * @private
     */
    var getPageId = function () {
        return page.id;
    };

    /**
     * Renders the component toolbar of a given component
     * @param {Object} component
     * @return {null}
     * @private
     */
    var renderComponentToolbar = function (component) {
        if (component) {
            $('#' + component.id + ' .ues-component-actions').html($(componentToolbarHbs(component.content)));
        }
    };

    /**
     * Updates the styles of a given store asset
     * @param {Object} asset
     * @return {null}
     * @private
     */
    var updateStyles = function (asset) {
        var styles = asset.styles || (asset.styles = {
                title: true,
                borders: true
            });
        if (styles.title && typeof styles.title === 'boolean') {
            styles.title = asset.title;
        }
    };

    /**
     * Creates a component in the given container
     * @param {Object} container
     * @param {Object} asset
     * @return {null}
     * @private
     */
    var createComponent = function (container, asset) {
        var id = randomId();
        var area = container.attr('id');
        pageType = pageType ? pageType : DEFAULT_DASHBOARD_VIEW;
        var content = page.content[pageType];
        content = content[area] || (content[area] = []);
        updateStyles(asset);
        var component = {
            id: id,
            content: asset
        };
        content.push(component);
        ues.components.create(container, component, function (err, block) {
            if (err) {
                throw err;
            }
            renderComponentToolbar(component);
            container.find('.ues-component-actions .ues-component-properties-handle').click();
            saveDashboard();
        });
    };

    /**
     * Triggers update hook of a given component
     * @param {String} id   Component ID
     * @return {null}
     * @private
     */
    var updateComponent = function (id) {
        ues.components.update(findComponent(id), function (err) {
            if (err) {
                throw err;
            }
        });
    };

    /**
     * Builds up the component notifiers
     * @param {Object[]} notifiers  List of events
     * @param {Object} current      Current component
     * @param {Object} component    Other component
     * @return {null}
     * @private
     */
    var componentNotifiers = function (notifiers, current, component) {
        if (current.id === component.id) {
            return;
        }
        var notify = component.content.notify;
        if (!notify) {
            return;
        }
        var event;
        var events;
        var data;
        for (event in notify) {
            if (notify.hasOwnProperty(event)) {
                data = notify[event];
                events = notifiers[data.type] || (notifiers[data.type] = []);
                events.push({
                    from: component.id,
                    event: event,
                    type: data.type,
                    content: component.content,
                    description: data.description
                });
            }
        }
    };

    /**
     * Builds up the area notifiers
     * @param {Object[]} notifiers  List of events
     * @param {Object} component    The component
     * @param {Object[]} components All components
     * @return {null}
     * @private
     */
    var areaNotifiers = function (notifiers, component, components) {
        var i;
        var length = components.length;
        for (i = 0; i < length; i++) {
            componentNotifiers(notifiers, component, components[i]);
        }
    };

    /**
     * Builds up the page notifiers
     * @param {Object} component    The component
     * @param {Object} page         The page
     * @returns {Object[]} Notifiers
     * @private
     */
    var pageNotifiers = function (component, page) {
        var area;
        var notifiers = {};
        pageType = pageType ? pageType : DEFAULT_DASHBOARD_VIEW;
        var content = page.content[pageType];
        for (area in content) {
            if (content.hasOwnProperty(area)) {
                areaNotifiers(notifiers, component, content[area]);
            }
        }
        return notifiers;
    };

    /**
     * Find matching notifiers for a given component
     * @param component
     * @param page
     * @returns {Array}
     * @private
     */
    var findNotifiers = function (component, page) {
        var event, listener, notifiers;
        var listeners = [];
        var content = component.content;
        var listen = content.listen;
        if (!listen) {
            return listeners;
        }
        notifiers = pageNotifiers(component, page);
        for (event in listen) {
            if (listen.hasOwnProperty(event)) {
                listener = listen[event];
                listeners.push({
                    event: event,
                    title: listener.title,
                    description: listener.description,
                    notifiers: notifiers[listener.type] || []
                });
            }
        }
        console.log(listeners);
        return listeners;
    };

    /**
     * Find the wired notifier
     * @param from
     * @param event
     * @param notifiers
     * @returns {*}
     * @private
     */
    var wiredNotifier = function (from, event, notifiers) {
        var i, notifier;
        var length = notifiers.length;
        for (i = 0; i < length; i++) {
            notifier = notifiers[i];
            if (notifier.from === from && notifier.event === event) {
                return notifier;
            }
        }
    };

    /**
     * Wire an event
     * @param on
     * @param notifiers
     * @private
     */
    var wireEvent = function (on, notifiers) {
        var i, notifier;
        var length = on.length;
        for (i = 0; i < length; i++) {
            notifier = on[i];
            notifier = wiredNotifier(notifier.from, notifier.event, notifiers);
            if (!notifier) {
                continue;
            }
            notifier.wired = true;
        }
    };

    /**
     * Get all notifiers of a given event
     * @param event
     * @param notifiers
     * @returns {notifiers|*}
     * @private
     */
    var eventNotifiers = function (event, notifiers) {
        var i, events;
        var length = notifiers.length;
        for (i = 0; i < length; i++) {
            events = notifiers[i];
            if (events.event === event) {
                return events.notifiers;
            }
        }
    };

    /**
     * Wire events of a given component
     * @param {Object} component
     * @param {Object[]} notifiers
     * @returns {Object[]}
     * @private
     */
    var wireEvents = function (component, notifiers) {
        var listen = component.content.listen;
        if (!listen) {
            return notifiers;
        }
        var event, on;
        for (event in listen) {
            if (listen.hasOwnProperty(event)) {
                on = listen[event].on;
                if (!on) {
                    continue;
                }
                wireEvent(on, eventNotifiers(event, notifiers));
            }
        }
        return notifiers;
    };

    /**
     * Check whether options are available or not
     * @param optionKeys {Object}
     * @param options {Object}
     * @return isAvailable {boolean}
     * @private
     * */
    var isOptionAvailable = function (optionKeys, options) {
        var isAvailable = false;
        for (var i = 0; i < optionKeys.length; i++) {
            if (options[optionKeys[i]].type.toUpperCase() != "HIDDEN") {
                isAvailable = true;
                break;
            }
        }
        return isAvailable;
    };

    /**
     * Build the hbs context for component properties panel
     * @param component
     * @param page
     * @returns {{id: (*|component.id), title: *, options: *, styles: (styles|*|buildPropertiesContext.styles), settings: *, listeners: *}}
     * @private
     */
    var buildPropertiesContext = function (component, page) {
        var notifiers = findNotifiers(component, page),
            content = component.content,
            optionsAvailable = isOptionAvailable(Object.keys(content.options), content.options),
            options = optionsAvailable ? content.options : {};

        return {
            id: component.id,
            title: content.title,
            options: options,
            styles: content.styles,
            settings: content.settings,
            listeners: wireEvents(component, notifiers)
        };
    };

    /**
     * Check whether current landing page is anonymous or not.
     * @param {String} landing  Current landing page
     * @return {boolean}
     * @private
     */
    var checkForAnonLandingPage = function (landing) {
        var isLandingAnon = false;
        for (var availablePage in dashboard.pages) {
            if (dashboard.pages[availablePage].id == landing) {
                isLandingAnon = dashboard.pages[availablePage].isanon;
                break;
            }
        }

        return isLandingAnon;
    };

    /**
     * Check whether there are any anonymous pages.
     * @param {String} pageId Page ID
     * @return [boolean]
     * @private
     */
    var checkForAnonPages = function (pageId) {
        var isAnonPagesAvailable = false;
        for (var availablePage in dashboard.pages) {
            if (dashboard.pages[availablePage].id != pageId && dashboard.pages[availablePage].isanon) {
                isAnonPagesAvailable = dashboard.pages[availablePage].isanon;
                break;
            }
        }

        return isAnonPagesAvailable;
    };

    /**
     * Check whether dashboard is anon or not based on whether there are anon
     * pages available or not.
     * @return {boolean} true if there are any page with anon view.
     */
    var checkWhetherDashboardIsAnon = function () {
        var isDashboardAnon = false;
        for (var availablePage in dashboard.pages) {
            if (dashboard.pages[availablePage].isanon) {
                isDashboardAnon = true;
                break;
            }
        }

        return isDashboardAnon;
    };

    /**
     * Check whether there are any pages which has given id.
     * @param {String} pageId
     * @return {boolean}
     * @private
     */
    var checkForPagesById = function (pageId) {
        var isPageAvailable = false;
        for (var availablePage = 0; availablePage < dashboard.pages.length; availablePage++) {
            if (dashboard.pages[availablePage].id == pageId) {
                isPageAvailable = true;
                break;
            }
        }

        return isPageAvailable;
    };

    /**
     * Check whether there are any page which has the given title.
     * @param {String} pageTitle
     * @return {boolean}
     * @private
     */
    var checkForPagesByTitle = function (pageTitle) {
        var isPageAvailable = false;
        for (var availablePage = 0; availablePage < dashboard.pages.length; availablePage++) {
            if (dashboard.pages[availablePage].title.toUpperCase() == pageTitle.toUpperCase()) {
                isPageAvailable = true;
                break;
            }
        }

        return isPageAvailable;
    };

    /**
     * Show error style for given element
     * @param {Object} element
     * @param {Object} errorElement
     * @return {null}
     * @private
     */
    var showInlineError = function (element, errorElement) {
        element.val('');
        element.parent().addClass("has-error");
        element.addClass("has-error");
        element.parent().find("span.glyphicon").removeClass("hide");
        element.parent().find("span.glyphicon").addClass("show");
        errorElement.removeClass("hide");
        errorElement.addClass("show");
    };

    /**
     * Hide error style for given element
     * @param {Object} element
     * @param {Object} errorElement
     * @return {null}
     * @private
     */
    var hideInlineError = function (element, errorElement) {
        element.parent().removeClass("has-error");
        element.removeClass("has-error");
        element.parent().find("span.glyphicon").removeClass("show");
        element.parent().find("span.glyphicon").addClass("hide");
        errorElement.removeClass("show");
        errorElement.addClass("hide");
    };

    /**
     * Update page options
     * @param {Object} e
     * @return {Boolean}
     * @private
     */
    var updatePageProperties = function (e) {

        var titleError = $("#title-error"),
            idError = $("#id-error"),
            hasError = false,
            id = $('input[name=id]', e),
            title = $('input[name=title]', e),
            idVal = $.trim(id.val()),
            titleVal = $.trim(title.val());

        // validate inputs
        hideInlineError(id, idError);
        hideInlineError(title, titleError);

        if (!idVal) {
            showInlineError(id, idError);
            hasError = true;
        }

        if (!titleVal) {
            showInlineError(title, titleError);
            hasError = true;
        }

        if (hasError) {
            return false;
        }

        var landing = $('input[name=landing]', e),
            toggleView = $('#toggle-dashboard-view'),
            anon = $('input[name=anon]', e),
            fluidLayout = $('input[name=fluidLayout]', e),
            hasAnonPages = checkForAnonPages(idVal);

        var fn = {
            id: function () {
                if (checkForPagesById(idVal) && page.id != idVal) {
                    showInformation("URL Already Exists",
                        "A page with entered URL already exists. Please select a different URL");
                    id.val(page.id);
                } else {
                    page.id = idVal;
                    if (landing.is(":checked")) {
                        dashboard.landing = idVal;
                    }
                }
            },
            title: function () {
                if (checkForPagesByTitle(titleVal) && page.title.toUpperCase() != titleVal.toUpperCase()) {
                    showInformation("Title Already Exists",
                        "A page with same title already exists. Please enter a different title");
                    title.val(page.title);
                    titleVal = page.title;
                } else {
                    page.title = titleVal;
                }
            },
            landing: function () {
                if (landing.is(':checked')) {
                    if (hasAnonPages && !page.isanon) {
                        landing.prop("checked", false);
                        showInformation("Cannot Select This Page As Landing",
                            "Please add an anonymous view to this page before select it as the landing page");
                    } else {
                        dashboard.landing = idVal;
                    }
                }
            },
            anon: function () {
                if (anon.is(':checked')) {
                    if (checkForAnonLandingPage(dashboard.landing) || dashboard.landing == idVal) {
                        ues.global.dbType = ANONYMOUS_DASHBOARD_VIEW;
                        dashboard.isanon = true;
                        page.isanon = true;

                        // create the template if there is no content create before
                        page.layout.content.anon = page.layout.content.anon || page.layout.content.loggedIn;
                        $('#designer-view-mode li[data-view-mode=anon]').removeClass('hide');
                        $('#designer-view-mode li[data-view-mode=anon] a').click();
                    } else {
                        $(anon).prop("checked", false);
                        showInformation("Cannot Make This Page Anonymous",
                            "Please add an anonymous view to the landing page in order to make this page anonymous");
                    }
                } else {
                    if (hasAnonPages && dashboard.landing == idVal) {
                        $(anon).prop("checked", true);
                        showInformation("Cannot Remove The Anonymous View",
                            "Cannot remove the anonymous view of landing page when there are pages with anonymous views");
                    } else {
                        page.isanon = false;

                        // Check if the dashboard is no longer anonymous.
                        if (!checkWhetherDashboardIsAnon()) {
                            dashboard.isanon = false;
                            ues.global.dbType = DEFAULT_DASHBOARD_VIEW;
                        }

                        // the anon layout should not be deleted since the gadgets in this layout is already there in the content
                        $('#designer-view-mode li[data-view-mode=anon]').addClass("hide");
                        page.content.anon = {};
                    }
                }
            },
            fluidLayout: function () {
                page.layout.fluidLayout = fluidLayout.is(':checked');
            }
        };

        if (typeof fn[e.context.name] === 'function') {
            fn[e.context.name]();
            updatePagesList();
            saveDashboard();
        } else {
            console.error('Unable to find the property.')
        }

        return true;
    };

    /**
     * Sanitize the given event's key code.
     * @param {Object} element
     * @param {Object} event
     * @param {String} regEx
     * @return {boolean}
     * @private
     */
    var sanitizeOnKeyPress = function (element, event, regEx) {
        var code;
        if (event.keyCode) {
            code = event.keyCode;
        } else if (event.which) {
            code = event.which;
        }

        var character = String.fromCharCode(code);
        if (character.match(regEx) && code != 8 && code != 46) {
            return false;
        } else {
            return !($.trim($(element).val()) == '' && character.match(/[\s]/gim));
        }
    };

    /**
     * Save page options of the component
     * @param {Object} sandbox  Sandbox element
     * @param {Object} options  Options object
     * @return {null}
     * @private
     */
    var saveOptions = function (sandbox, options) {
        $('.ues-options input', sandbox).each(function () {
            var el = $(this);
            var type = el.attr('type');
            var name = el.attr('name');
            if (type === 'text') {
                options[name] = el.val();
                return;
            }
            if (type === 'checkbox') {
                options[name] = el.is(':checked');
            }
            if (type === 'enum') {
                options[name] = el.val();
                return;
            }
        });
        $('.ues-options select', sandbox).each(function () {
            var el = $(this);
            options[el.attr('name')] = el.val();
        });
        $('.ues-options textarea', sandbox).each(function () {
            var el = $(this);
            options[el.attr('name')] = el.val();
        });
    };

    /**
     * Save settings of the component
     * @param {Object} sandbox  Sandbox element
     * @param {Object} settings Settings object
     * @return {null}
     * @private
     */
    var saveSettings = function (sandbox, settings) {
        $('.ues-settings input', sandbox).each(function () {
            var el = $(this);
            var type = el.attr('type');
            var name = el.attr('name');
            if (type === 'text') {
                settings[name] = el.val();
                return;
            }
            if (type === 'checkbox') {
                settings[name] = el.is(':checked');
            }
        });
    };

    /**
     * Save styles of the component
     * @param {Object} sandbox  Sandbox element
     * @param {Object} styles   Styles object
     * @return {null}
     * @private
     */
    var saveStyles = function (sandbox, styles, id) {
        $('.ues-styles input', sandbox).each(function () {
            var el = $(this);
            var type = el.attr('type');
            var name = el.attr('name');
            if (type === 'text' && name != 'title') {
                styles[name] = el.val();
                return;
            }
            if (type === 'checkbox') {
                styles[name] = el.is(':checked');
            }
        });
        styles.titlePosition = $('.ues-styles .ues-title-position', sandbox).val();
        var compLocale = findComponent(id).content.locale_titles || {};
        compLocale[lang] = $('.ues-styles .ues-localized-title', sandbox).val();
    };

    /**
     * Save notifiers of the component
     * @param {Object} sandbox  Sandbox element
     * @param notifiers         Notifiers object
     * @return {null}
     * @private
     */
    var saveNotifiers = function (sandbox, notifiers) {
        $('.ues-notifiers .notifier', sandbox).each(function () {
            var el = $(this);
            var from = el.data('from');
            var event = el.data('event');
            var listener = el.closest('.listener').data('event');
            var events = notifiers[listener] || (notifiers[listener] = []);
            if (!el.is(':checked')) {
                return;
            }
            events.push({
                from: from,
                event: event
            });
        });
    };

    /**
     * Holds the store paging history for infinite scroll
     * @type {{}}
     * @private
     */
    var pagingHistory = {};

    /**
     * Check whether given category is already existing or not
     * @param {Object} categories   All the categories
     * @param {String} category     Category to be checked
     * @return {Boolean}
     * @private
     */
    var checkForExistingCategory = function (categories, category) {
        return categories[category] ? true : false;
    };

    /**
     * Filter the component By Categories
     * @param {Object} data
     * @return {Object}
     * @private
     */
    var filterComponentByCategories = function (data) {
        var componentsWithCategories = {};
        var categories = {};
        for (var i = 0; i < data.length; i++) {
            var category = data[i].category ? data[i].category : nonCategoryKeyWord;
            if (checkForExistingCategory(categories, category)) {
                componentsWithCategories[category].components.push(data[i]);
            } else {
                var newCategory = {
                    components: [],
                    category: category
                };

                categories[category] = category;
                newCategory.components.push(data[i]);
                componentsWithCategories[category] = newCategory;
            }
        }
        return componentsWithCategories;
    };

    /**
     * Sort the Filtered component to show the non categorized components first
     * @param {String} nonCategoryString
     * @param {String} componentsWithCategories
     * @return {Object}
     * @private
     */
    var sortComponentsByCategory = function (nonCategoryString, componentsWithCategories) {
        var sortedList = {};
        sortedList[nonCategoryString] = componentsWithCategories[nonCategoryString];
        for (var i in componentsWithCategories) {
            if (i != nonCategoryString) {
                sortedList[i] = componentsWithCategories[i];
            }
        }
        return sortedList;
    };

    /**
     * Loads given type of assets matching the query
     * @param {String} type
     * @param {String} query
     * @return {null}
     * @private
     */
    var loadAssets = function (type, query) {
        var paging = pagingHistory[type] || (pagingHistory[type] = {
                start: 0,
                count: COMPONENTS_PAGE_SIZE
            });
        var buildPaging = function (paging, query) {
            if (paging.query === query) {
                return;
            }
            paging.end = false;
            paging.query = query;
            paging.start = 0;
            paging.count = COMPONENTS_PAGE_SIZE;
        };
        buildPaging(paging, query);
        if (paging.loading) {
            return;
        }
        if (paging.end) {
            return;
        }
        paging.loading = true;
        ues.store.assets(type, paging, function (err, data) {
            paging.loading = false;
            if (err) {
                console.log(err);
                return;
            }

            var assets = $('.ues-store-assets').find('.ues-thumbnails');
            var fresh = !paging.start;
            var assetz = storeCache[type];
            storeCache[type] = assetz.concat(data);
            paging.start += COMPONENTS_PAGE_SIZE;
            paging.end = !data.length;
            if (!fresh) {

                assets.append(componentsListHbs({
                    type: type,
                    assets: data,
                    lang: lang
                }));
                initNanoScroller();
                return;
            }
            if (data.length) {

                assets.html(componentsListHbs({
                    type: type,
                    assets: sortComponentsByCategory(nonCategoryKeyWord, filterComponentByCategories(data)),
                    lang: lang
                }));
                initNanoScroller();
                return;
            }
            assets.html(noComponentsHbs());
        });
    };

    /**
     * Initializes the components
     * @return {null}
     * @private
     */
    var initComponents = function () {

        $('#sidebarNavSettings .ues-thumbnails').on('mouseenter', '.ues-thumbnail', function () {
            $(this).draggable({
                cancel: false,
                appendTo: 'body',
                helper: 'clone'
            });
        }).on('mouseleave', '.ues-thumbnail', function () {
            $(this).draggable('destroy');
        });
    };

    /**
     * Initializes the designer
     * @return {null}
     * @private
     */
    var initDesigner = function () {

        $('#designer-view-mode').on('click', 'li', function () {
            var currentPageType = pageType;
            var mode = $(this).data('view-mode');
            if (mode === 'default') {
                pageType = DEFAULT_DASHBOARD_VIEW;
                ues.global.type = DEFAULT_DASHBOARD_VIEW;
            } else {
                pageType = ANONYMOUS_DASHBOARD_VIEW;
                ues.global.type = ANONYMOUS_DASHBOARD_VIEW;
                ues.global.anon = true;
            }

            switchPage(getPageId(), currentPageType);
        });
    };

    /**
     * Load layouts for workspace
     * @returns {null}
     * @private
     * */
    var loadLayouts = function () {
        ues.store.assets('layout', {
            start: 0,
            count: 20
        }, function (err, data) {
            storeCache.layout = data;
            $('#ues-page-layouts').html(layoutsListHbs(data));
            initNanoScroller();
        });
    };

    /**
     * Initialize the layout for the workspace
     * @returns {null}
     * @private
     * */
    var initLayoutWorkspace = function () {

        $('#ues-page-layouts').on('click', '.thumbnail', function (e) {
            e.preventDefault();
            var options = pageOptions();

            createPage(options, $(this).data('id'), function (err) {

                // reload pages list
                updatePagesList();

                // hide the sidebar
                $('#sidebarNavFileExplorer button[rel="createPage"]').click();
                // open page options
                $('#ues-dashboard-pages .ues-page-list-heading[data-id="' + options.id + '"]').click();

            });
        });
        loadLayouts();
    };

    /**
     * Initializes the store
     * @return {null}
     * @private
     */
    var initStore = function () {
        loadAssets('gadget');

        // initialize search options in gadgets sidebar
        $('.ues-store-assets .ues-search-box input[type=text]').on('keypress', function (e) {
            if (e.which !== 13) {
                return;
            }
            e.preventDefault();

            var query = $(this).val();
            loadAssets('gadget', query);
        });

        $('.ues-store-assets .ues-search-box button').on('click', function () {
            var query = $(this).closest('.ues-search-box').find('input[type=text]').val();
            loadAssets('gadget', query);
        });
    };

    /**
     * Initialize the designer menu
     * @return {null}
     * @private
     */
    var initDesignerMenu = function () {
        // menu functions
        $('.ues-context-menu')
            .on('click', '.ues-dashboard-preview', function () {
                // Preview the dashboard
                previewDashboard(page);
            })
            .on('click', '.ues-copy', function (e) {
                // reset the dashboard
                e.preventDefault();
                var that = $(this);
                showConfirm('Resetting the page',
                    'This will remove all the customization added to the dashboard. Do you want to continue?',
                    function () {
                        window.open(that.attr('href'), "_self");
                    });
            });

        // page header functions
        $('.page-header')
            .on('click', '.ues-switch-page-prev, .ues-switch-page-next, .ues-refresh-page', function () {
                // navigate/refresh pages
                var pid = $(this).attr('data-page-id');

                ues.global.isSwitchToNewPage = true;
                switchPage(pid, pageType);
                ues.global.isSwitchToNewPage = false;
            })
            .on('click', '.ues-delete-page', function () {

                // delete dashboard page
                var pid = $(this).attr('data-page-id');

                showConfirm('Deleting the page',
                    'This will remove the page and all its content. Do you want to continue?',
                    function () {
                        removePage(pid, DEFAULT_DASHBOARD_VIEW, function (err) {
                            var pages = dashboard.pages;

                            updatePagesList(pages);

                            // if the landing page was deleted, make the first page to be the landing page
                            if (dashboard.pages.length) {
                                if (pid == dashboard.landing) {
                                    dashboard.landing = pages[0].id;
                                }
                            } else {
                                dashboard.landing = null;

                                // hide the sidebar if it is open
                                if ($('#left-sidebar').hasClass('toggled')) {
                                    $('#btn-pages-sidebar').click();
                                }
                            }


                            // save the dashboard
                            saveDashboard();
                            renderPage(dashboard.landing);
                        });
                    });
            });

        // dashboard pages list functions
        var pagesMenu = $("#ues-dashboard-pages");
        // load page properties
        pagesMenu.on("click", '.ues-page-list-heading', function (e) {

            var pid = $(this).data('id')

            // do not re-render if the user clicks on the current page name
            if (pid != page.id) {
                ues.global.isSwitchToNewPage = true;
                switchPage(pid, pageType);
                ues.global.isSwitchToNewPage = false;
            }

            // render page properties
            pagesMenu.find('.ues-page-properties#pages' + pid).html(pageOptionsHbs({
                id: page.id,
                title: page.title,
                landing: (dashboard.landing == page.id),
                isanon: page.isanon,
                isUserCustom: dashboard.isUserCustom,
                fluidLayout: page.layout.fluidLayout || false
            })).on('change', 'input', function () {
                if (updatePageProperties($(this).closest('.ues-page-properties'))) {
                    switchPage(page.id, pageType);
                }
            });

            // bind event handlers for page properties input fields
            // sanitize the page title property
            $("#page-title").on("keypress", function (e) {
                return sanitizeOnKeyPress(this, e, /[^a-z0-9-\s]/gim);
            });

            // sanitize the page URL property
            $("#page-url").on("keypress", function (e) {
                return sanitizeOnKeyPress(this, e, /[^a-z0-9-\s]/gim);
            }).on("keyup", function (e) {
                var sanitizedInput = $(this).val().replace(/[^\w]/g, '-').toLowerCase();
                $(this).val(sanitizedInput);
            });
        });

        var pageSearchFunction = function (searchQuery) {
            var pages = dashboard.pages;
            var resultPages = [];
            if (searchQuery) {
                for (var i = 0; i < pages.length; i++) {
                    if (pages[i].title.toLowerCase().indexOf(searchQuery.toLowerCase()) >= 0) {
                        resultPages.push(pages[i]);
                    }
                }
                updatePagesList(page, resultPages, dashboard.landing);
            } else {
                updatePagesList();
            }
        }

        $('#sidebarNavFileExplorer .ues-search-box input[type=text]').on('keypress', function (e) {
            if (e.which !== 13) {
                return;
            }
            e.preventDefault();
            pageSearchFunction($(this).val());
        });

        $('#sidebarNavFileExplorer .ues-search-box button').on('click', function () {
            var query = $(this).closest('.ues-search-box').find('input[type=text]').val();
            pageSearchFunction(query);
        });
    };

    /**
     * Register the "set_pref" rpc function. When user set the user preferences using
     * pref.set() method this will be executed.
     * @return {null}
     * @private
     */
    var registerRpc = function () {
        gadgets.rpc.register('set_pref', function (token, name, value) {

            //Store the gadget id in a variable
            var id = this.f.split("-")[2];

            var pages = dashboard.pages;
            var numberOfPages = pages.length;
            for (var i = 0; i < numberOfPages; i++) {
                var pageContent = pages[i].content.default;
                var zones = Object.keys(pageContent);
                var numberOfZones = zones.length;
                for (var j = 0; j < numberOfZones; j++) {
                    var zone = zones[j];
                    var gadgets = pageContent[zone];
                    var numberOfGadgets = gadgets.length;
                    for (var k = 0; k < numberOfGadgets; k++) {
                        var gadget = gadgets[k];
                        if (gadgets[k].id == id) {
                            var gadgetOption = gadget.content.options;
                            gadgetOption[name].value = value;
                        }
                    }
                }
            }

            saveDashboard();
        });
    };

    /**
     * Initialized adding block function
     * @return {null}
     */
    var initAddBlock = function () {

        var dummySizeChanged = function () {
            var dummy = $('.ues-dummy-gadget'),
                unitSize = parseInt(dummy.data('unit-size'));

            dummy.css({
                width: unitSize * parseInt($('#block-width').val()),
                height: unitSize * parseInt($('#block-height').val())
            });
        }

        // redraw the grid when changing the width/height values
        $('#block-height, #block-width')
            .on('change', dummySizeChanged)
            .on('keyup', dummySizeChanged)
            .on('blur', dummySizeChanged);

        // add block handler
        $('#ues-add-block-btn').on('click', function () {

            var width = $('#block-width').val() || 0,
                height = $('#block-height').val() || 0,
                id = guid();

            if (width == 0 || height == 0) {
                return;
            }

            getGridstack().add_widget($(newBlockHbs({id: id})), 0, 0, width, height);
            $('.ues-component-box#' + id).html(componentBoxContentHbs())

            updateLayout();
            listenLayout();
        });

        $('.ues-dummy-gadget').resizable({
            grid: 18,
            containment: '.ues-block-container',
            resize: function (e, ui) {

                var height = $(this).height() / 18,
                    width = $(this).width() / 18;

                $('#block-width').val(width);
                $('#block-height').val(height);
            }
        });

        $('#block-width').val($('.ues-dummy-gadget').width() / 18);
        $('#block-height').val($('.ues-dummy-gadget').height() / 18);
    };

    /**
     * Initializes the UI
     * @return {null}
     * @private
     */
    var initUI = function () {
        registerRpc();
        initDesignerMenu();
        initLayoutWorkspace();
        initDesigner();
        initStore();
        initComponentToolbar();
        initComponents();
        initAddBlock();

        if (ues.global.dashboard.isEditorEnable && ues.global.dashboard.isUserCustom) {
            showInformation("Received Edit Permission",
                "You have given edit permission for this dashboard. Please reset the dashboard to receive the permission.");
        }
    };

    /**
     * Initializes the layout listeners
     * @return {null}
     * @private
     */
    var listenLayout = function () {
        $('.gadgets-grid').find('.grid-stack-item:not([data-banner=true]) .ues-component-box').droppable({
            hoverClass: 'ui-state-hover',
            drop: function (event, ui) {

                var id = ui.helper.data('id'),
                    type = ui.helper.data('type');

                if (!hasComponents($(this))) {
                    createComponent($(this), findStoreCache(type, id));
                }
            }
        });
    };

    /**
     * Check whether the container has any components
     * @param {Object} container
     * @returns {boolean}
     * @private
     */
    var hasComponents = function (container) {
        return (container.find('.ues-component .ues-component-body div').length > 0);
    };

    /**
     * Update the page list as changes happened to the pages
     * @param {Object} current
     * @param {Object} pages
     * @param {String} landing
     * @return {null}
     * @private
     * */
    var updatePagesList = function (current, pages, landing) {

        current = current || page;

        $('#ues-dashboard-pages').html(pagesListHbs({
            current: current,
            pages: pages || dashboard.pages,
            home: landing || dashboard.landing,
        }));

        $('#ues-dashboard-pages #pagesButton' + current.id).click();
    };

    /**
     * creates a page with the given options
     * @param {Object} options  Page options
     * @param {String} lid      Layout ID
     * @param {function} done   Callback function
     * @return {null}
     * @private
     */
    var createPage = function (options, lid, done) {

        var layout = findStoreCache('layout', lid);
        $.get(resolveURI(layout.url), function (data) {
            var id = options.id;
            var page = {
                id: id,
                title: options.title,
                layout: {
                    content: {
                        loggedIn: JSON.parse(data),
                    },
                    fluidLayout: false
                },
                isanon: false,
                content: {
                    default: {},
                    anon: {}
                }
            };
            dashboard.landing = dashboard.landing || id;
            dashboard.isanon = dashboard.isanon ? dashboard.isanon : false;
            dashboard.pages.push(page);

            saveDashboard();
            if (ues.global.page) {
                currentPage(findPage(dashboard, ues.global.page.id));
                switchPage(id, pageType);
                done();
            } else {
                renderPage(id, done);
            }
        }, 'html');
    };

    /**
     * Returns the current page
     * @param {Object} p    Page object
     * @returns {Object}
     * @private
     */
    var currentPage = function (p) {
        return page = (ues.global.page = p);
    };

    /**
     * Switches the given page
     * @param {String} pid      Page ID
     * @param {String} pageType Type of the page
     * @return {null}
     * @private
     */
    var switchPage = function (pid, pageType) {
        if (!page) {
            return renderPage(pid);
        }
        destroyPage(page, pageType, function (err) {
            if (err) {
                throw err;
            }
            renderPage(pid);
        });
    };

    /**
     * Update the layout after modification
     * @return {null}
     */
    var updateLayout = function () {

        // extract the layout from the designer and save it
        var res = _.map($('.grid-stack .grid-stack-item:visible'), function (el) {
            el = $(el);
            var node = el.data('_gridstack_node');

            if (node) {
                return {
                    id: el.attr('data-id'),
                    x: node.x,
                    y: node.y,
                    width: node.width,
                    height: node.height,
                    banner: el.attr('data-banner') == 'true'
                };
            }
        });

        var serializedGrid = [];
        for (i = 0; i < res.length; i++) {
            if (res[i]) {
                serializedGrid.push(res[i]);
            }
        }

        var json = {blocks: serializedGrid},
            id, i;

        // find the current page index
        for (i = 0; i < ues.global.dashboard.pages.length; i++) {
            if (ues.global.dashboard.pages[i].id === page.id) {
                id = i;
            }
        }

        if (typeof id === 'undefined') {
            throw 'specified page : ' + page.id + ' cannot be found';
        }

        if (pageType === ANONYMOUS_DASHBOARD_VIEW) {
            ues.global.dashboard.pages[id].layout.content.anon = json;
        } else {
            ues.global.dashboard.pages[id].layout.content.loggedIn = json;
        }

        saveDashboard();
    };

    /**
     * Generate GUID
     * @returns {String}
     * @private
     */
    function guid() {
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }

        return s4() + s4() + s4() + s4() + s4() + s4() + s4() + s4();
    }

    /**
     * Show page layouts selection pane
     * @returns {null}
     * @private
     */
    var showCreatePage = function () {

        // if the left panel is closed, click on the pages button
        if (!$('#left-sidebar').hasClass('toggled')) {
            $('#btn-pages-sidebar').click()
        }

        // if the select layout is closed, click on the create page button
        if (!$('#left-sidebar-sub').hasClass('toggled')) {
            $('#left-sidebar button[rel=createPage]').click();
        }
    }

    /**
     * Renders the given page in the designer view
     * @param {String} pid      Page ID
     * @param {function} done   Callback function
     * @return {null}
     * @private
     */
    var renderPage = function (pid, done) {

        // if no pages found, display a message
        if (!dashboard.pages.length) {

            $('#ues-dashboard-preview-link').hide();

            $('.gadgets-grid')
                .html(noPagesHbs())
                .find('#btn-add-page-empty').on('click', function () {
                    showCreatePage();
                });

            $('.page-header .page-actions').hide();
            $('#btn-sidebar-layouts, #btn-sidebar-gadgets').hide();

            showCreatePage();
            return;
        }

        $('#ues-dashboard-preview-link').show();
        $('.gadgets-grid').html('');
        $('.page-header .page-actions').show();
        $('#btn-sidebar-layouts, #btn-sidebar-gadgets').show();

        currentPage(findPage(dashboard, pid));
        if (!page) {
            throw 'specified page : ' + pid + ' cannot be found';
        }

        pageType = pageType || DEFAULT_DASHBOARD_VIEW;

        var anonToggle = $('#designer-view-mode li[data-view-mode=anon]');
        if (page.isanon && !dashboard.isUserCustom) {
            anonToggle.removeClass('hide');
        } else {
            anonToggle.addClass('hide');
        }

        // if the current page doesn't have a anon view defined, render the default view
        if (!page.isanon) {
            pageType = DEFAULT_DASHBOARD_VIEW;
            ues.global.type = DEFAULT_DASHBOARD_VIEW;
            $('#designer-view-mode li').removeClass('active');
            $('#designer-view-mode li[data-view-mode=default]').addClass('active');
        }

        // render page header
        var currentPageIndex = 0;
        for (; currentPageIndex < dashboard.pages.length && dashboard.pages[currentPageIndex].id != pid; currentPageIndex++);

        var hasPrevPage = currentPageIndex > 0,
            hasNextPage = currentPageIndex < dashboard.pages.length - 1;

        $('.page-header').html(headerContent = designerHeadingHbs({
            id: page.id,
            title: page.title,
            pageNumber: currentPageIndex + 1,
            totalPages: dashboard.pages.length,
            prev: {
                available: hasPrevPage,
                id: (hasPrevPage ? dashboard.pages[currentPageIndex - 1].id : '')
            },
            next: {
                available: hasNextPage,
                id: (hasNextPage ? dashboard.pages[currentPageIndex + 1].id : '')
            }
        }));

        ues.dashboards.render($('.gadgets-grid'), dashboard, pid, pageType, function (err) {

            $('.gadgets-grid').find('.ues-component').each(function () {
                var id = $(this).attr('id');
                renderComponentToolbar(findComponent(id));
            });
            listenLayout();

            $('.grid-stack').gridstack({
                width: 12,
                animate: true,
                cell_height: 150,
                vertical_margin: 30,
                handle: '.ues-component-heading, .ues-component-heading .ues-component-title',
            }).on('dragstop', function (e, ui) {

                updateLayout();

            }).on('resizestart', function (e, ui) {

                // hide the component content on start resizing the component
                var container = $(ui.element).find('.ues-component');
                if (container) {
                    container.find('.ues-component-body').hide();
                }

            }).on('resizestop', function (e, ui) {

                // re-render component on stop resizing the component
                var container = $(ui.element).find('.ues-component');
                if (container) {
                    
                    var gsItem = container.closest('.grid-stack-item'),
                        node = gsItem.data('_gridstack_node'), 
                        gsHeight = node ? node.height : parseInt(gsItem.attr('data-gs-height')), 
                        height = (gsHeight * 150) + ((gsHeight - 1) * 30);
                    
                    container.closest('.ues-component-box').attr('data-height', height);
                    
                    container.find('.ues-component-body').show();
                    if (container.attr('id')) {
                        updateComponent(container.attr('id'));
                    }
                }

                updateLayout();

            });

            $('.gadgets-grid [data-banner=true] .ues-component-body').addClass('ues-banner-placeholder');

            if (!done) {
                return;
            }

            done(err);
        }, true);

        updatePagesList();
        initBanner();
    };

    /**
     * Check for the autogenerated name to stop repeating the same name
     * @param {String} prefix
     * @param {Number} pid
     * @return {Number}
     * @private
     * */
    var checkForExistingPageNames = function (prefix, pid) {
        var i,
            pages = dashboard.pages,
            length = pages.length,
            page = prefix + pid;

        for (i = 0; i < length; i++) {
            if (pages[i].id === page) {
                pid++;
                page = prefix + pid;
                return checkForExistingPageNames(prefix, pid);
            }
        }

        return pid;
    };

    /**
     * Build up the page options for the given type of page
     * @param {String} type
     * @returns {{id: string, title: string}}
     * @private
     */
    var pageOptions = function (type) {
        var pages = dashboard.pages;
        if (type === 'landing' || !pages.length) {
            return {
                id: 'landing',
                title: 'Home'
            };
        }
        if (type === 'login') {
            return {
                id: 'login',
                title: 'Login'
            };
        }

        var pid = 0;
        var prefix = 'page';
        var titlePrefix = 'Page ';

        pid = checkForExistingPageNames(prefix, pid);

        return {
            id: prefix + pid,
            title: titlePrefix + pid
        };
    };

    /**
     * Initializes the dashboard
     * @param {Object} db   Dashboard object
     * @param {String} page Page ID
     * @returns {null}
     * @private
     */
    var initDashboard = function (db, page) {

        dashboard = (ues.global.dashboard = db);
        var pages = dashboard.pages;

        if (pages.length > 0) {
            renderPage(page || db.landing || pages[0].id);
        } else {
            renderPage(null)
        }
    };

    /**
     * Load the content within the banner placeholder
     * @returns {null}
     */
    var loadBanner = function () {

        ues.global.dashboard.banner = ues.global.dashboard.banner || {
                globalBannerExists: false,
                customBannerExists: false
            };

        var $placeholder = $('.ues-banner-placeholder'),
            customDashboard = ues.global.dashboard.isUserCustom || false,
            banner = ues.global.dashboard.banner;

        var bannerExists = banner.globalBannerExists || banner.customBannerExists;

        // create the view model to be passed to handlebar
        var data = {
            isAdd: !bannerExists && !banner.cropMode,
            isEdit: bannerExists && !banner.cropMode,
            isCrop: banner.cropMode,
            isCustomBanner: customDashboard && banner.customBannerExists,
            showRemove: (customDashboard && banner.customBannerExists) || !customDashboard,
            isEditable: (pageType == DEFAULT_DASHBOARD_VIEW)
        };
        $placeholder.html(bannerHbs(data));

        // display the image
        var img = $placeholder.find('.banner-image');
        if (bannerExists) {
            img.css('background-image', "url('" + img.data('src') + '?rand=' + Math.floor(Math.random() * 100000) + "')").show();
        } else {
            img.hide();
        }
    };

    /**
     * Change event handler for the banner file control
     * @param {Object} e
     * @returns {null}
     */
    var bannerChanged = function (e) {

        var file = e.target.files[0];
        if (file.size == 0) {
            return;
        }

        if (!new RegExp('^image/', 'i').test(file.type)) {
            return;
        }

        // since a valid image is selected, render the banner in crop mode
        ues.global.dashboard.banner.cropMode = true;
        loadBanner();
        $('.ues-banner-placeholder button').prop('disabled', true);
        $('.ues-dashboard-banner-loading').show();

        var $placeholder = $('.ues-banner-placeholder'),
            srcCanvas = document.getElementById('src-canvas'),
            $srcCanvas = $(srcCanvas),
            img = new Image(),
            width = $placeholder.width(),
            height = $placeholder.height();

        // remove previous cropper bindings to the canvas (this will remove all the created controls as well)
        $srcCanvas.cropper('destroy');

        // draw the selected image in the source canvas and initialize cropping
        var srcCtx = srcCanvas.getContext('2d'),
            objectUrl = URL.createObjectURL(file);

        img.onload = function () {
            // draw the uploaded image on the canvas
            srcCanvas.width = img.width;
            srcCanvas.height = img.height;

            srcCtx.drawImage(img, 0, 0);

            // bind the cropper
            $srcCanvas.cropper({
                aspectRatio: width / height,
                autoCropArea: 1,
                strict: true,
                guides: true,
                highlight: true,
                dragCrop: false,
                cropBoxMovable: false,
                cropBoxResizable: true,

                crop: function (e) {
                    // draw the cropped image part in the dest. canvas and get the base64 encoded string
                    var cropData = $srcCanvas.cropper('getData'),
                        destCanvas = document.getElementById('dest-canvas'),
                        destCtx = destCanvas.getContext('2d');

                    destCanvas.width = width;
                    destCanvas.height = height;

                    destCtx.drawImage(img, Math.max(cropData.x, 0), Math.max(cropData.y, 0), cropData.width, cropData.height, 0, 0, destCanvas.width, destCanvas.height);

                    var dataUrl = destCanvas.toDataURL('image/jpeg');
                    $('#banner-data').val(dataUrl);
                }
            });

            $('.ues-banner-placeholder button').prop('disabled', false);
            $('.ues-dashboard-banner-loading').hide();
        };
        img.src = objectUrl;
    };

    /**
     * Initialize the banner
     * @returns {null}
     */
    var initBanner = function () {

        loadBanner();

        // bind a handler to the change event of the file element (removing the handler initially to avoid multiple binding to the same handler)
        var fileBanner = document.getElementById('file-banner');
        fileBanner.removeEventListener('change', bannerChanged);
        fileBanner.addEventListener('change', bannerChanged, false);

        // event handler for the banner edit button
        $('.ues-banner-placeholder').on('click', '#btn-edit-banner', function (e) {
            $('#file-banner').val('').click();
        });

        // event handler for the banner save button
        $('.ues-banner-placeholder').on('click', '#btn-save-banner', function (e) {
            var $form = $('#ues-dashboard-upload-banner-form');
            var cropData = $form.find('#banner-data').val();

            $.ajax({
                url: $form.attr('action'),
                type: $form.attr('method'),
                data: {'data': cropData},
            }).success(function (d) {

                if (ues.global.dashboard.isUserCustom) {
                    ues.global.dashboard.banner.customBannerExists = true;
                } else {
                    ues.global.dashboard.banner.globalBannerExists = true;
                }
                ues.global.dashboard.banner.cropMode = false;
                loadBanner();
            });
        });

        // event handler for the banner cancel button
        $('.ues-banner-placeholder').on('click', '#btn-cancel-banner', function (e) {
            ues.global.dashboard.banner.cropMode = false;
            $('#file-banner').val('');
            loadBanner();
        });

        // event handler for the banner remove button
        $('.ues-banner-placeholder').on('click', '#btn-remove-banner', function (e) {
            var $form = $('#ues-dashboard-upload-banner-form');

            if (ues.global.dashboard.isUserCustom && !ues.global.dashboard.banner.customBannerExists) {

                // in order to remove the global banner from a personalized dashboard, we need to save an empty resource.
                $.ajax({
                    url: $form.attr('action'),
                    type: $form.attr('method'),
                    data: {data: ''},
                }).success(function (d) {

                    // we need to suppress the global banner when removing the global banner from a custom dashboard.
                    // therefore the following flag is set to false forcefully.
                    ues.global.dashboard.banner.globalBannerExists = false;

                    if (ues.global.dashboard.isUserCustom) {
                        ues.global.dashboard.banner.customBannerExists = false;
                    }

                    ues.global.dashboard.banner.cropMode = false;

                    loadBanner();
                });

            } else {
                // remove the banner
                $.ajax({
                    url: $form.attr('action'),
                    type: 'DELETE',
                    dataType: 'json'
                }).success(function (d) {

                    if (ues.global.dashboard.isUserCustom) {
                        ues.global.dashboard.banner.globalBannerExists = d.globalBannerExists;
                        ues.global.dashboard.banner.customBannerExists = false;
                    } else {
                        ues.global.dashboard.banner.globalBannerExists = false;
                    }
                    ues.global.dashboard.banner.cropMode = false;
                    loadBanner();
                });
            }
        });
    };

    initUI();
    initDashboard(ues.global.dashboard, ues.global.page);

    ues.dashboards.save = saveDashboard;
});

// Initialize nano scrollers
var nanoScrollerSelector = $(".nano");
nanoScrollerSelector.nanoScroller();

/**
 * Update sidebar
 * @param {String} view     Selector of the sidebar pane
 * @param {Object} button   Event source
 */
function updateSidebarNav(view, button) {
    var target = $(button).data('target');
    $(view).show();
    $(view).siblings().hide();

    if ($(view).find('button[data-target=#left-sidebar-sub]').length == 0) {
        $('#left-sidebar-sub').hide();
    }
    else {
        $('#left-sidebar-sub').show();
    }

    nanoScrollerSelector[0].nanoscroller.reset();
}

/**
 * Update the UI when closing the right sidebar
 * @param {Object} button       Event source
 */
function updateSidebarOptions(button) {

    var target = $(button).data('target');

    $('.gadget').removeClass('active');
    setTimeout(function () {
        if ($(target).hasClass('toggled')) {
            $(button).closest('.gadget').addClass('active');
            $(button).closest('.gadget').removeClass('deactive');
            $('.gadget:not(.active)').addClass('deactive');
        }
        else {
            $('.gadget').removeClass('active').removeClass('deactive');
        }
    }, 5);
}

/**
 * Toggle caret when sidebar toggles
 * @param {*} e
 */
function toggleCaret(e) {
    $(e.target)
        .prev('.panel-heading')
        .toggleClass('dropup dropdown');
    nanoScrollerSelector[0].nanoscroller.reset();
}

$('.sidebar-wrapper').on('hidden.bs.collapse', toggleCaret);
$('.sidebar-wrapper').on('shown.bs.collapse', toggleCaret);

$('#left-sidebar').on('hidden.sidebar', function (e) {
    $(e.target).find('button[data-target=#left-sidebar-sub]').removeClass('active').attr('aria-expanded', 'false');
    $.sidebar_toggle('hide', '#left-sidebar-sub', '.page-content-wrapper');
});

$('#gridGuideToggle').change(function () {
    $('body').toggleClass('grid-on');
});

$('.gadgets-grid').on({
    mouseenter: function () {
        toggleHeading($(this), true);
    },
    mouseleave: function () {
        toggleHeading($(this), false);
    }
}, '.ues-component');

/**
 * Toggle gadget heading when no heading is activated
 * @param {Object} source       Event source
 * @param {Boolean} show        Flag
 */
function toggleHeading(source, show) {
    if (source.hasClass('ues-no-heading')) {
        var heading = source.find('.ues-component-heading');
        if (show) {
            heading.slideDown();
        } else {
            heading.slideUp();
        }
    }
}




// Enforce min/max values of number fields
$('input[type=number]').on('change', function () {

    var input = $(this),
        max = input.attr('max'),
        min = input.attr('min');

    if (input.val().trim() == '') {
        return;
    }

    var value = parseInt(input.val());

    if (max !== '' && !isNaN(max) && value > parseInt(max)) {
        input.val(max);
    }

    if (min !== '' && !isNaN(min) && value < parseInt(min)) {
        input.val(min);
    }

}).on('blur', function () {

    var input = $(this);
    if (input.val() == '' && input.attr('min')) {
        input.val(input.attr('min'));
    }
});
