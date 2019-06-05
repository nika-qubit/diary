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
  1. In the GCP Console, go to the
  [Create service account key](https://pantheon.corp.google.com/apis/credentials/serviceaccountkey) page.
  2. From the Service account list, select New service account.
  3. In the Service account name field, enter a name.
  4. From the Role list, select Project > Owner. *Note: The Role field authorizes your service
   account to access resources. You can view and change this field later by using the GCP Console.
   If you are developing a production app, specify more granular permissions than Project > Owner.
   For more information, see granting roles to service accounts.
  5. Click Create. A JSON file that contains your key downloads to your computer.
  6. Activate the service account.
  
  ```bash
  # E.g.,  gcloud auth activate-service-account ningk-001@ningk-test-project.iam.gserviceaccount.com --key-file=/usr/local/google/home/ningk/gcp_credential.json
  gcloud auth activate-service-account $SERVICE_ACCOUNT_EMAIL --key-file=$JSON_FILE_DOWNLOADED
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
