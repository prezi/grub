package com.prezi.grub.gradle;

import groovy.lang.Closure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class GrubPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		GrubConvention convention = new GrubConvention();
		project.getConvention().getPlugins().put("grub", convention);

		ProcessTemplateFiles processDefaultTemplateFiles = project.getTasks().create("processDefaultTemplateFiles", ProcessTemplateFiles.class);
		processDefaultTemplateFiles.setTemplateDirectory(project.file(project.property("template") + "/src/main/grub"));
		processDefaultTemplateFiles.setTargetDirectory(project.file(project.property("target")));

		Task generateTask = project.getTasks().create("generate");
		generateTask.dependsOn(processDefaultTemplateFiles);
	}

	private static class GrubConvention {
		@SuppressWarnings("UnusedDeclaration")
		public void parameters(Closure<?> closure) {
			// Ignore parameters configuration
		}
	}
}
