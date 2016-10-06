var initTree = function(tree) {
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
