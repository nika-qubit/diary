# 06/11/2019

## Goal
* Correlate price change and news sentiment

## Diary
Adding ClusterKey as (currency, date) to be used as data grouping key.

**[Suggestion]** 

```
The jobs page orders finished jobs by their end time. So a newer pipeline shows 
above an older one when both are running while below the older one if the newer pipeline completes
first. Thus the position of a job in jobs page might change conditionally. However, there is no
indication in the UI which field is used for ordering. And there is no widget to customize the 
ordering.

From a user’s perspective, when running a cheap prototype pipeline, if something simple
goes wrong, they press Ctrl+C, make some adjustments and run a new job. Now they have 2 jobs 
running. The older pipeline below newer one on jobs page is the failing one. However, the failing 
one might end later than the newer one. Then when you visit the jobs page again when both pipelines
finish, the older pipeline shows above the newer one on jobs page now. The user wonders why the 
failed pipeline is now succeeded and the pipeline with their fix is now failing. Until they read 
through the job id and start time. Like in the 3 fields pointed in 
https://screenshot.googleplex.com/UE09PqpSGNv, 2 of them indicate the second row should be above the
first row.

We should have a simple mechanism to order jobs, or indicate the ordering is by end time, or 
ask+help the user to cancel existing pipeline when they start a new one with similar configuration 
under the same project.
```

Completed pipeline.
Pull Request: https://github.com/KevinGG/diary/commit/6914a3741b4914703da19695e4bc2bdc8785a8cc

Build a static doc hosted on Github:
* To explain the diary study process and how to contribute
* To list 4+ stream and 4+ batch scenarios as future work
* To explain how to file tickets for both internal and external contributors
* The final site will be located at: https://kevingg.github.io/diary/

