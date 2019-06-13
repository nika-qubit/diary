# Objective
* Gain empathy of Dataflow users
* Learn gaps between expectation and user experience

# General Guide
* Pick a scenario where a batch/stream pipeline could help solve a real world problem.
* Plan 1 or 2 iterations of work to create a pipeline with Beam SDK and run on Dataflow.
* Document how the study is carried out as a diary.
* Document project and usage of the project.
* File tickets for issues and suggest improvements of Beam and Dataflow.

A planning tool (e.g., Planr, Buganizer or JIRA), a version control tool (e.g., Piper or Git) and
detailed project/user/study documentation (e.g., in Google Docs or Github pages) are recommended.
When encountering issues with Beam and Dataflow, if an external customer, feel free to reach out to
the communities; if an internal developer, reach out to the dataflow team.

The outcome should be improving Beam/Dataflow user experience and having an evolving community (both
external and internal) to help with it.

# Quickstart with Example - Cryptocurrency Research
Research the correlation between crypto currencies' price change and their news sentiment.
* [Gradle Instructions](https://kevingg.github.io/diary/docs/gradle)\
  This example uses Gradle to build and run. Build scan has been setup to always publish so you can
  check the build performance from the short link after each build. The document describes basic
  Gradle installation and usage.
* [Collect Crypto Currency Price and News Data](https://kevingg.github.io/diary/crypto-ingest/)\
  This example uses python script to collect data from the Internet. There is no guarantee that such
  script won't stop working in the future. The document describes basic Python3, pip3 and virtualenv
  usage with dependencies the script needs.
* [Build a Batch Pipeline to Analyze the Correlation](https://kevingg.github.io/diary/crypto-research-batch/)\
  This example uses Java Beam SDK with direct runner and dataflow runner. The document describes
  minimum dependencies needed for pipeline development and execution with supplemental instructions
  of running pipeline with Gradle, authenticating with service account and exploring pipeline 
  options.
* [Diary](https://kevingg.github.io/diary/docs/crypto-diary)\
  The document shows an example of carrying out and recording diary study for future contributors.
 