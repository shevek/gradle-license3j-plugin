/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.gradle.plugin.license3j;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shevek
 */
@RunWith(Parameterized.class)
public class License3jPluginApplyTest {

    private static final Logger LOG = LoggerFactory.getLogger(License3jPluginApplyTest.class);

    @Nonnull
    private static Object[] A(Object... in) {
        return in;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> parameters() throws Exception {
        return Arrays.asList(
                // A("2.12"),
                // A("2.14"),
                // A("3.0"),
                // A("3.2.1"),
                A("3.4.1"),
                A("4.5.1"),
                A("4.10.3"),
                A("5.6"),
                A("6.1")
        );
    }
    private final String gradleVersion;
    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();
    public File testProjectBuildFile;

    @Before
    public void setUp() throws Exception {
        testProjectBuildFile = testProjectDir.newFile("build.gradle");
    }

    public License3jPluginApplyTest(String gradleVersion) {
        this.gradleVersion = gradleVersion;
    }

    @Test
    public void testApply() throws Exception {
        String text = "plugins { id 'java';\nid 'org.anarres.license3j'; }\n"
                + "generateLicense { privateKeyFile 'build/license3j/private.key'; publicKeyFile 'build/license3j/public.key'; expiryDate '2019-06-07'; }\n";
        Files.write(testProjectBuildFile.toPath(), Collections.singletonList(text));

        GradleRunner runner = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withPluginClasspath()
                .withDebug(true)
                .forwardOutput()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("--stacktrace", License3jPlugin.TASK_GENERATE_KEYS, "processResources");
        LOG.info("Building...\n\n");
        // System.out.println("ClassPath is " + runner.getPluginClasspath());
        BuildResult result = runner.build();
        LOG.info("Output:\n\n" + result.getOutput() + "\n\n");

        for (BuildTask task : result.getTasks())
            LOG.info("Task: " + task + " outcome " + task.getOutcome());

        {
            BuildTask task = result.task(":" + License3jPlugin.TASK_GENERATE_KEYS);
            LOG.debug("Task is " + task);
            assertNotNull("Task was null", task);
            LOG.debug("Task outcome is " + task.getOutcome());
            Files.walk(testProjectDir.getRoot().toPath()).forEach(p -> System.out.println("Path: " + p));
        }

        File file = new File(testProjectDir.getRoot(), "build/resources/main/license.dat");
        assertTrue(file + " exists", file.isFile());
    }
}
