/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.gradle.plugin.license3j;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Base64;
import java.util.Objects;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax0.license3j.Feature;
import javax0.license3j.License;
import javax0.license3j.crypto.LicenseKeyPair;
import javax0.license3j.io.IOFormat;
import javax0.license3j.io.LicenseWriter;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import static java.time.temporal.ChronoField.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.CheckForNull;
import org.gradle.api.tasks.Internal;

/**
 *
 * @author shevek
 */
public class GenerateLicenseTask extends ConventionTask {

    public static final String PRIVATE_KEY_FILE = "src/license3j/private.key";
    public static final String PUBLIC_KEY_FILE = "src/license3j/public.key";
    private static final DateTimeFormatter DATE_PARSER;

    static {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        // Date part
        builder
                .appendValue(YEAR, 1, 10, SignStyle.NORMAL)
                .appendLiteral('-')
                .appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NORMAL)
                .appendLiteral('-')
                .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NORMAL);
        // Time part
        builder
                .optionalStart().appendLiteral(" ") // [time]
                .appendValue(HOUR_OF_DAY, 1, 2, SignStyle.NORMAL)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 1, 2, SignStyle.NORMAL)
                .optionalStart() // [seconds] Not in Hive
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 1, 2, SignStyle.NORMAL)
                .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true).optionalEnd() // [millis]
                .optionalEnd() // [seconds] Not in Hive
                .optionalEnd(); // [time]
        builder
                .optionalStart().appendZoneOrOffsetId().optionalEnd(); // [zone]
        builder
                .parseDefaulting(HOUR_OF_DAY, HOUR_OF_DAY.range().getMinimum())
                .parseDefaulting(MINUTE_OF_HOUR, MINUTE_OF_HOUR.range().getMinimum())
                .parseDefaulting(SECOND_OF_MINUTE, SECOND_OF_MINUTE.range().getMinimum())
                .parseDefaulting(OFFSET_SECONDS, 0);
        DATE_PARSER = builder.toFormatter().withResolverStyle(ResolverStyle.LENIENT);
    }

    private final Map<String, Feature> features = new LinkedHashMap<>();
    public Object privateKeyFile = PRIVATE_KEY_FILE;
    public Object publicKeyFile = PUBLIC_KEY_FILE;
    public IOFormat keyFormat = License3jPlugin.DEFAULT_KEY_FORMAT;
    public String digest = "SHA-512";
    public Object licenseFile = "build/license3j/license.dat";
    public IOFormat licenseFormat = IOFormat.STRING;
    public boolean verbose = true;

    public GenerateLicenseTask() {
        issuedAt(Instant.now());
    }

    @Input
    public Map<String, Feature> getFeatures() {
        return features;
    }

    @InputFile
    public File getPrivateKeyFile() {
        return getProject().file(privateKeyFile);
    }

    public void setPrivateKeyFile(@Nonnull Object privateKeyFile) {
        this.privateKeyFile = Objects.requireNonNull(privateKeyFile);
    }

    public void privateKeyFile(@Nonnull Object privateKeyFile) {
        setPrivateKeyFile(privateKeyFile);
    }

    @InputFile
    public File getPublicKeyFile() {
        return getProject().file(publicKeyFile);
    }

    public void setPublicKeyFile(@Nonnull Object publicKeyFile) {
        this.publicKeyFile = Objects.requireNonNull(publicKeyFile);
    }

    public void publicKeyFile(@Nonnull Object publicKeyFile) {
        setPublicKeyFile(publicKeyFile);
    }

    @Input
    public IOFormat getKeyFormat() {
        return keyFormat;
    }

    @Input
    public String getDigest() {
        return digest;
    }

    @OutputFile
    public File getLicenseFile() {
        return getProject().file(licenseFile);
    }

    public void setLicenseFile(@Nonnull Object licenseFile) {
        this.licenseFile = Objects.requireNonNull(licenseFile, "License file was null.");
    }

    public void licenseFile(@Nonnull Object licenseFile) {
        setLicenseFile(licenseFile);
    }

    @Input
    public IOFormat getLicenseFormat() {
        return licenseFormat;
    }

    @CheckForNull
    @Internal
    public Instant getLicenseExpiresAt() {
        Feature feature = features.get("expiresAt");
        if (feature == null)
            return null;
        return feature.getInstant();
    }

    @Internal
    public boolean isLicenseExpired() {
        Instant expiresAt = getLicenseExpiresAt();
        if (expiresAt == null)
            return false;
        return Instant.now().isAfter(expiresAt);
    }

    public void feature(@Nonnull Feature feature) {
        features.put(feature.name(), feature);
    }

    public void feature(@Nonnull String text) {
        feature(Feature.Create.from(text));
    }

    public void feature(@Nonnull String name, int value) {
        feature(Feature.Create.intFeature(name, value));
    }

    public void feature(@Nonnull String name, long value) {
        feature(Feature.Create.longFeature(name, value));
    }

    public void feature(@Nonnull String name, float value) {
        feature(Feature.Create.floatFeature(name, value));
    }

    public void feature(@Nonnull String name, double value) {
        feature(Feature.Create.doubleFeature(name, value));
    }

    public void feature(@Nonnull String name, @Nonnull String value) {
        feature(Feature.Create.stringFeature(name, value));
    }

    public void feature(@Nonnull String name, @Nonnull Instant value) {
        feature(Feature.Create.instantFeature(name, value));
    }

    public void feature(@Nonnull String name, @Nonnull UUID value) {
        feature(Feature.Create.uuidFeature(name, value));
    }

    public void licenseId(@Nonnull UUID value) {
        feature("licenseId", value);
    }

    public void licenseId(@Nonnull String value) {
        licenseId(UUID.fromString(value));
    }

    public void issuedAt(@Nonnull Instant value) {
        feature("issuedAt", value);
    }

    public void issuedAt(@Nonnull String value) {
        ZonedDateTime datetime = ZonedDateTime.parse(value, DATE_PARSER);
        issuedAt(datetime.toInstant());
    }

    public void expiryDate(@Nonnull Instant value) {
        feature("expiryDate", value);
    }

    public void expiryDate(@Nonnull String value) {
        ZonedDateTime datetime = ZonedDateTime.parse(value, DATE_PARSER);
        expiryDate(datetime.toInstant());
    }

    /**
     * @see Instant#plus(long, TemporalUnit)
     * @param value
     * @param unit
     */
    public void expiresAfterIssue(@Nonnegative long value, @Nonnull TemporalUnit unit) {
        Instant issuedAt = features.get("issuedAt").getInstant();
        expiryDate(issuedAt.plus(value, unit));
    }

    /**
     * @see ChronoUnit
     * @param value
     * @param unit
     */
    public void expiresAfterIssue(@Nonnegative long value, @Nonnull String unit) {
        expiresAfterIssue(value, Enum.valueOf(ChronoUnit.class, unit));
    }

    /**
     * @see Instant#plus(long, TemporalUnit)
     * @param value
     * @param unit
     */
    public void expiresAfterNow(@Nonnegative long value, @Nonnull TemporalUnit unit) {
        expiryDate(Instant.now().plus(value, unit));
    }

    /**
     * @see ChronoUnit
     * @param value
     * @param unit
     */
    public void expiresAfterNow(@Nonnegative long value, @Nonnull String unit) {
        expiresAfterIssue(value, Enum.valueOf(ChronoUnit.class, unit));
    }

    private void dump(@Nonnull License license) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (LicenseWriter writer = new LicenseWriter(baos)) {
            writer.write(license, IOFormat.STRING);
        }
        getLogger().info("License:\n" + new String(baos.toByteArray(), StandardCharsets.UTF_8));
    }

    @Nonnull
    /* pp */ static byte[] read(@Nonnull File file, IOFormat format) throws IOException {
        byte[] data = Files.readAllBytes(file.toPath());
        if (IOFormat.BASE64.equals(format))
            data = Base64.getDecoder().decode(data);
        return data;
    }

    @Nonnull
    /* pp */ static LicenseKeyPair read(@Nonnull File privateKeyFile, @Nonnull File publicKeyFile, @Nonnull IOFormat keyFormat) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateKey = read(privateKeyFile, keyFormat);
        byte[] publicKey = read(publicKeyFile, keyFormat);
        return LicenseKeyPair.Create.from(privateKey, publicKey);
    }

    @TaskAction
    public void runTask() throws Exception {
        try {
            LicenseKeyPair keyPair = read(getPrivateKeyFile(), getPublicKeyFile(), keyFormat);

            License license = new License();
            for (Feature feature : features.values())
                license.add(feature);
            if (license.getLicenseId() == null)
                license.setLicenseId();
            license.sign(keyPair.getPair().getPrivate(), digest);
            if (verbose)
                dump(license);
            try (LicenseWriter writer = new LicenseWriter(getLicenseFile())) {
                writer.write(license, licenseFormat);
            }
            if (verbose)
                getLogger().info("Generated license id=" + license.getLicenseId() + ", fingerprint=" + license.fingerprint());
            if (!license.isOK(keyPair.getPair().getPublic()))
                throw new IllegalStateException("Failed to generate a valid license.");
        } catch (Exception e) {
            getLogger().error("License generation failed: " + e, e);
            throw e;
        }
    }
}
