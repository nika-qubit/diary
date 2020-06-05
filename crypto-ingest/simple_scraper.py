import sys
import traceback
import csv
from datetime import datetime
from datetime import timedelta
import json

from cryptocmd import CmcScraper
from dateutil.parser import parse
from pytz import timezone
import requests

from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer

# https://github.com/guptarohit/cryptoCMD
from_date = "01-01-2020"
to_date = datetime.now().strftime('%d-%m-%Y')

# Hutto, C.J. & Gilbert, E.E. (2014). VADER: A Parsimonious Rule-based Model for
# Sentiment Analysis of Social Media Text. Eighth International Conference on
# Weblogs and Social Media (ICWSM-14). Ann Arbor, MI, June 2014.
sentiment_analyzer = SentimentIntensityAnalyzer()


# Gets the price history of last year.
def ScrapePrices(coin_code):
  # Initializes scraper with time interval
  scraper = CmcScraper(coin_code, from_date, to_date)

  # Gets raw data as list of lists
  headers, data = scraper.get_data()
  headers.insert(0, 'Coin')
  # Parses dates like 28-04-2019
  for row in data:
    date = datetime.strptime(row[0], '%d-%m-%Y')
    # The datetime info doesn't have timezone, put a 'Z' in the end
    row[0] = date.strftime('%Y-%m-%dT%H:%M:%SZ')
    row.insert(0, coin_code)

  with open(('prices_' + coin_code + '_' + datetime.now().strftime(
      '%Y-%m-%d') + '.csv'),
            'w') as f:
    w = csv.writer(f)
    w.writerow(headers)
    w.writerows(data)


# Gets the recent news (about 1 week).
# https://cointelegraph.com/
def ScrapeNews(coin_code, coin_tag):
  articles = []
  url = "https://cointelegraph.com/api/v1/content/json/_tp"
  with requests.Session() as s:
    s.headers.update({'User-Agent': 'Fake User Agent'})
    most_recent = json.loads(
        s.post(url, data={'lang': 'en', 'tag': coin_tag}).text)
    index = 0
    for article in most_recent['posts']['recent']:
      sentiment = sentiment_analyzer.polarity_scores(
          (article['title'] + '. ' + article['leadfull']))
      new_article = {
          'Coin': coin_code,
          'Date': article['publishedW3'],
          'Url': article['url'],
          'Title': article['title'],
          'Lead': article['leadfull'],
          'neg': sentiment['neg'],
          'neu': sentiment['neu'],
          'pos': sentiment['pos'],
          'compound': sentiment['compound']
      }
      articles.append(new_article)

    # The API starts counting the pages at 2. Page 1 is the most_recent.
    page = 2
    now = timezone("America/Los_Angeles").localize(datetime.now())
    date = now - timedelta(days=365)
    while date < now:
      resp = json.loads(s.post(url, data={'lang': 'en', 'page': page,
                                          'tag': coin_tag}).text)
      if len(resp['posts']['recent']) == 0:
        break
      for article in (resp['posts']['recent']):
        sentiment = sentiment_analyzer.polarity_scores(
            (article['title'] + '. ' + article['leadfull']))
        new_article = {
            'Coin': coin_code,
            'Date': article['publishedW3'],
            'Url': article['url'],
            'Title': article['title'],
            'Lead': article['leadfull'],
            'neg': sentiment['neg'],
            'neu': sentiment['neu'],
            'pos': sentiment['pos'],
            'compound': sentiment['compound']
        }
        articles.append(new_article)
      # Date format is '%Y-%m-%dT%H:%M:%S%z'
      date = parse(articles[-1]['Date'])
      page += 1

  with open(
      'news_' + coin_code + '_' + datetime.now().strftime('%Y-%m-%d') + '.csv',
      'w') as f:
    w = csv.DictWriter(f, ['Coin', 'Date', 'Url', 'Title', 'Lead', 'neg', 'neu',
                           'pos', 'compound'])
    w.writeheader()
    for article in articles:
      w.writerow(article)


# Gets all coin data
coins = {
    "btc": "bitcoin",
    "ltc": "litecoin",
    "nmc": "namecoin",
    "ppc": "peercoin",
    "doge": "dogecoin",
    "grc": "gridcoin",
    "xpm": "primecoin",
    "xrp": "ripple",
    "nxt": "nxt",
    "aur": "auroracoin",
    "dash": "dash",
    "neo": "neo",
    "xmr": "monero",
    "xem": "nem",
    "tit": "titcoin",
    "xvg": "verge",
    "xlm": "stellar",
    "vtc": "vertcoin",
    "eth": "ether",
    "etc": "ethereumclassic",
    "usdt": "tether",
    "zec": "zcash",
    "bch": "bitcoincash",
    "eos": "eos.io"
}
for code in coins:
  print("Coin code: %s, tag: %s" % (code, coins[code]))
  try:
    ScrapePrices(code.upper())
  except (KeyboardInterrupt, SystemExit):
    raise
  except:
    print("Failed to collect prices data for coin code: %s, tag: %s."
           % (code, coins[code]))
    _, _, exc_traceback = sys.exc_info()
    traceback.print_tb(exc_traceback)
  try:
    ScrapeNews(code, coins[code])
  except (KeyboardInterrupt, SystemExit):
    raise
  except:
    print("Failed to collect news data for coin code: %s, tag: %s."
           % (code, coins[code]))
    _, _, exc_traceback = sys.exc_info()
    traceback.print_tb(exc_traceback)

