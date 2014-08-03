package com.prezi.grub.gradle.config

import org.gradle.util.ConfigureUtil
import spock.lang.Specification

class ConfigurationTest extends Specification {
	def "simple config"() {
		def params = createParams() {
			moduleName {
				value { 12 }
			}
		}
		def input = new StringReader("")

		expect:
		params.resolve(input) == [
		        moduleName: 12,
		]
	}

	def "prompt with default value"() {
		def params = createParams() {
			lajos {
				defaultValue = "tibor"
			}
		}
		def input = new StringReader("\n")

		expect:
		params.resolve(input) == [
				lajos: "tibor",
		]
	}

	def "prompt overrides default value"() {
		def params = createParams() {
			lajos {
				defaultValue "tibor"
			}
		}
		def input = new StringReader("geza\n")

		expect:
		params.resolve(input) == [
				lajos: "geza",
		]
	}

	def "parameter using other parameter"() {
		def params = createParams() {
			moduleName {
				value { 12 }
			}
			moduleLongName {
				value { moduleName + 5 }
			}
		}
		def input = new StringReader("")

		expect:
		params.resolve(input) == [
		        moduleName: 12,
				moduleLongName: 17,
		]
	}

	private static ParameterContainer createParams(Closure closure) {
		ConfigureUtil.configure(closure, new ParameterContainer())
	}
}
