# A tensorflow model
The model is trained with data collected from executing [crypto-research-batch](https://kevingg.github.io/diary/crypto-research-batch/).
We'll not discuss the training process. However, you can play with the model.

## Serve the model
Using docker to serve the model:

```bash
mkdir -p /tmp/crypto/1/
cd crypto-tensorflow-saved-model
cp * /tmp/crypto/1/
docker run -p 8500:8500 -p 8501:8501 --mount type=bind,source=/tmp/crypto,target=/models/crypto  -e MODEL_NAME=crypto -t tensorflow/serving &
```

Once the container is up, you can make [REST](https://www.tensorflow.org/tfx/serving/api_rest) calls
to the served model:

```bash
curl -d '{"examples": [{"coin": "btc","sentiment_pos": 0.0,"sentiment_neg": 1.0,"sentiment_neu": 0.0,"sentiment_compound": -1.0,"date": 1560809713000}]}' \
-X POST http://127.0.0.1:8501/v1/models/crypto:regress

{
  "results": [0.00226326]
}
```
The result is the price change in percentile (positive for increment, negative for decrement).
