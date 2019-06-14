# Gradle

## [Install Gradle](https://gradle.org/install/)
The project uses the newest Gradle version up to date (v5.4.1).
Please check your Gradle version and make sure it's newer when adding new Gradle projects.
If apt/apt-get cannot offer a newer version of Gradle and you cannot use homebrew, please use
 SDKMAN!.
Install SDKMAN!:

```bash
curl -s "https://get.sdkman.io" | bash
```

Then Install Gradle:

```bash
sdk install gradle
```

## Init a new directory with Gradle
Don't do this if you are working on an existing Gradle project.

```bash
gradle init
```

The Gradle wrapper, gradlew, will use the version of your Gradle CLI.

To upgrade wrapper (e.g., to v5.4.1) even when you don't have a gradle installed:

```bash
./gradlew wrapper --gradle-version 5.4.1
```

## Build
If a package/directory/folder contains gradle projects already:

```bash
 ./gradlew build
```

Note not to use the gradle you have installed in your environment.
You should always use ./gradlew (the Gradle wrapper) to work with existing projects.
This approach isolates your runtime from the repo's configuration and thus other contributors.

This diary study has already setup build scan automatically for all sub projects. The scanned result
is always published and can be accessed from the short link you get after each build.

## Dependencies
For users who are only familiar with Maven, finding a corresponding Gradle dependency is fairly
 easy. You can search the artifact you desire in [search.maven.org](https://search.maven.org). You
 can also configure the search engine as *"https://search.maven.org/search?q=%s"* if you use chrome
 browser or browser with similar functionality.
