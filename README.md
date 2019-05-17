Gradle Plugin for License3J
===========================

This plugin makes it easy, nay, trivial, to generate and distribute
license keys using the open-source license3j library.

There are two plugins in this package:
* org.anarres.license3j.keys - key generation only
* org.anarres.license3j - key generation AND license generation

Caveats
-------

You **must** use the org.anarres.mirrors.license3j fork in order to
have consistent datetime handling. This plugin generates licenses in
the format for that fork, not the mainstream.

org.anarres.license3j.keys
--------------------------

This creates a task `generateLicenseKeys`, which will generate a
public-private key pair in `build/license3j`. No configuration is
required but much is offered.

You should copy the generated keys to a location in your source tree
(or elsewhere) and preserve them, so that you can check them in the
normal license3j manner.

org.anarres.license3j
---------------------

This applies `org.anarres.license3j.keys` and additionally creates
a task `generateLicense`.

```
buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath 'org.anarres.gradle:gradle-license3j-plugin:1.0.2'
	}
}

apply plugin: 'org.anarres.license3j'

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.anarres.mirrors.license3j:license3j:3.1.0.2'
}

generateLicense {
	publicKeyFile rootProject.file('src/license/public.key')
	privateKeyFile rootProject.file('src/license/private.key')

	feature "customerName", project.name;
	feature "my-other-feature", "anything"
	issuedAt java.time.Instant.now()
	expiresAfterNow 30, 'DAYS'
}
```

This will create a `license.dat` as a resource in your built JAR,
which may be loaded using license3j.