## Conclusion
### Study Outcome for Beam and Dataflow
* Several suggestions as listed in the diary, including go/dataflow-eou-diary-ningk-initial from
running Beam and Dataflow quickstart. See [Tickets Created](https://www.google.com/url?q=https://b.corp.google.com/issues?q%3Dreporter:ningk@google.com%2520%255BDiary%2520Study%255D%2520componentid:593447&sa=D&ust=1561076032663000&usg=AFQjCNHFLCi1qlI1ozbgT9LCQqeYkv_aFg).
* A Gradle based pipeline example additional to existing Maven examples on Beam and Dataflow 
quickstart.
* I (ningk) have gained some user empathy.

### Study Outcome for the Cryptocurrency Research
The data collected include prices of all cryptocurrencies for the last 365 days and their related
news for the last 2~3 weeks.
After running below pipeline:
![crypto-research-batch](https://i.imgur.com/nlrKeE9.png)

We achieved some correlation data that later can be explored within Big Query or Google sheet.

It's fairly hard to collect past news data since many news links and contents expire in a short
period of time.

#### Conclusion 1 - In General, news sentiment doesn't reflect in the price change of cryptocurrencies

Good news, bad news, news with strong emotion or not, they don't reflect or influence the price 
change of cryptocurrencies. Or at least not linearly correlated.

![Sentiment X Price Change](https://i.imgur.com/hHGnGKn.png)

Even after removing all the neutral news

![Non-neutral Sentiment X Price Change](https://i.imgur.com/5JBRvYR.png)

#### Conclusion 2 - Price change and news sentiment follow normal distribution
![Price Change](https://i.imgur.com/SlNHWm6.png)
![News Sentiment](https://i.imgur.com/EcIHo20.png)

Even after removing all the neutral news, looking at correlated Price Change:

![Non-neutral Price Change](https://i.imgur.com/wwF7EmI.png)

The normal distribution's expectation falls within almost no price change and neutral news.

#### Conclusion 3 - There might be some correlation after all
After removing all the neutral news, sort data by correlated Price Change:

![Non-neutral sorted Price Change X News Sentiment](https://i.imgur.com/CgrDwPK.png)

As we can observe, there are several price change ranges where non-neutral news sentiment score
correlates to. Roughly demonstrated as:

![Demonstration](https://i.imgur.com/ePO2FZW.png)

For example, we can say when a news is scored ~ 0.4 (looks positive, right?), it's possible the 
crypto currency will have a decrease in value between -10% and -4%.

If we take Bitcoin only, this characteristic still retends:

![Bitcoin](https://i.imgur.com/zAySwxL.png)

#### Future Work
* We could analyze characteristics and correlation for/between each different cryptocurrency.
* We could build a stream pipeline on top of this example so that data collecting script doesn't
need to be run manually once in a while though batch pipeline can handle such duplicated inputs.
* We could use a better sentiment analysis model.
* We could do machine learning with the correlation data.
  * A saved trained tensorflow model is provided at crypto-tensorflow-saved-model that could predict
  change_percentile from coin, date, sentiment_neg, sentiment_neu, sentiment_pos and
  sentiment_compound.
 
# 06/10/2019

## Goal
* Modularize scraper for each currency
* Extract news sentiment
* Correlate price change and news sentiment

## Diary
Modularized the script to collect all prices and news information for all crypto currencies by day.
Thus at the end of any day, the script is idempotent.

Pull Request: https://github.com/KevinGG/diary/commit/39b925a49f04ae2d9d55906485556ab216f422f6

Looking for open source dependency that can extract news sentiment scores from news title or 
content. Decided to use [vaderSentiment](http://t-redactyl.io/blog/2017/04/using-vader-to-handle-sentiment-analysis-with-social-media-text.html)
:

![http://t-redactyl.io/blog/2017/04/using-vader-to-handle-sentiment-analysis-with-social-media-text.html](http://t-redactyl.io/figure/Vader_1.jpg)

Using vaderSentiment module to analyze news title and lead and get sentiment scores.
The scores are negative value, neutral value, positive value and compound value.
We'll use compound for future analysis.
Pull Request: https://github.com/KevinGG/diary/commit/2a3a5d1f0ca57318623148264e3232d89c8ff5d5

**[Suggestion]** 

```
The job graph looks green when the pipeline is still running: 
https://screenshot.googleplex.com/uDWe0ENtKyK.png. So there is no way to immediately identify the 
failing step when you see error logs popping out.

Even after completing/failing the whole pipeline, the job logs don’t show which step the logging is 
at: https://screenshot.googleplex.com/w50aMYmrgE8 when the logged entry (e.g., the user code 
exception) does happen within some step (due to the timing/location of job message logging in 
workflow_monitor doesn’t have the step info).

So during/after a pipeline execution, if an error occurs in job logs, the user couldn’t directly 
find out which step runs into the exception.

You have to select a step in the graph to view the step logs that could give the step information: 
https://screenshot.googleplex.com/arfRTCxdVcn. However the step information is not that useful when 
you already know which step to select in the first place.

More confusingly, when you click on a red or grey step, there is no step log: 
https://screenshot.googleplex.com/kOLBYf2suOa, https://screenshot.googleplex.com/hx6VaB5zfBT

Based on the exception, the failing step should be “CalculatePriceChangesByDay”, however, steps 
before that are shown as grey and red too.
```

Upgraded the existing batch pipeline to accept all crypto currency price data and output all entries
in csv format. Exported the data to BigQuery.
An example query, “find out all dates and price changes when bitcoin closing value increased more 
than 10% in the last 365 days”: https://screenshot.googleplex.com/KjxHrom9mq4

# 06/07/2019

## Goal
* Build the first working batch pipeline

## Diary
Debugging the pipeline, realizing it’s because of the input file Date column not conforming to 
ISO-8061.

Updated the simple_scraper.py to populate ISO-8061 formatted date.
Had the first running Pipeline: https://screenshot.googleplex.com/K3SFNAhobS2

Output the history btc price change analysis in gcs: https://screenshot.googleplex.com/NypFnwbuygr

No that we have the price change analysis, we can join price change with news sentiment by truncated
date (to unit DAYS).

AIs:
* We should modularize price and news collection to each currency. Price history can trace back to 
at least a year while news history can only trace back a week. A stream pipeline is more appropriate 
(though manually executing a script once per day is not that tedious). The script should take a list
of currency symbols [“btc”, …] and output $symbol_news.csv and $symbol_prices.csv files.
* We could make a stream pipeline to extract sentiments (by keyword fuzzy match or any existing 
algorithm) of recent (about last week) news. The sentiment should be a range of values, say 
[-10, 10].

We have the price percentile and value changes. With additional new sentiment value on each day, we 
can correlate them. Let x-axis be sentiment value and y-axis be price changes. We can have many 
different fun ways to render the data:
* Log or square
* Abs

# 06/06/2019 - Ning

## Goal
* Build the first working batch pipeline

## Diary
Added a pipeline to calculate daily crypto currency price change.

Interrupted by b/134581092

# 06/05/2019 - Ning

## Goal
* Build the first working batch pipeline

## Diary
About the suggestions from yesterday, I’ve revised the README file under crypto-research-batch sub 
project to include detailed instructions.

A user has to create and explicitly specify GCS buckets for tempLocation and stagingLocation to 
avoid GCS bucket anonymous caller permission issue when Dataflow service uses default GCS bucket.

Added --help command instructions for users to explore available options in terminal.

The current diary repo should already solved several [difficulties](go/dataflow-eou-diary-ningk-initial)
I’ve encountered in Beam and Dataflow quickstart, such as building with Gradle and authentication.

**[Suggestion - b/131697271, initiated by Sam]**

```
When reading a csv file, 
It would be great to have an ORM mechanism that parses columns for each line entry into user defined
serializable objects.
It could be better to have the reading handle corrupted line entries either by ignoring or allowing 
user defined process (e.g., use default values). In this way, the user doesn't need to write the 
boilerplate try-catch and additional filtering transform.
```

# 06/04/2019 - Ning

## Goal
* Setup Gradle dependencies for Beam SDK	

## Diary
Updated README files. Added bash syntax highlight for bash codeblocks.

Added dependencies for direct runner, dataflow runner.

Added instructions for finding dependencies in maven central and set chrome search engine.

Added instructions of running pipeline with Gradle and different runners.

A Dataflow run:

```bash
ningk@ningk:~/IdeaProjects/diary (b134396904)$ ./gradlew run --args='--runner=DataflowRunner'

> Task :crypto-research-batch:run
Jun 04, 2019 11:30:04 AM org.apache.beam.sdk.extensions.gcp.options.GcpOptions$DefaultProjectFactory create
INFO: Inferred default GCP project 'ningk-test-project' from gcloud. If this is the incorrect project, please cancel this Pipeline and specify the command-line argument --project.
Jun 04, 2019 11:30:04 AM com.google.auth.oauth2.DefaultCredentialsProvider warnAboutProblematicCredentials
WARNING: Your application has authenticated using end user credentials from Google Cloud SDK. We recommend that most server applications use service accounts instead. If your application continues to use end user credentials from Cloud SDK, you might receive a "quota exceeded" or "API not enabled" error. For more information about service accounts, see https://cloud.google.com/docs/authentication/.
Jun 04, 2019 11:30:05 AM org.apache.beam.sdk.extensions.gcp.options.GcpOptions$GcpTempLocationFactory tryCreateDefaultBucket
INFO: No tempLocation specified, attempting to use default bucket: dataflow-staging-us-central1-23379953210
Jun 04, 2019 11:30:05 AM org.apache.beam.sdk.extensions.gcp.util.RetryHttpRequestInitializer$LoggingHttpBackOffHandler handleResponse
WARNING: Request failed with code 409, performed 0 retries due to IOExceptions, performed 0 retries due to unsuccessful status codes, HTTP framework says request can be retried, (caller responsible for retrying): https://www.googleapis.com/storage/v1/b?predefinedAcl=projectPrivate&predefinedDefaultObjectAcl=projectPrivate&project=ningk-test-project. 
Jun 04, 2019 11:30:06 AM org.apache.beam.runners.dataflow.options.DataflowPipelineOptions$StagingLocationFactory create
INFO: No stagingLocation provided, falling back to gcpTempLocation
Jun 04, 2019 11:30:06 AM org.apache.beam.runners.dataflow.DataflowRunner fromOptions
INFO: PipelineOptions.filesToStage was not specified. Defaulting to files from the classpath: will stage 137 files. Enable logging at DEBUG level to see which files will be staged.

BUILD SUCCESSFUL in 3s
2 actionable tasks: 2 executed

Publishing build scan...
https://gradle.com/s/qkydt6edlylhs
```

**[Suggestion 1]** 

```
Error messages (gpaste) from Dataflow doesn’t provide project ID but project number. It seems that 
both are unique while project ID (e.g., ningk-test-project) is more readable. The error message 
could output both. If a user works on multiple projects, they might need to check the 2 values on 
Pantheon dashboard a lot until they memorize all the pairs.
```

**[Suggestion 2]** 

```
Why does the GCS invoked by Dataflow service treat me as anonymous caller? I should have been 
authenticated using EUC from gCloud SDK. Unless the right way is to follow the instructions here for
authentication setting up an owner service account and download the credential. However, neither 
works. Also the latter instructions never mention activating the service account for auth. It seems 
that the authority is not forwarded from Dataflow service to GCS. As described in Cloud Dataflow 
security and permissions: https://screenshot.googleplex.com/52quNm6aSuG, this might be a “known” 
bug.
```

**[Suggestion 3]**
 
```
Due to [Suggestion 2], I have to explicitly set a tmpLocation and stagingLocation at the same time 
so that there is no weird permission issue. And I don’t know what flags are available. If only there
is such Pipeline option documentation in the guide.
```

Added explanation about auth and running pipeline locally or on Dataflow. Pull Request merged for 
goal "*Setup Gradle dependencies for Beam SDK*".

# 06/03/2019
Added Objective and Key Result to [Planr iteration](https://planr.corp.google.com/planr/planview?projectId=254977&iterationId=255091&name=allWork).
If you use Planr and the dataflow > EOU > diary component, to exclude diary study specific bugs from
Buganizer, use this filter:

```
assignee:$ldap@google.com status:open -componentid:642747
```

## Goal
* Build the first working batch pipeline
  * Setup Gradle dependencies for Beam SDK
* Collect cryptocurrency price and news sentiment data for batch
  * Fetch sentiments of cryptocurrency news

## Diary
* Setup Gradle dependencies for Beam SDK
  * Setup Gradle build file under crypto-research-batch.
  * Utilizes Gradle build scan and always publish the scan result. Engineers could discover their 
  build process on published web pages.
  * Beam Gradle dependency discovered and documented.
  * src folder created, Java package named “com.google.dataflow.eou.diary.crypto”, batch pipeline 
  class created “CryptoResearchBatch”. 
  * Transformed root project diary into a multi-project project.
* Collect cryptocurrency price and news sentiment data for batch
  * Fetch sentiments of cryptocurrency news
    * The crypto-news-api (2.3.1) provides sentiment API.
    * The sentiment data needs premium and the API is a blackbox which talks to their server through
    a proxy that requires API_KEY authentication.
    * Abandoning this task and stick with Sam's script.
  * Mark the epic as fixed and attached the pull request to master.

# 05/31/2019

## Goal - Only putting completed/pending ones from now on
The bigger picture goals (for me) are organized in the [Planr project](https://planr.corp.google.com/planr/goals?projectId=254977) 
and its iterations.

* Build the first working batch pipeline
  * Build a working .gitignore
  * Build empty Gradle project to hold pipeline
* Collect cryptocurrency price and news sentiment data for batch
  * Fetch cryptocurrency news

The verbose diaries function for diary study piloting. Future diaries will be compact.

## Diary
I’m using:
* Community edition of Intellij as IDE for Beam Java dev
* Gradle as build tool to build a batch pipeline under the diary named crypto-research-batch
* JDK >= 8

In root README.md, explain how to install Gradle and best practice to build.

Write a README.md under crypto-research-batch to explain the purpose of this sub-project.

Just showing a feature branch workflow example here, won’t record in future diaries:
* Added .gitignore for task "*Build a working .gitignore*" under goal (epic) "*Build the first 
working batch pipeline*".
* The branch created is https://github.com/KevinGG/diary/tree/b134156677.
* The squashed [commit](https://github.com/KevinGG/diary/commit/0f76be8a89ac85949afe3a44388767b65125a301) 
in branch [b134156677](https://github.com/KevinGG/diary/tree/b134156677).
* The [pull request](https://github.com/KevinGG/diary/pull/1) from branch [b134156677](https://github.com/KevinGG/diary/tree/b134156677)
to [master](https://github.com/KevinGG/diary/tree/master).
* The commit message (note to add a newline between title and details when editing):

  ```markdown
  [b/134156677] Added .gitignore
  1. Ignored Idea (Intellij) related files.
  2. Ignored some Gradle related files based on github official ignore
  guide project:
  https://github.com/github/gitignore/blob/master/Gradle.gitignore.
  ```

* Leave the branch [b134156677](https://github.com/KevinGG/diary/tree/b134156677) alone and delete it
from origin after some time. This UUID branch naming convention ensures that no contributor(s) would
(re)use the same branch name at any time. You can always locally delete the branch you are done with
by “git branch -d b12345”.

Initialized Gradle projects for "*Build empty Gradle project to hold pipeline*".

Recorded all Pull Requests in corresponding task tracking items.

Created a separated package for data ingestion.

Added simple scraper script and README:
* Use virtualenv, pip3 and Python3
* The https://cointelegraph.com/api/v1/content/json/_tp API now has captcha protection and blocks 
querying for news.

Resolved "*Fetch cryptocurrency news sentiments*" blocked by reCAPTCHA by adding fake user agent. 
The final squashed commit from branch [b134090007](https://github.com/KevinGG/diary/tree/b134090007)
will contain a row of detail commit message starting with [b/134176915](http://b/134176915) since 
this task belongs to the epic [b/134090007](http://b/134090007).

# 05/30/2019

## Goal - [Milestone 1](https://planr.corp.google.com/planr/planview?projectId=254977&goalId=134090993&name=allWork)
Pilot the diary study path for following contributors.

We consider it done when all the followings are met and should build a presentation to share in
WEUs:
1. a running pipeline that solves some real life problem;
2. document for the pipeline as an example, e.g., notebook or README;
3. a real improvement bug is documented and filed;
4. diary example written.

## Methodology
* Everything we’ve worked on including documents should be version controlled inside the repo for 
future references.
* Each feature should originate from a UUID from buganizer item (or whatever planning method that 
can identify a specific task)
* The repo should use feature branch git workflow. Branch naming uses the UUID, e.g., b123456 when 
using Buganizer or Planr.
* When sending pull requests, squash all commits from the branch and leave task UUID such as 
“[b/123456]” in the beginning of first line of commit message.
* [Optional] Mention the commit (link) in your task tracking/planning tool when you mark it as 
fixed/completed/end-state.
* Work on the goals planned for your iteration every day and write diary.
* Feel free to write detailed commit messages with bugs you filed and gaps/empathy you feel. The 
message format should be “compact titile started with UUID, new line, bulletin points”.
* [Conditional] A Buganizer component has been created if you use Buganizer.
* [Conditional] A Planr project has been created for that Buganizer component if you use Planr.
* Otherwise, feel free to use anything that could improve your productivity as long as it doesn’t 
enforce other contributors to depend on.

## Diary
I have received the scraper code from Sam and set up an initial git repo to hold everything about 
the diary study as a base to build on top of for everyone in the team and any new members to come. 
I try to minimize the process required and maximize the flexibility of methodology each individual 
prefers. 

* Setup the project [git repo](https://github.com/KevinGG/diary) and methodology for diary study.
* [First iteration](https://planr.corp.google.com/planr/planview?projectId=254977&iterationId=255091&name=allWork)
planned.
* Identified [Milestone 1](https://planr.corp.google.com/planr/planview?projectId=254977&goalId=134090993&name=allWork)
. Milestone 1 should pilot the diary study path for following contributors.
