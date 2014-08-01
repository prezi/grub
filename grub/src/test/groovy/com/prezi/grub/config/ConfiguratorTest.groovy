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
""")
		expect:
		config.resolve(null) == [
		        moduleName: 12
		]
	}
}
