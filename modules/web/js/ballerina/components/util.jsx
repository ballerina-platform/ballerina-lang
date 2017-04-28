import path from 'path';
import $ from 'jquery';

let instance = null;

// require all react images
function requireAll(requireContext) {
    let components = {};
    requireContext.keys().map((item) => {
        var module = requireContext(item);
        if (module) {
            components[path.basename(item, '.svg')] = module;
        }
    });
    return components;
}

const images = requireAll(require.context('images', true, /\.svg$/));

class Utils {
    constructor() {
        if (!instance) {
            instance = this;
        }
    }

    static getSVGIcon(iconName) {
        return images[iconName];
    }

    /**
     * Get given text's pixel width.
     * @param {string} text
     * @return {int} width
     * */
    static getTextWidth(text) {
        $('<div class="text-width-calc-container" style="visibility:hidden;">' +
            '<svg id="mySvg" xmlns="http://www.w3.org/2000/svg" version="1.1">' +
            '<text class="text-width-calc-text" x="20" y="40">' + text + '</text>' +
            '</svg></div>').appendTo($('body'));

        let element = document.getElementsByClassName("text-width-calc-text")[0];
        return element.getComputedTextLength();
    }
}

export default instance || Utils;
