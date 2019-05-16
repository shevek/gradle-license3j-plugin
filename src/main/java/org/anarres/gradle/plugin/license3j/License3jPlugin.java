/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.gradle.plugin.license3j;

import javax0.license3j.io.IOFormat;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

/**
 *
 * @author shevek
 */
public class License3jPlugin extends License3jKeysPlugin {

    public static final String TASK_GENERATE_LICENSE = "generateLicense";

    @Override
    public void apply(final Project project) {
        super.apply(project);
        GenerateLicenseTask generateLicenseTask
                = project.getTasks().create(TASK_GENERATE_LICENSE, GenerateLicenseTask.class, new Action<GenerateLicenseTask>() {
                    @Override
                    public void execute(GenerateLicenseTask t) {
                        t.setGroup(GROUP);
                        t.setDescription("Generates a fresh license.");
                        t.mustRunAfter(TASK_GENERATE_KEYS);
                    }
                });
        // project.getTasksByName("processResources", true).
        SourceSet sourceSet = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        CopySpec processResourcesTask = (CopySpec) project.getTasks().getByName(sourceSet.getProcessResourcesTaskName());
        processResourcesTask.with(
                project.copySpec().from(generateLicenseTask)
        );
    }
}
