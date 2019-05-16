/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.gradle.plugin.license3j;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Collections;
import javax.annotation.Nonnull;
import javax0.license3j.crypto.LicenseKeyPair;
import javax0.license3j.io.IOFormat;
import javax0.license3j.io.KeyPairWriter;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

/**
 *
 * @author shevek
 */
public class GenerateLicenseKeysTask extends ConventionTask {

    public static final String PRIVATE_KEY_FILE = "build/license3j/private.key";
    public static final String PUBLIC_KEY_FILE = "build/license3j/public.key";

    @Input
    public String algorithm = "RSA/ECB/PKCS1Padding";
    @Input
    public int keySize = 2048;
    @Input
    public IOFormat keyFormat = License3jPlugin.DEFAULT_KEY_FORMAT;

    public Object privateKeyFile = PRIVATE_KEY_FILE;
    public Object publicKeyFile = PUBLIC_KEY_FILE;
    public Object javaFile = "build/license3j/LicenseKeys.java";

    @OutputFile
    public File getPrivateKeyFile() {
        return getProject().file(privateKeyFile);
    }

    @OutputFile
    public File getPublicKeyFile() {
        return getProject().file(publicKeyFile);
    }

    @OutputFile
    public File getJavaFile() {
        return getProject().file(javaFile);
    }

    private void appendJava(@Nonnull StringBuilder buf, @Nonnull byte[] data) {
        for (int i = 0; i < data.length; i++) {
            if ((i & 7) == 0)
                buf.append("\n");
            buf.append(String.format("(byte)0x%02X, ", data[i] & 0xff));
        }
    }

    @TaskAction
    public void runTask() throws Exception {
        LicenseKeyPair pair = LicenseKeyPair.Create.from(algorithm, keySize);
        try (KeyPairWriter writer = new KeyPairWriter(getPrivateKeyFile(), getPublicKeyFile())) {
            writer.write(pair, keyFormat);
        }

        byte[] publicKey = pair.getPublic();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(publicKey);
        StringBuilder buf = new StringBuilder("final byte [] DIGEST = new byte[] {");
        appendJava(buf, digest);
        buf.append("\n};\n");

        buf.append("final byte [] KEY = new byte[] {");
        appendJava(buf, publicKey);
        buf.append("\n};\n");

        Files.write(getJavaFile().toPath(), Collections.singletonList(buf));

        // Lint:
        GenerateLicenseTask.read(getPrivateKeyFile(), getPublicKeyFile(), keyFormat);
    }

}
