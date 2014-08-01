package com.prezi.grub;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import groovy.text.GStringTemplateEngine;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ProcessTemplateFiles extends DefaultTask {
	private File template;
	private File target;

	@InputDirectory
	public File getTemplate() {
		return template;
	}

	public void setTemplate(File template) {
		this.template = template;
	}

	@OutputDirectory
	public File getTarget() {
		return target;
	}

	public void setTarget(File target) {
		this.target = target;
	}

	private final GStringTemplateEngine engine = new GStringTemplateEngine();

	@TaskAction
	public void copy() throws Exception {
		copyChildren(new File(getTemplate(), "src/main/grub"), getTarget());
	}

	private void copyChildren(File templateDir, File targetDir) throws Exception {
		getLogger().info("Processing {} to {}", templateDir, targetDir);
		String[] names = templateDir.list();
		if (names == null) {
			throw new IOException("Could not read files from " + templateDir);
		}
		for (String childName : names) {
			if (childName.equals(".") || childName.equals("..")) {
				continue;
			}
			File templateChild = new File(templateDir, childName);
			String targetName = processName(childName);
			File targetChild = new File(targetDir, targetName);

			if (templateChild.isDirectory()) {
				FileUtils.forceMkdir(targetChild);
				copyChildren(templateChild, targetChild);
			} else if (templateChild.isFile()) {
				Files.copy(templateChild, targetChild);
			}
		}
	}

	private String processName(String name) throws Exception {
		Map<String, Object> binding = Maps.newHashMap();
		binding.put("project", getProject());
		return engine.createTemplate(name).make(binding).toString();
	}
}
