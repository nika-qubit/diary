# Diary Study
* Dataflow EOU team project from an external user's perspective

## Objective
* Gain empathy of Dataflow users
* Learn gaps between expectation and user experience

## Documents (Google Internal Only)
* [Initial Document](https://docs.google.com/document/d/1yC9JE5OAY65BgLv-z6lOAN1tc2gB9Y4v2QgU-X4zyA4/edit?usp=sharing)
* [Diary Study](https://docs.google.com/document/d/1yC9JE5OAY65BgLv-z6lOAN1tc2gB9Y4v2QgU-X4zyA4/edit?usp=sharing)

## Gradle
### [Install Gradle](https://gradle.org/install/)
### Init a new directory with Gradle
Don't do this if you are working on an existing Gradle project.

```
gradle init

```

### Build
If a package/directory/folder contains gradle projects already:

```
 ./gradlew build
```

Note not to use the gradle you have installed in your environment.
You should always use ./gradlew (the Gradle wrapper) to work with existing projects.
This approach isolates your runtime from the repo's configuration and thus other contributors.
