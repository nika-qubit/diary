# Crypto Research - Batch
This project researches correlation between crypto currency price changes and news sentiments.

## Details
* A batch pipeline:
  * input with historical crypto currency prices and news sentiments data gathered and stored on 
    [GCS](https://cloud.google.com/storage/).
  * output correlation between price changes and news sentiments.

## Tech Stack
* SDK
  * [Apache Beam](https://beam.apache.org/)
* Runner
  * local runner
  * [Dataflow](https://cloud.google.com/dataflow/)
 