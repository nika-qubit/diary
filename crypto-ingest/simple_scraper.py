import csv
import json
import requests

from cryptocmd import CmcScraper
from datetime import datetime
from datetime import timedelta

# https://github.com/guptarohit/cryptoCMD
from_date = "28-04-2018"
to_date = datetime.now().strftime('%d-%m-%Y')


def Scrape(coin_code):
  # Initializes scraper with time interval
  scraper = CmcScraper(coin_code, from_date, to_date)

  # Gets raw data as list of lists
  headers, data = scraper.get_data()
  # Parses dates like 28-04-2019
  for row in data:
    date = datetime.strptime(row[0], '%d-%m-%Y')
    row[0] = date.strftime('%Y-%m-%dT%H:%M:%S%z')

  with open('btc_prices.csv', 'w') as f:
    w = csv.writer(f)
    w.writerow(headers)
    w.writerows(data)


# Gets all coin data
coin_codes = ["BTC"]
for code in coin_codes:
  Scrape(code)

# Gets the news
# https://cointelegraph.com/
articles = []
url = "https://cointelegraph.com/api/v1/content/json/_tp"
with requests.Session() as s:
  s.headers.update({'User-Agent': 'Fake User Agent'})
  most_recent = json.loads(
      s.post(url, data={'lang': 'en', 'tag': 'bitcoin'}).text)
  index = 0
  for article in most_recent['posts']['recent']:
    new_article = {
        'Date': article['publishedW3'].encode('utf-8'),
        'Url': article['url'].encode('utf-8'),
        'Title': article['title'].encode('utf-8'),
        'Lead': article['leadfull'].encode('utf-8')
    }
    # Parses dates like '2019-04-24T18:03:00+01:00'
    # date = datetime.strptime(new_article['Date'], '%Y-%m-%dT%H:%M:%S%z')
    articles.append(new_article)

  page = 2

  # The API starts counting the pages at 2. Page 1 is the most_recent.
  date = datetime.now() - timedelta(days=365)
  while date < datetime.now():
    resp = json.loads(s.post(url, data={'lang': 'en', 'page': page,
                                        'tag': 'bitcoin'}).text)
    if len(resp['posts']['recent']) == 0:
      break
    for article in (resp['posts']['recent']):
      new_article = {
          'Date': article['publishedW3'].encode('utf-8'),
          'Url': article['url'].encode('utf-8'),
          'Title': article['title'].encode('utf-8'),
          'Lead': article['leadfull'].encode('utf-8')
      }
      articles.append(new_article)
    date = datetime.strptime(articles[-1]['Date'].decode('utf-8'),
                             '%Y-%m-%dT%H:%M:%S+01:00')
    page += 1

with open('btc_news.csv', 'w') as f:
  w = csv.DictWriter(f, ['Date', 'Url', 'Title', 'Lead'])
  w.writeheader()
  for article in articles:
    w.writerow(article)
