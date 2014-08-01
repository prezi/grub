package com.prezi.grub.config;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;

public class Parameter extends GroovyObjectSupport {
	private final String name;
	private String title;
	private String description;
	private Closure<?> value;
	private Class<?> type;
	private boolean required = true;
	private boolean prompt = true;

	public Parameter(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@SuppressWarnings("UnusedDeclaration")
	public void title(String title) {
		setTitle(title);
	}

	public Closure<?> getValue() {
		return value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@SuppressWarnings("UnusedDeclaration")
	public void description(String description) {
		setDescription(description);
	}

	private void setValueInternal(Closure<?> value, boolean prompt) {
		this.value = value;
		setPrompt(prompt);
	}

	public void setValue(Closure<?> value) {
		setValueInternal(value, false);
	}

	@SuppressWarnings("UnusedDeclaration")
	public void value(Closure<?> value) {
		setValue(value);
	}

	public void setDefaultValue(Closure<?> value) {
		setValueInternal(value, true);
	}

	@SuppressWarnings("UnusedDeclaration")
	public void defaultValue(Closure<?> value) {
		setDefaultValue(value);
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	@SuppressWarnings("UnusedDeclaration")
	public void type(Class<?> type) {
		setType(type);
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	@SuppressWarnings("UnusedDeclaration")
	public void required(boolean required) {
		setRequired(required);
	}

	public boolean isPrompt() {
		return prompt;
	}

	public void setPrompt(boolean prompt) {
		this.prompt = prompt;
	}

	@SuppressWarnings("UnusedDeclaration")
	public void prompt(boolean prompt) {
		setPrompt(prompt);
	}
}
