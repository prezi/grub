package com.prezi.grub;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class GrubPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		ProcessTemplateFiles processTemplateFiles = project.getTasks().create("processTemplateFiles", ProcessTemplateFiles.class);
		processTemplateFiles.setTemplateDirectory(project.file(project.property("template") + "/src/main/grub"));
		processTemplateFiles.setTargetDirectory(project.file(project.property("target")));
		Task generateTask = project.getTasks().create("generate");
		generateTask.dependsOn(processTemplateFiles);
	}
}
