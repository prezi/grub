package com.prezi.grub.config

import spock.lang.Specification

class ConfiguratorTest extends Specification {
	def "loading"() {
		Configuration config = Configurator.loadConfiguration("""
parameters {
	moduleName {
		value { 12 }
	}
}
generate {
	// Make sure we succeed even if this closure doesn't work
	make_sure this_deosnt compile
}
""")
		expect:
		config.resolve(null) == [
		        moduleName: 12
		]
	}
}
