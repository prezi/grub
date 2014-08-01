package com.prezi.grub.config;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public class ParameterContainer extends GroovyObjectSupport {
	private final Map<String, Parameter> parameters = Maps.newLinkedHashMap();
	private final Map<String, Object> values = Maps.newLinkedHashMap();

	@SuppressWarnings("UnusedDeclaration")
	public Object methodMissing(String name, Object args) {
		if (parameters.containsKey(name)) {
			throw new IllegalArgumentException("Parameter with name already registered: " + name);
		}
		Closure<?> closure = (Closure<?>) ((Object[]) args)[0];
		Parameter parameter = new Parameter(name);
		parameters.put(name, parameter);
		closure.setDelegate(parameter);
		closure.setResolveStrategy(Closure.DELEGATE_FIRST);
		return closure.call(parameter);
	}

	@SuppressWarnings("UnusedDeclaration")
	public Object propertyMissing(String name) {
		if (values.containsKey(name)) {
			return values.get(name);
		}
		throw new MissingPropertyException(name, ParameterContainer.class);
	}

	public Map<String, Object> resolve(BufferedReader input) throws IOException {
		Map<String, Object> resolved = Maps.newLinkedHashMap();
		for (Parameter parameter : parameters.values()) {
			String title = parameter.getTitle();
			if (Strings.isNullOrEmpty(title)) {
				title = parameter.getName();
			}
			String description = parameter.getDescription();
			boolean required = parameter.isRequired();
			Object value = getParameterDefaultValue(parameter);
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
			resolved.put(parameter.getName(), value);
		}
		return resolved;
	}

	private Object getParameterDefaultValue(Parameter parameter) {
		Closure<?> value = parameter.getValue();
		if (value == null) {
			return null;
		}
		Closure<?> clone = (Closure<?>) value.clone();
		clone.setDelegate(parameters);
		clone.setResolveStrategy(Closure.DELEGATE_FIRST);
		return clone.call();
	}
}
