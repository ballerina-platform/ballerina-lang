import sinon from 'sinon'
import events from 'events'
import DotReporter from '../lib/reporter'

class BaseReporter extends events.EventEmitter {
    get symbols () {
        return {}
    }

    get color () {
        return 'some color'
    }
}

var baseReporterMock = new BaseReporter()
var reporter, printDotsMock, epilogueMock

describe('dot reporter', () => {
    beforeEach(() => {
        printDotsMock = sinon.spy()
        epilogueMock = sinon.spy()

        baseReporterMock.epilogue = epilogueMock
        baseReporterMock.printDots = printDotsMock

        reporter = new DotReporter(baseReporterMock)
    })

    it('should print nothing when testrun starts', () => {
        reporter.printDots = sinon.spy()
        reporter.emit('start')
        reporter.printDots.calledWith(null).should.be.true
    })

    it('should print \\n and call baseReporters epilogue when suite ends', () => {
        reporter.emit('end')
        epilogueMock.called.should.be.true()
    })

    it('should print pending dots for pending events', () => {
        reporter.printDots = sinon.spy()
        reporter.emit('test:pending')
        reporter.printDots.calledWith('pending').should.be.true
    })

    it('should print pass dots for passing events', () => {
        reporter.printDots = sinon.spy()
        reporter.emit('test:pass')
        reporter.printDots.calledWith('green').should.be.true
    })

    it('should print fail dots for failing events', () => {
        reporter.printDots = sinon.spy()
        reporter.emit('test:fail')
        reporter.printDots.calledWith('fail').should.be.true
    })

    it('should print pending dots for pending events', () => {
        reporter.printDots = sinon.spy()
        reporter.emit('test:pending')
        reporter.printDots.calledWith('pending').should.be.true
    })

    it('should print nothing when test ends', () => {
        reporter.printDots = sinon.spy()
        reporter.emit('test:end')
        reporter.printDots.calledWith(null).should.be.true
    })

    it('printDots should return nothing if status is falsy', () => {
        (reporter.printDots() === undefined).should.be.true
    })
})
