package com.prezi.grub;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.util.ConfigureUtil;

public class GrubPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		final GrubConvention convention = new GrubConvention();
		project.getConvention().getPlugins().put("grub", convention);

		ProcessTemplateFiles processDefaultTemplateFiles = project.getTasks().create("processDefaultTemplateFiles", ProcessTemplateFiles.class);
		processDefaultTemplateFiles.setTemplateDirectory(project.file(project.property("template") + "/src/main/grub"));
		processDefaultTemplateFiles.setTargetDirectory(project.file(project.property("target")));

		Task generateTask = project.getTasks().create("processTemplate");
		generateTask.dependsOn(processDefaultTemplateFiles);
		
		project.afterEvaluate(new Action<Project>() {
			@Override
			public void execute(Project project) {
				Closure<?> generate = convention.getGenerate();
				if (generate != null) {
					ConfigureUtil.configure(generate, project);
				}
			}
		});
	}

	private static class GrubConvention {
		private Closure<?> generate;

		@SuppressWarnings("UnusedDeclaration")
		public void parameters(Closure<?> closure) {
			// Ignore parameters configuration
		}

		@SuppressWarnings("UnusedDeclaration")
		public void generate(Closure<?> closure) {
			generate = closure;
		}

		public Closure<?> getGenerate() {
			return generate;
		}
	}
}
