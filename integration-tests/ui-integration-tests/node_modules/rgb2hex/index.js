/**
 * rgb2hex
 *
 * @author Christian Bromann <mail@christian-bromann.com>
 * @description converts rgba color to HEX
 *
 * @param  {String} color  rgb or rgba color
 * @return {Object}        object with hex and alpha value
 */

var rgb2hex = module.exports = function rgb2hex(color) {

    if(typeof color !== 'string') {
        // throw error of input isn't typeof string
        throw new Error('color has to be type of `string`');
    } else if (color.substr(0, 1) === '#') {
        // or return if already rgb color
        return {
            hex: color,
            alpha: 1
        };
    }

    // parse input
    var digits = /(.*?)rgb(a)*\((\d+),(\d+),(\d+)(,[0-9]*\.*[0-9]+)*\)/.exec(color.replace(/\s+/g,''));

    if(!digits) {
        // or throw error if input isn't a valid rgb(a) color
        throw new Error('given color (' + color + ') isn\'t a valid rgb or rgba color');
    }

    var red = parseInt(digits[3]);
    var green = parseInt(digits[4]);
    var blue = parseInt(digits[5]);
    var alpha = digits[6] ? /([0-9\.]+)/.exec(digits[6])[0] : '1';
    var rgb = ((blue | green << 8 | red << 16) | 1 << 24).toString(16).slice(1);

    // parse alpha value into float
    if(alpha.substr(0,1) === '.') {
        alpha = parseFloat('0' + alpha, 10);
    }

    // limit alpha value to 1
    if(alpha > 1) {
        alpha = 1;
    }

    // cut alpha value after 2 digits after comma
    alpha = parseFloat(Math.round(alpha * 100), 10) / 100;

    return {
        hex: digits[1] + '#' + rgb.toString(16),
        alpha: alpha
    }

};