## Changelog ##

### 2.4.4

#### Fixed

* trigger is sometimes called on undefined objects because of typecheck on null. thanks @andreasrosdal

### 2.4.3

#### Changed 

* The inline style causes a Content Security Policy violation if style-src 'unsafe-inline' is not defined in the policy. [PR 498](https://github.com/swisnl/jQuery-contextMenu/pull/498) thanks @StealthDuck

* Removed GPL license from the comment in the plugin. Was already removed everywhere else. Only MIT applies now.

#### Added

* Added SauceLabs tests for common browsers.

### 2.4.2 ###
 
### Fixed

* Focus not set on content editable element when right clicking the second time ([Issue #482](https://github.com/swisnl/jQuery-contextMenu/issues/482)) 

* `selectableSubMenu` broke disabling click menu (fixes ([Issue #493](https://github.com/swisnl/jQuery-contextMenu/issues/493))

### 2.4.1 ###

#### Fixed

* Quick fix for error in visible check ([Issue #484](https://github.com/swisnl/jQuery-contextMenu/issues/484))

#### Updated

* Tweaked positioning of submenu ([Issue #387](https://github.com/swisnl/jQuery-contextMenu/issues/387))

### 2.4.0 ###

#### Added

* Selectable Sub Menus ([Issue #483](https://github.com/swisnl/jQuery-contextMenu/issues/483)) thanks @zyuhel

#### Fixed

* The contextmenu shows even if all items are set to visible:false ([Issue #473](https://github.com/swisnlhttps://github.com/swisnl/jQuery-contextMenu/issues/482/jQuery-contextMenu/issues/473)) 

#### Documentation

* Update documentation to include demo for async promise fixes ([Issue #470](https://github.com/swisnl/jQuery-contextMenu/issues/470))

### 2.3.0 ###

#### Added

* Asynchronous promise support for submenu's ([Issue #429](https://github.com/swisnl/jQuery-contextMenu/issues/429)) thanks @Ruud-cb for the hard work.
* Include dist and src in package.json to easily use SCSS files ([PR #467](https://github.com/swisnl/jQuery-contextMenu/pull/467))  thanks @RoachMech


#### Fixed

* Font family when using font awesome ([Issue #433](https://github.com/swisnl/jQuery-contextMenu/issues/433))
* Add check for `opt.$menu` is null when handling callbacks. ([Issue #462](https://github.com/swisnl/jQuery-contextMenu/issues/462)) thanks @andreasrosdal

#### Changed

* Make `<input>` and `<select>` tags xhtml compatible ([Issue #451](https://github.com/swisnl/jQuery-contextMenu/issues/451)) thanks @andreasplesch
* Update jQuery UI position to 1.12.1

#### Documentation

* Fix demo for custom-command. ([Issue #294](https://github.com/swisnl/jQuery-contextMenu/issues/294))
* Fix broken link and demo title ([Issue #458](https://github.com/swisnl/jQuery-contextMenu/issues/458))

### 2.2.4 ###

#### Fixed

* Error on try to recreate menu after destroy ([Issue #397](https://github.com/swisnl/jQuery-contextMenu/issues/397))


### 2.2.3 ###

#### Fixed

* Callbacks are now called from the scope of the menu the item is in (like a submenu). For now they overwrite root callbacks only if the item is not in a submenu, this so the callbacks are always correct. Unfortunately this will also mean the callbacks option is still not complete if you use the same key for an item in any place. Cant fix that easily. Issue #413.

### 2.2.1 ###

#### Added

* Alias for 'cm_seperator' type: 'cm_separator' (thanks @nelson6e65)

#### Changed

* Removed old integration tests, framework on which they were built is abandoned.
* Enable jQuery 3 tests in TravisCI

#### Fixed

* jQuery 3 support was fixed again, was a result of jQuery UI (Fixes #407)
* Add checks for null before using opt.$menu and root.$menu. Fixes #352 (thanks @andreasrosdal)
* Small fix for color or ``input`` option on hover

#### Documentation

* Documentation added for cm_seperator (thanks @nelson6e65)
* Fix typo in items options documentation (thanks @nelson6e65)
* Fix typo in animation: fadeOut (thanks @avi-meslati-sp)
* Fix typo in docs code: `show` is used twice (thanks @kgeorgiou)
* Fix in async documentation.

### 2.2.0 ###

#### Added
* Add option to show item title as HTML (thanks @brassard)
* Full Font Awesome support 

#### Changed 
* Use relative units for css fixes ([Issue #386](https://github.com/swisnl/jQuery-contextMenu/issues/386)) (thanks @RoachMech)
* Change unicode characters in CSS to readable strings.
* Improved item styles (thanks @anseki)

#### Fixed 
* Force woff2 font creation for Windows some machines.
* Fix so that disabled items can't get focus anymore (thanks @anseki)
* Fix so menu size is calculated better no items will take up 2 lines again (thanks @anseki)
* Fix bower.json (thanks @nelson6e65)
* Fix typo in documentation for "position" and "build" callback (thanks @mmcev106)

### 2.1.1 ###

* Fixed a problem when using the open function with custom arguments (thanks @RareDevil)
* `width` is increased when repoening menu. Fixed by using outerwidth to calculate width. Fixes #360 (thanks @anseki)
* Submenus are not collapsed when the menu is closed fixes #358 (thanks @anseki)
* Small delay in checking for autohide to fix missing the menu by a pixel or two. Fixes #347 (thanks @Risord)
* Check if an item is not hidden in any way when scrolling through items with the keyboard. Fixes #348
* Change links and base url of documentation to https as mentioned by @OmgImAlexis in PR#345

### 2.1.0 ###

* Added support for providing a function as zIndex value in options object (thanks @eivindga)
* Fixed a switch to use the correct type for separators (thanks @RareDevil)
* Fixed the problem with submenus size wrongly ([Issue #308](https://github.com/swisnl/jQuery-contextMenu/issues/308)) (thanks @RareDevil)
* Incorrect entry on package.json ([Issue #336](https://github.com/swisnl/jQuery-contextMenu/issues/336)) (thanks @Dijir)
* Gray out disabled icons as well as text ([Issue #337](https://github.com/swisnl/jQuery-contextMenu/issues/337)) (thanks @r02b)
* Optimized generated CSS so that ``context-menu-icon`` class can be used to overwrite icon CSS.
* Positioning of contextmenu when using appendTo (thanks @mrMarco)
* Check to see if target have a higher zIndex than the contextmenu in the key event handler (thanks @RareDevil)

###  2.0.1 (December 3rd 2015) ###

* Remove executable bit from jquery.contextMenu.js (thanks @jacknagel)
* Fixed a problem there was when using a function for icons (thanks @RareDevil)
* Fixed a problem where submenus resized wrong (thanks @RareDevil)
* Fixed a problem where the contextmenu would open another menu (thanks @RareDevil) - ([Issue #252](https://github.com/swisnl/jQuery-contextMenu/issues/252) and [Issue #293](https://github.com/swisnl/jQuery-contextMenu/issues/293))
* Fixed regression of node name's not being appended to the label of input elements. (thanks @RareDevil)
* Add check that root.$layer exists, to prevent calling hide() on an defined object. (thanks @andreasrosdal)


### 2.0.0 (October 28th 2015) ###

* __This version changes the default names of the icon classes in order to stop CSS conflicts with frameworks which define the class 'icon'.__ In order to keep the icon names the same as before this change you can change the defaults on the classnames for the icons ([docs on classNames option](http://swisnl.github.io/jQuery-contextMenu/docs.html#options-classNames)). The classnames will probably be "context-menu-icon-*" as proposed earlier by @rodneyrehm.
* You can not use SASS to customize your contextmenu. The gulp command build-icons takes all the SVG icons from src/icons and builds them into a font. In order to this we needed to break backwards compatibility. This does mean the new CSS does not have the old .icon class defined which makes it a lot more stable within CSS frameworks. The first revision of the documentation is found [here](documentation/docs/customize.md).
* The 1.x branch will be maintained for a while with bugfixes. But support for 1.x will be dropped in the coming months.
* Reverted the change from 1.7.0: .html() changed back to .text() since it is an security issue (thanks @arai-a)

### 1.10.1 (October 25th 2015) ###

* Added gulp command (integration-test-paths) to change the paths in the integration tests to the correct path after they are overwritten by the documentation generator.
* Make sure the contextmenu is not outside the client area by (thanks to @arai-a)
* Update jQuery dependecy so that it will not result in double installation of jQuery when using npm (thanks to @fredericlb)

### 1.9.1 (October 11th 2015) ###

* Fixed a bug where the classNames options would fail on a submenu.
* New documentation site and generation using [couscous](https://github.com/CouscousPHP/Couscous)

### 1.9.0 (October 1st 2015) ###

* Make classes configurable for those that can easily conflict. See the [docs on classNames option](http://swisnl.github.io/jQuery-contextMenu/docs.html#options-classNames). This also prepares to change classnames to non conflicting defaults so the hassle with frameworks as bootstrap will stop.
* Fix for handling of seperator string. It threw an error on the protected property of String.$node
* Fix for opening the contextmenu at coordinate 0,0 (by [Andreme](https://github.com/andreme))
* Fixed check for jQuery UI ([Issue #182](https://github.com/swisnl/jQuery-contextMenu/issues/182))
* Updated doc for function argument for icon

### 1.8.1 (September 14th 2015) ###

* Updated readme.
* Updated dist files

### 1.8.0 (September 14th 2015) - dist files not updated! ###

* Added dist folder with compiled JS and CSS, added these files to package and bower configuration.
* Fixed doc link for jQuery UI position ([Issue #274](https://github.com/swisnl/jQuery-contextMenu/issues/274))
* Item icon can now be a callback to dynamically decide on icon class. - ([Issue #158](https://github.com/swisnl/jQuery-contextMenu/issues/158), [Issue #129](https://github.com/swisnl/jQuery-contextMenu/issues/129), [Issue #151](https://github.com/swisnl/jQuery-contextMenu/issues/151), [Issue #249](https://github.com/swisnl/jQuery-contextMenu/issues/249))
* Small fix to calculating width and height on screen edges when padding is present.

### 1.7.0 (August 29th 2015) ###

* Touch support optimisations (by kccarter76) 
* changed .text to .html so there are no extra span's fixed - ([Issue #252](https://github.com/swisnl/jQuery-contextMenu/issues/252))
* added visibility callback to item definition
* copy the HTML5 icon attribute when creating from HTML5 elements 
* growing menu when opening multiple times fixed - ([Issue #197](https://github.com/swisnl/jQuery-contextMenu/issues/197))
* fixed failure to run tests

### 1.6.8 (August 18th 2015) ###

* changes for new maintainer

### 1.6.7 (May 21st 2015) ###

* looking for maintainer note
* publish to npm

### 1.6.6 (July 12th 2014) ###

* fixing bower manifest

### 1.6.5 (January 20th 2013) ###

* fixing "opening a second menu can break the layer" - ([Issue #105](https://github.com/swisnl/jQuery-contextMenu/issues/105))

### 1.6.4 (January 19th 2013) ###

* fixing [jQuery plugin manifest](https://github.com/swisnl/jQuery-contextMenu/commit/413b1ecaba0aeb4e50f97cee35f7c367435e7830#commitcomment-2465216), again. yep. I'm that kind of a guy. :(

### 1.6.3 (January 19th 2013) ###

* fixing [jQuery plugin manifest](https://github.com/swisnl/jQuery-contextMenu/commit/413b1ecaba0aeb4e50f97cee35f7c367435e7830#commitcomment-2465216)

### 1.6.2 (January 19th 2013) ###

* fixing "menu won't close" regression introduced by 1.6.1

### 1.6.1 (January 19th 2013) ###

* fixing potential html parsing problem
* upgrading to jQuery UI position v1.10.0
* replaced `CRLF` by `LF` (no idea how this happened in the first place...)
* adding `options.reposition` to dis/allow simply relocating a menu instead of rebuilding it ([Issue #104](https://github.com/swisnl/jQuery-contextMenu/issues/104))

### 1.6.0 (December 29th 2012) ###

* adding [DOM Element bound context menus](http://swisnl.github.io/jQuery-contextMenu/demo/on-dom-element.html) - ([Issue 88](https://github.com/swisnl/jQuery-contextMenu/issues/88))
* adding class `context-menu-active` to define state on active trigger element - ([Issue 92](https://github.com/swisnl/jQuery-contextMenu/issues/92))
* adding [demo for TouchSwipe](http://swisnl.github.io/jQuery-contextMenu/demo/trigger-swipe.html) activation
* adding export of internal functions and event handlers - ([Issue 101](https://github.com/swisnl/jQuery-contextMenu/issues/101))
* fixing key "watch" might translate to Object.prototype.watch in callbacks map - ([Issue 93](https://github.com/swisnl/jQuery-contextMenu/issues/93))
* fixing menu and submenu width calculation - ([Issue 18](https://github.com/swisnl/jQuery-contextMenu/issues/18))
* fixing unused variables - ([Issue 100](https://github.com/swisnl/jQuery-contextMenu/issues/100))
* fixing iOS "click" compatibility problem - ([Issue 83](https://github.com/swisnl/jQuery-contextMenu/issues/83))
* fixing separators to not be clickable - ([Issue 85](https://github.com/swisnl/jQuery-contextMenu/issues/85))
* fixing issues with fixed positioned triggers ([Issue 95](https://github.com/swisnl/jQuery-contextMenu/issues/95))
* fixing word break problem - ([Issue 80](https://github.com/swisnl/jQuery-contextMenu/issues/80))

### 1.5.25 (October 8th 2012) ###

* upgrading to jQuery 1.8.2 ([Issue 78](https://github.com/swisnl/jQuery-contextMenu/issues/78))
* upgrading to jQuery UI position 1.9.0 RC1 ([Issue 78](https://github.com/swisnl/jQuery-contextMenu/issues/78))

### 1.5.24 (August 30th 2012) ###

* adding context menu options to input command events ([Issue 72](https://github.com/swisnl/jQuery-contextMenu/issues/72), dtex)
* code cosmetics for JSLint

### 1.5.23 (August 22nd 2012) ###

* fixing reposition/close issue on scrolled documents ([Issue 69](https://github.com/swisnl/jQuery-contextMenu/issues/69))
* fixing jQuery reference ([Issue 68](https://github.com/swisnl/jQuery-contextMenu/issues/68))

### 1.5.22 (July 16th 2012) ###

* fixing issue with animation and remove on hide (Issue #64)

### 1.5.21 (July 14th 2012) ###

* fixing backdrop would not remove on destroy (Issue #63)

### 1.5.20 (June 26th 2012) ###

Note: git tag of version is `v1.6.20`?!

* fixing backdrop would not position properly in IE6 (Issue #59)
* fixing nested input elements not accessible in Chrome / Safari (Issue #58)

### 1.5.19 ###

Note: git tag of version is missing...?!

* fixing sub-menu positioning when `$.ui.position` is not available (Issue #56)

### 1.5.18 ###

Note: git tag of version is missing...?!

* fixing html5 `<menu>` import (Issue #53)

### 1.5.17 (June 4th 2012) ###

* fixing `options` to default to `options.trigger = "right"`
* fixing variable name typo (Within Issue #51)
* fixing menu not closing while opening other menu (Within Issue #51)
* adding workaround for `contextmenu`-bug in Firefox 12 (Within Issue #51)

### 1.5.16 (May 29th 2012) ###

* added vendor-prefixed user-select to CSS
* fixed issue with z-indexing when `<body>` is used as a trigger (Issue #49)

### 1.5.15 (May 26th 2012) ###

* allowing to directly open another element's menu while a menu is shown (Issue #48)
* fixing autohide option that would not properly hide the menu

### 1.5.14 (May 22nd 2012) ###

* options.build() would break default options (Issue #47)
* $.contextMenu('destroy') would not remove backdrop

### 1.5.13 (May 4th 2012) ###

* exposing $trigger to dynamically built custom menu-item types (Issue #42)
* fixing repositioning of open menu (formerly accidental re-open)
* adding asynchronous example
* dropping ignoreRightClick in favor of proper event-type detection

### 1.5.12 (May 2nd 2012) ###

* prevent invoking callback of first item of a sub-menu when clicking on the sub-menu-item (Issue #41)

### 1.5.11 (April 27th 2012) ###

* providing `opt.$trigger` to show event (Issue #39)

### 1.5.10 (April 21st 2012) ###

* ignoreRightClick would not prevent right click when menu is already open (Issue #38)

### 1.5.9 (March 10th 2012) ###

* If build() did not return any items, an empty menu was shown (Issue #33)

### 1.5.8 (January 28th 2012) ###

* Capturing Page Up and Page Down keys to ignore like space (Issue #30)
* Added Home / End keys to jump to first / last command of menu (Issue #29)
* Bug hitting enter in an &lt;input&gt; would yield an error (Issue #28)

### 1.5.7 (January 21st 2012) ###

* Non-ASCII character in jquery.contextMenu.js caused compatibility issues in Rails (Issue #27)

### 1.5.6 (January 8th 2012) ###

* Bug contextmenu event was not passed to build() callback (Issue #24)
* Bug sub-menu markers would not display properly in Safari and Chrome (Issue #25)

### 1.5.5 (January 6th 2012) ###

* Bug Internet Explorer would not close menu when giving input elements focus (Issue #23)

### 1.5.4 (January 5th 2012) ##

* Bug not set z-index of sub-menus might not overlap the main menu correctly (Issue #22)

### 1.5.3 (January 1st 2012) ###

* Bug `console.log is undefined`

### 1.5.2 (December 25th 2012) ###

* Bug sub-menus would not properly update their disabled states (Issue #16) [again…]
* Bug sub-menus would not properly adjust width accoring to min-width and max-width (Issue #18)

### 1.5.1 (December 18th 2011) ###

* Bug sub-menus would not properly update their disabled states (Issue #16)

### 1.5 (December 13th 2011) ###

* Added [dynamic menu creation](http://swisnl.github.io/jQuery-contextMenu/demo/dynamic-create.html) (Issue #15)

### 1.4.4 (December 12th 2011) ###

* Bug positioning &lt;menu&gt; when trigger element is `position:fixed` (Issue #14)

### 1.4.3 (December 11th 2011) ###

* Bug key handler would caputure all key strokes while menu was visible (essentially disabling F5 and co.)

### 1.4.2 (December 6th 2011) ###

* Bug opt.$trigger was not available to disabled callbacks
* jQuery bumped to 1.7.1

### 1.4.1 (November 9th 2011) ###

* Bug where &lt;menu&gt; imports would not pass action (click event) properly

### 1.4 (November 7th 2011) ###

* Upgraded to jQuery 1.7 (changed dependecy!)
* Added internal events `contextmenu:focus`, `contextmenu:blur` and `contextmenu:hide`
* Added custom &lt;command&gt; types
* Bug where `className` wasn't properly set on &lt;menu&gt;

### 1.3 (September 5th 2011) ###

* Added support for accesskeys
* Bug where two sub-menus could be open simultaneously

### 1.2.2 (August 24th 2011) ###

* Bug in HTML5 import

### 1.2.1 (August 24th 2011) ###

* Bug in HTML5 detection

### 1.2 (August 24th 2011) ###

* Added compatibility to &lt;menuitem&gt; for Firefox 8
* Upgraded to jQuery 1.6.2

### 1.1 (August 11th 2011) ###

* Bug #1 TypeError on HTML5 action passthru
* Bug #2 disbaled callback not invoked properly
* Feature #3 auto-hide option for hover trigger
* Feature #4 option to use a single callback for all commands, rather than registering the same function for each item
* Option to ignore right-click (original "contextmenu" event trigger) for non-right-click triggers

### 1.0 (July 7th 2011) ###

* Initial $.contextMenu handler
