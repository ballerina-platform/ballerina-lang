import events from 'events'

/**
 * Initialize a new `Dot` matrix test reporter.
 *
 * @param {Runner} runner
 * @api public
 */
class DotReporter extends events.EventEmitter {
    constructor (baseReporter, config, options = {}) {
        super()

        this.baseReporter = baseReporter
        const { epilogue } = this.baseReporter

        this.on('start', () => {
            console.log()
        })

        this.on('test:pending', () => {
            this.printDots('pending')
        })

        this.on('test:pass', () => {
            this.printDots('green')
        })

        this.on('test:fail', () => {
            this.printDots('fail')
        })

        this.on('test:end', () => {
            this.printDots(null)
        })

        this.on('end', () => {
            epilogue.call(baseReporter)
            console.log()
        })
    }

    printDots (status) {
        const { color, symbols } = this.baseReporter

        if (!status) {
            return
        }

        const symbol = status === 'fail' ? 'F' : symbols.dot
        process.stdout.write(color(status, symbol))
    }
}

export default DotReporter
