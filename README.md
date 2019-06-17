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

# Quickstart with Example Scenario - Cryptocurrency Research
Research the correlation between crypto currencies' price change and their news sentiment.
* [Gradle Instructions](https://kevingg.github.io/diary/docs/gradle)
  This example uses Gradle to build and run. Build scan has been setup to always publish so you can
  check the build performance from the short link after each build. The document describes basic
  Gradle installation and usage.
* [Collect Crypto Currency Price and News Data](https://kevingg.github.io/diary/crypto-ingest/)
  This example uses python script to collect data from the Internet. There is no guarantee that such
  script won't stop working in the future. The document describes basic Python3, pip3 and virtualenv
  usage with dependencies the script needs.
* [Build a Batch Pipeline to Analyze the Correlation](https://kevingg.github.io/diary/crypto-research-batch/)
  This example uses Java Beam SDK with direct runner and dataflow runner. The document describes
  minimum dependencies needed for pipeline development and execution with supplemental instructions
  of running pipeline with Gradle, authenticating with service account and exploring pipeline 
  options.
* [Diary](https://kevingg.github.io/diary/docs/crypto-diary)
  The document shows an example of carrying out and recording diary study for future contributors.
* [Share a Tensorflow Saved Model Trained from Pipelined Data](https://kevingg.github.io/diary/crypto-tensorflow-saved-model)
  This is an example tensorflow model to solve the regression problem, how does news sentiment 
  affect cryptocurrency price change? The input includes coin, date, sentiment values and output
  predicts the percentile price change.

# Available Scenarios to Take
Pick a scenario, indicate whether you are building a batch or stream pipeline, put your github
usernames under the corresponding columns. Or add new scenarios if you want to take on other real
world problems. If you only solve a concrete portion of an open problem, add some note.

Note to focus on experiencing Beam and Dataflow to achieve the [Objective](#objective) instead of
spending too many efforts obtaining data. You can choose the topic you are familiar with, interested
in or most efficient and productive working on.

ID   | Scenario | Batch  | Stream | Note
:--- | :------- | :---:  | :----: | ---:
1    | Crypto currency price and news sentiment correlation | KevinGG
2    | GW2 profitable trading post items
3    | Genetic data analytics
4    | IoT data analytics
5    | SETI - help find aliens

* Scenario #1 is already explained as the quickstart example in this repo.
* Scenario #2. GW2 is an MMORPG. It has a trading post where bidding of in-game items are happening.
It has well-defined [REST API](https://wiki.guildwars2.com/wiki/API:2) that serves data without
charging fees. There is also example application [gw2efficiency](https://gw2efficiency.com/)
utilizing such API. With a 15% processing fee when selling items, based on trading history (might
need stream pipeline to support), figure out what items in trading post provide the best profit.
E.g., some items are seasonal, at one point in a year they reach lowest selling price while reaching
highest selling price at another point in a year. Note: you have to always consider whether an item
is actively listed/purchased in the trading post, the margin of a sale, the throughput of sales and
many other aspects to solve the problem.
* Scenario #3. There are many data listed by [nature genetic database](https://www.nature.com/subjects/genetic-databases).
An example of [Atlas of RNA sequencing profiles for normal human tissues](https://www.nature.com/articles/s41597-019-0043-4)
uses raw data from a [NCBI GEO dataset](https://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=GSE120795).
* Scenario #4. Some [Emerging Use Cases for IoT Data Analytics](https://www.softwareadvice.com/resources/iot-data-analytics-use-cases/).
* Scenario #5. The raw data can be found on this [page](https://seti.berkeley.edu/listen/data.html).
Data was provided by [Berkeley SETI](https://seti.berkeley.edu/). All SETI researches can be found
on [SETI institute](https://www.seti.org/).
