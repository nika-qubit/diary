# Crypto Research - Batch
This project researches correlation between crypto currency price changes and news sentiments.

## Details
* A batch pipeline:
  * input with historical crypto currency prices and news sentiments data gathered and stored on 
    [GCS](https://cloud.google.com/storage/).
  * output correlation between price changes and news sentiments.

## Tech Stack
There are Maven documents of Beam and runner dependencies in
 [Dataflow Quickstart](https://cloud.google.com/dataflow/docs/quickstarts/quickstart-java-maven) and
 [Beam Quickstart](https://beam.apache.org/get-started/quickstart-java/). The following instructions
 are for Gradle users only.

Note Beam Java SDK **requires Java 8** to work.

```groovy
dependencies {
  // Beam SDK
  // https://search.maven.org/artifact/org.apache.beam/beam-sdks-java-core/2.12.0/jar
  implementation 'org.apache.beam:beam-sdks-java-core:2.12.0'
  
  // Beam direct runner
  // https://search.maven.org/artifact/org.apache.beam/beam-runners-direct-java/2.13.0/jar
  implementation 'org.apache.beam:beam-runners-direct-java:2.13.0'
  
  // Google Cloud Dataflow runner
  // https://search.maven.org/artifact/org.apache.beam/beam-runners-google-cloud-dataflow-java/2.13.0/jar
  implementation 'org.apache.beam:beam-runners-google-cloud-dataflow-java:2.13.0'
  
  // slf4j
  // https://search.maven.org/artifact/org.slf4j/slf4j-api/1.7.26/jar
  implementation 'org.slf4j:slf4j-api:1.7.26'
  // https://search.maven.org/artifact/org.slf4j/slf4j-jdk14/1.7.26/jar
  implementation 'org.slf4j:slf4j-jdk14:1.7.26'
}
```

### SDK
* [Apache Beam](https://beam.apache.org/)
* [Google Cloud SDK](https://cloud.google.com/sdk/) You will need this if you need any Google Cloud
 support such as reading files from GCS or running pipeline on Google Cloud Dataflow.

### Runner
#### local runner

```bash
# Run pipeline with direct runner
./gradlew run --args='--runner=DirectRunner'
```

#### [Dataflow](https://cloud.google.com/dataflow/)
* Check your [Dataflow GCP console](https://pantheon.corp.google.com/dataflow) on Google Cloud and
 make sure that you have Dataflow enabled.
* Authenticate with service account
 (referring the "*Set up authentication:*" step of "*Before you begin*" in
 [Dataflow Quickstart](https://cloud.google.com/dataflow/docs/quickstarts/quickstart-java-maven))
  * In the GCP Console, go to the
  [Create service account key](https://pantheon.corp.google.com/apis/credentials/serviceaccountkey) page.
  * From the Service account list, select New service account.
  * In the Service account name field, enter a name.
  * From the Role list, select Project > Owner. *Note: The Role field authorizes your service
   account to access resources. You can view and change this field later by using the GCP Console.
   If you are developing a production app, specify more granular permissions than Project > Owner.
   For more information, see granting roles to service accounts.
  * Click Create. A JSON file that contains your key downloads to your computer.
  * Activate the service account.

  ```bash
  # E.g.,  gcloud auth activate-service-account ningk-001@ningk-test-project.iam.gserviceaccount.com --key-file=/usr/local/google/home/ningk/gcp_credential.json
  gcloud auth activate-service-account $SERVICE_ACCOUNT_EMAIL --key-file=$JSON_FILE_DOWNLOADED
  ```

  * In your ~/.bashrc or ~/.bash_profile (MAC OS), add an environment variable
  
  ```bash
  # GCP application credentials
  export GOOGLE_APPLICATION_CREDENTIALS="$JSON_FILE_DOWNLOADED"
  ```

* Run pipeline on Dataflow

  ```bash
  # Once you are authenticated, run pipeline with Dataflow runner on Google Cloud. This will use your
  # default application and you should see the job in your Dataflow console of your default project.
  ./gradlew run --args='--runner=DataflowRunner'
  # Or you can specify the project you want to use.
  ./gradlew run --args='--runner=DataflowRunner --project=$YOUR_PROJECT_ID'
  # If you run into any anonymous caller related error when using default staging/temp GCS locations,
  # please create your own GCS buckets (e.g., gs://dataflow-eou-diary/staging) under the project and
  # explicitly specify those locations when running the pipeline.
  ./gradlew run --args='--runner=DataflowRunner --project=$YOUR_PROJECT_ID --tempLocation=gs://dataflow-eou-diary/staging --stagingLocation=gs://dataflow-eou-diary/staging'
  ```

* To view available options that you can pass as args

  ```bash
  # Shows all the options available. You can register your custom options as
  # `PipelineOptionsFactory.register(MyOptions.class);` so that they are available in help document.
  ./gradlew run --args='--help'

  # Shows all Dataflow pipeline options
  ./gradlew run --args='--help=org.apache.beam.runners.dataflow.options.DataflowPipelineOptions'
  ```
  
  E.g., look up the options defined by CryptoResearchBatch:
  
  ```bash
  ./gradlew run --args='--help=com.google.dataflow.eou.diary.crypto.CryptoResearchBatch$CryptoResearchBatchOptions'
  ```
  
  An example output:
  
  ```
  > Task :run
  com.google.dataflow.eou.diary.crypto.CryptoResearchBatch$CryptoResearchBatchOptions:
  
    --correlationOutput=<String>
      Default: gs://dataflow-eou-diary/correlation.csv
      Path to the file to write price change and news sentiment correlation to
    --currencyPriceChangeOutput=<String>
      Default: gs://dataflow-eou-diary/price_change_output.csv
      Path of the file to write price changes to
    --currencyPriceInputFile=<String>
      Default: gs://dataflow-eou-diary/prices_*.csv
      Path of the file to read crypto currency prices from
    --newsInputFile=<String>
      Default: gs://dataflow-eou-diary/news_*.csv
      Path of the file to read news articles from
  
  org.apache.beam.sdk.options.PipelineOptions:
  
    --jobName=<String>
      Default: JobNameFactory
      Name of the pipeline execution.It must match the regular expression
      '[a-z]([-a-z0-9]{0,38}[a-z0-9])?'.It defaults to
      ApplicationName-UserName-Date-RandomInteger
    --optionsId=<long>
      Default: AtomicLongFactory
    --runner=<Class>
      Default: DirectRunner
      The pipeline runner that will be used to execute the pipeline. For
      registered runners, the class name can be specified, otherwise the fully
      qualified name needs to be specified.
    --stableUniqueNames=<OFF | WARNING | ERROR>
      Default: WARNING
      Whether to check for stable unique names on each transform. This is
      necessary to support updating of pipelines.
    --tempLocation=<String>
      A pipeline level default location for storing temporary files.
    --userAgent=<String>
      Default: UserAgentFactory
      A user agent string describing the pipeline to external services. The format
      should follow RFC2616. This option defaults to "[name]/[version]" where name
      and version are properties of the Apache Beam release.
  ```
