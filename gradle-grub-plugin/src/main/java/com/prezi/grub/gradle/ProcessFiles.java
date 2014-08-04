package com.prezi.grub.gradle;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.prezi.grub.gradle.internal.GlobsUtils;
import groovy.text.GStringTemplateEngine;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessFiles extends DefaultTask {
	public static final String VERBATIM_FILE = ".grubverbatim";

	private File templateDirectory;
	private File targetDirectory;
	private File verbatimFile;

	@InputDirectory
	public File getTemplateDirectory() {
		return templateDirectory;
	}

	public void setTemplateDirectory(Object templateDirectory) {
		this.templateDirectory = getProject().file(templateDirectory);
	}

	@SuppressWarnings("UnusedDeclaration")
	public void templateDirectory(Object templateDirectory) {
		setTemplateDirectory(templateDirectory);
	}

	@SuppressWarnings("UnusedDeclaration")
	public void template(Object templateDirectory) {
		setTemplateDirectory(templateDirectory);
	}

	@OutputDirectory
	public File getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(Object targetDirectory) {
		this.targetDirectory = getProject().file(targetDirectory);
	}

	@SuppressWarnings("UnusedDeclaration")
	public void targetDirectory(Object targetDirectory) {
		setTargetDirectory(targetDirectory);
	}

	@SuppressWarnings("UnusedDeclaration")
	public void target(Object targetDirectory) {
		setTargetDirectory(targetDirectory);
	}

	@InputFile
	@Optional
	public File getVerbatimFile() {
		File result = verbatimFile;
		if (result == null) {
			result = new File(getTemplateDirectory(), VERBATIM_FILE);
		}
		if (!result.exists()) {
			result = null;
		}
		return result;
	}

	public void setVerbatimFile(Object verbatimFile) {
		this.verbatimFile = getProject().file(verbatimFile);
	}

	@SuppressWarnings("UnusedDeclaration")
	public void verbatimFile(Object verbatimFile) {
		setVerbatimFile(verbatimFile);
	}

	@SuppressWarnings("UnusedDeclaration")
	public void verbatim(Object verbatimFile) {
		setVerbatimFile(verbatimFile);
	}

	private final GStringTemplateEngine engine = new GStringTemplateEngine();

	@TaskAction
	public void copy() throws Exception {
		if (getTemplateDirectory().exists()) {
			copyChildren(getTemplateDirectory(), getTargetDirectory());
		}
	}

	private void copyChildren(File templateDir, File targetDir) throws Exception {
		Collection<Pattern> verbatimPatterns = GlobsUtils.readGlobs(getVerbatimFile());

		getLogger().info("Processing {} to {}", templateDir, targetDir);
		String[] names = templateDir.list();
		if (names == null) {
			throw new IOException("Could not read files from " + templateDir);
		}
		String templateDirPath = templateDir.getAbsolutePath().replaceAll("\\\\", "/") + "/";
		for (String childName : names) {
			if (childName.equals(".") || childName.equals("..") || childName.equals(VERBATIM_FILE)) {
				continue;
			}
			File templateChild = new File(templateDir, childName);
			String targetName = engine.createTemplate(childName).make(createBindings()).toString();
			File targetChild = new File(targetDir, targetName);

			if (templateChild.isDirectory()) {
				FileUtils.forceMkdir(targetChild);
				copyChildren(templateChild, targetChild);
			} else if (templateChild.isFile()) {
				boolean verbatim = isVerbatim(verbatimPatterns, templateDirPath + childName);
				if (verbatim) {
					Files.copy(templateChild, targetChild);
				} else {
					Reader reader = Files.asCharSource(templateChild, Charsets.UTF_8).openStream();
					try {
						Writer writer = Files.asCharSink(targetChild, Charsets.UTF_8).openStream();
						try {
							engine.createTemplate(reader).make(createBindings()).writeTo(writer);
						} finally {
							writer.close();
						}
					} finally {
						reader.close();
					}
				}
			}
		}
	}

	private static boolean isVerbatim(Collection<Pattern> verbatimPatterns, String childPath) {
		for (Pattern verbatimPattern : verbatimPatterns) {
			Matcher verbatimMatcher = verbatimPattern.matcher(childPath);
			if (verbatimMatcher.matches()) {
				return true;
			}
		}
		return false;
	}

	private Map<String, Object> createBindings() {
		Map<String, Object> binding = Maps.newHashMap();
		binding.put("project", getProject());
		for (Map.Entry<String, ?> entry : getProject().getProperties().entrySet()) {
			binding.put(entry.getKey(), entry.getValue());
		}
		return binding;
	}
}
