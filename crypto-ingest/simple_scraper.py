import csv
from datetime import datetime
from datetime import timedelta
import json

from cryptocmd import CmcScraper
from dateutil.parser import parse
from pytz import timezone
import requests

# https://github.com/guptarohit/cryptoCMD
from_date = "28-04-2018"
to_date = datetime.now().strftime('%d-%m-%Y')


# Gets the price history of last year.
def ScrapePrices(coin_code):
  # Initializes scraper with time interval
  scraper = CmcScraper(coin_code, from_date, to_date)

  # Gets raw data as list of lists
  headers, data = scraper.get_data()
  # Parses dates like 28-04-2019
  for row in data:
    date = datetime.strptime(row[0], '%d-%m-%Y')
    # The datetime info doesn't have timezone, put a 'Z' in the end
    row[0] = date.strftime('%Y-%m-%dT%H:%M:%SZ')

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
      new_article = {
          'Date': article['publishedW3'],
          'Url': article['url'],
          'Title': article['title'],
          'Lead': article['leadfull']
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
        new_article = {
            'Date': article['publishedW3'],
            'Url': article['url'],
            'Title': article['title'],
            'Lead': article['leadfull']
        }
        articles.append(new_article)
      # Date format is '%Y-%m-%dT%H:%M:%S%z'
      date = parse(articles[-1]['Date'])
      page += 1

  with open(
      'news_' + coin_code + '_' + datetime.now().strftime('%Y-%m-%d') + '.csv',
      'w') as f:
    w = csv.DictWriter(f, ['Date', 'Url', 'Title', 'Lead'])
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
  print ("Coin code: %s, tag: %s" % (code, coins[code]))
  try:
    ScrapePrices(code)
    ScrapeNews(code, coins[code])
  except Exception as exception:
    print ("Failed to collect some of the data for coin code: %s, tag: %s."
           " Exception %s" % (code, coins[code], exception))
