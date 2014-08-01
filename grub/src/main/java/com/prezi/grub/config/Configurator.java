package com.prezi.grub.config;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.io.IOException;

public class Configurator {
	public static Configuration loadConfiguration(File configFile) throws IOException {
		return loadConfiguration(Files.asCharSource(configFile, Charsets.UTF_8).read());
	}

	public static Configuration loadConfiguration(String configuration) {
		CompilerConfiguration compilerConfig = new CompilerConfiguration();
		compilerConfig.setScriptBaseClass(Configuration.class.getName());

		Binding binding = new Binding();
		binding.setVariable("configuration", new Configuration());
		GroovyShell shell = new GroovyShell(Configurator.class.getClassLoader(), binding, compilerConfig);

		String script = "def closure = { " + configuration + " };"
			+ "closure.setDelegate(configuration);"
			+ "closure.setResolveStrategy(Closure.DELEGATE_FIRST);"
			+ "closure.call(configuration);"
			+ "return configuration;";
		return (Configuration) shell.evaluate(script);
	}
}
