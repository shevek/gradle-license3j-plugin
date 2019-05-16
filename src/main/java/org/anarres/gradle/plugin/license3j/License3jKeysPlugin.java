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

/**
 *
 * @author shevek
 */
public class License3jKeysPlugin implements Plugin<Project> {

    public static final IOFormat DEFAULT_KEY_FORMAT = IOFormat.BINARY;
    public static final String GROUP = "License";
    public static final String TASK_GENERATE_KEYS = "generateLicenseKeys";

    @Override
    public void apply(final Project project) {
        GenerateLicenseKeysTask generateLicenseKeysTask
                = project.getTasks().create(TASK_GENERATE_KEYS, GenerateLicenseKeysTask.class, new Action<GenerateLicenseKeysTask>() {
                    @Override
                    public void execute(GenerateLicenseKeysTask t) {
                        t.setGroup(GROUP);
                        t.setDescription("Generates a fresh license public/private key pair.");
                    }
                });
    }
}
