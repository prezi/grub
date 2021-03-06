package com.prezi.grub.gradle.config;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class ParameterContainer extends GroovyObjectSupport {
	private final Map<String, Parameter> parameters = Maps.newLinkedHashMap();
	private Map<String, Object> resolvedValues;

	@SuppressWarnings("UnusedDeclaration")
	public Object methodMissing(String name, Object args) {
		if (parameters.containsKey(name)) {
			throw new IllegalArgumentException("Parameter with name already registered: " + name);
		}
		Closure<?> closure = (Closure<?>) ((Object[]) args)[0];
		Parameter parameter = new Parameter(name);
		parameters.put(name, parameter);
		closure.setDelegate(parameter);
		closure.setResolveStrategy(Closure.DELEGATE_ONLY);
		return closure.call(parameter);
	}

	@SuppressWarnings("UnusedDeclaration")
	public Object propertyMissing(String name) {
		if (resolvedValues.containsKey(name)) {
			return resolvedValues.get(name);
		}
		throw new MissingPropertyException(name, ParameterContainer.class);
	}

	public Map<String, Object> resolve(Reader reader) throws IOException {
		BufferedReader input = new BufferedReader(reader);
		if (resolvedValues == null) {
			resolvedValues = Maps.newLinkedHashMap();
			for (Parameter parameter : parameters.values()) {
				String title = parameter.getTitle();
				if (Strings.isNullOrEmpty(title)) {
					title = parameter.getName();
				}
				String description = parameter.getDescription();
				boolean required = parameter.isRequired();
				Object value = getParameterValue(parameter);
				if (parameter.isPrompt()) {
					StringBuilder prompt = new StringBuilder();
					if (description != null) {
						prompt.append(description).append(System.lineSeparator());
					}
					prompt.append(title);
					if (required) {
						prompt.append(" (required)");
					}
					if (value != null) {
						prompt.append(" [").append(value).append(']');
					}
					prompt.append(": ");

					while (true) {
						System.out.print(prompt);
						System.out.flush();
						String userInput = input.readLine();
						if (Strings.isNullOrEmpty(userInput)) {
							if (required && value == null) {
								System.out.println("Parameter '" + parameter + "' is required.");
								continue;
							}
						} else {
							Class<?> type = parameter.getType();
							if (type == null && value != null) {
								type = value.getClass();
							} else {
								type = String.class;
							}
							value = StringGroovyMethods.asType(userInput, type);
						}
						break;
					}
				}
				resolvedValues.put(parameter.getName(), value);
			}
		}
		return resolvedValues;
	}

	private Object getParameterValue(Parameter parameter) {
		Closure<?> value = parameter.getValue();
		if (value == null) {
			return null;
		}
		Closure<?> clone = (Closure<?>) value.clone();
		clone.setDelegate(this);
		clone.setResolveStrategy(Closure.DELEGATE_ONLY);
		return clone.call();
	}
}
