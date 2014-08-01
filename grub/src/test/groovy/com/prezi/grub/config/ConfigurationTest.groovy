package com.prezi.grub.config

import spock.lang.Specification

class ConfigurationTest extends Specification {
	def "simple config"() {
		def config = new Configuration()
		config.parameters {
			moduleName {
				value { 12 }
			}
		}
		def input = new BufferedReader(new StringReader(""))

		expect:
		config.parameters.resolve(input) == [
		        moduleName: 12,
		]
	}

	def "prompt with default value"() {
		def config = new Configuration()
		config.parameters {
			lajos {
				defaultValue { "tibor" }
			}
		}
		def input = new BufferedReader(new StringReader("\n"))

		expect:
		config.parameters.resolve(input) == [
				lajos: "tibor",
		]
	}

	def "prompt overrides default value"() {
		def config = new Configuration()
		config.parameters {
			lajos {
				defaultValue { "tibor" }
			}
		}
		def input = new BufferedReader(new StringReader("geza\n"))

		expect:
		config.parameters.resolve(input) == [
				lajos: "geza",
		]
	}

	def "parameter using other parameter"() {
		def config = new Configuration()
		config.parameters {
			moduleName {
				value { 12 }
			}
			moduleLongName {
				value { moduleName + 5 }
			}
		}
		def input = new BufferedReader(new StringReader(""))

		expect:
		config.parameters.resolve(input) == [
		        moduleName: 12,
				moduleLongName: 17,
		]
	}
}
