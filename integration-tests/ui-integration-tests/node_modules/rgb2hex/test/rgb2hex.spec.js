var rgb2hex = require('../index'),
    should = require('should'),
    typeofErrorMessage = 'color has to be type of `string`',
    invalidErrorMessage = function(input) { return 'given color (' + input + ') isn\'t a valid rgb or rgba color'; };

describe('rgb2hex should', function() {

    describe('throw an error if input is not typeof string', function() {

        it('[Object] {color: \'something\'}', function() {
            var input = {color: 'something'};
            rgb2hex.bind(null,input).should.throw(typeofErrorMessage);
        });

        it('[Function] function(){}', function() {
            var input = function(){};
            rgb2hex.bind(null,input).should.throw(typeofErrorMessage);
        });

        it('[Number] 231', function() {
            var input = 231;
            rgb2hex.bind(null,input).should.throw(typeofErrorMessage);
        });

    });

    describe('throw an error if input is invalid', function() {

        it('notacolor', function() {
            var input = 'notacolor';
            rgb2hex.bind(null,input).should.throw(invalidErrorMessage(input));
        });

        it('rgba(100, 100)', function() {
            var input = 'rgb(100, 100)';
            rgb2hex.bind(null,input).should.throw(invalidErrorMessage(input));
        });

        it('rgba(100, 10a0, 200, 300)', function() {
            var input = 'rgba(100, 10a0, 200, 300)';
            rgb2hex.bind(null,input).should.throw(invalidErrorMessage(input));
        });

        it('rgba(23, 54, 4, -.33)', function() {
            var input = 'rgba(23, 54, 4, -.33)';
            rgb2hex.bind(null,input).should.throw(invalidErrorMessage(input));
        });

    });

    it('return input if it is already a hex color', function() {
        var input = '#ffffff',
            parsedValue = rgb2hex(input);

        parsedValue.should.have.property('hex');
        parsedValue.should.have.property('alpha');
        parsedValue.hex.should.be.type('string');
        parsedValue.hex.should.eql('#ffffff');
        parsedValue.alpha.should.be.type('number');
        parsedValue.alpha.should.eql(1);
    });

    describe('parse input properly', function() {

        it('converting rgb(210,43,2525)', function() {
            var input = 'rgb(210,43,255)',
                parsedValue = rgb2hex(input);

            parsedValue.should.have.property('hex');
            parsedValue.should.have.property('alpha');
            parsedValue.hex.should.be.type('string');
            parsedValue.hex.should.eql('#d22bff');
            parsedValue.alpha.should.be.type('number');
            parsedValue.alpha.should.eql(1);
        });

        it('converting rgba(12,173,22,.67)', function() {
            var input = 'rgba(12,173,22,.67)',
                parsedValue = rgb2hex(input);

            parsedValue.should.have.property('hex');
            parsedValue.should.have.property('alpha');
            parsedValue.hex.should.be.type('string');
            parsedValue.hex.should.eql('#0cad16');
            parsedValue.alpha.should.be.type('number');
            parsedValue.alpha.should.eql(0.67);
        });

        it('by limiting alpha value to 1', function() {
            var input = 'rgba(12,173,22,12312.67)';
            rgb2hex(input).alpha.should.not.be.above(1);
        })

    })

    describe('not care about', function() {

        it('rgb or rgba prefix', function() {
            var rgb = 'rgb(0, 0, 0)',
                rgba = 'rgba(0, 0, 0)';

            rgb2hex(rgb).hex.should.be.equal(rgb2hex(rgba).hex);
        });

        it('spaces between color numbers', function() {
            var rgbWithSpaces = 'rgb(0, 0, 0)',
                rgbWithoutSpaces = 'rgba(0,0,0)';

            rgb2hex(rgbWithSpaces).hex.should.be.equal(rgb2hex(rgbWithoutSpaces).hex);
        });

        it('if alpha value starts with `.` or with `0`', function() {
            var rgbaWithDot = 'rgba(213,12,4,.45)',
                rgbWitahoutDot = 'rgba(213,12,4,0.45)';

            rgb2hex(rgbaWithDot).alpha.should.be.equal(rgb2hex(rgbWitahoutDot).alpha);
        });

    })

})