package com.prezi.grub.config;

import groovy.lang.Closure;
import groovy.lang.Script;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public class Configuration extends Script {
	private final ParameterContainer parameters = new ParameterContainer();

	public Configuration() {
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
