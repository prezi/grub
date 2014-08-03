package com.prezi.grub.config;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Configuration extends Script {
	private final ParameterContainer parameters = new ParameterContainer();

	public static Configuration loadConfiguration(File configFile) throws IOException {
		return loadConfiguration(Files.asCharSource(configFile, Charsets.UTF_8).read());
	}

	public static Configuration loadConfiguration(String configuration) {
		CompilerConfiguration compilerConfig = new CompilerConfiguration();
		compilerConfig.setScriptBaseClass(Configuration.class.getName());

		Binding binding = new Binding();
		binding.setVariable("configuration", new Configuration());
		GroovyShell shell = new GroovyShell(Configuration.class.getClassLoader(), binding, compilerConfig);

		String script = "def closure = { " + configuration + " };"
			+ "closure.setDelegate(configuration);"
			+ "closure.setResolveStrategy(Closure.DELEGATE_ONLY);"
			+ "closure.call(configuration);"
			+ "return configuration;";
		return (Configuration) shell.evaluate(script);
	}
	@Override
	public Object run() {
		return this;
	}

	public ParameterContainer getParameters() {
		return parameters;
	}

	public void parameters(Closure<?> closure) {
		Closure<?> clone = (Closure<?>) closure.clone();
		clone.setDelegate(parameters);
		clone.setResolveStrategy(Closure.DELEGATE_FIRST);
		clone.call(parameters);
	}

	@SuppressWarnings("UnusedDeclaration")
	public void generate(Closure<?> closure) {
		// Do nothing
	}

	public Map<String, Object> resolve(BufferedReader input) throws IOException {
		return parameters.resolve(input);
	}
}
