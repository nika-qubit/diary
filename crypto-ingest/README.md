# Data Ingest for Crypto Currency Research Projects
The package offers different data ingestion channels for crypto currency research pipelines.

## Simple Scraper (simple_scraper.py)
A scraper to collect historical crypto currency prices and related news sentiments.

### Tech Stack
If you use *[pipenv](https://github.com/pypa/pipenv)*, please feel free to keep a Pipfile in your work branch and keep
a record of dependencies in this README. We intend to maximize the flexibility in the tools or workflow choice for each
contributor while keeping the code base platform, tool and workflow isolated.
* Python3
* pip3
* virtualenv

If you use *[homebrew](https://brew.sh/)*, installation:

```
# If you don't have Python3.
brew install python

# If you want to use pipenv.
brew install pipenv

# If you don't use pipenv.
pip3 install virtualenv
```

If you use *[apt/apt-get](https://askubuntu.com/questions/445384/what-is-the-difference-between-apt-and-apt-get)*
, installation:

```
apt install python3-pip
pip3 install virtualenv
```

### Instructions
#### Activate Virtual Env

```
virtualenv env
. ./env/bin/activate
```
"env" has been ignored by this repo.
Check .gitignore for details.
Do not create and version control your virtual environment in this repo.

#### Packages to install
Install packages in your virtual environment after activating it.

```
# If not yet, activate the virtual environment.
. ./env/bin/activate

pip3 install cryptocmd
pip3 install pandas
pip3 install crypto-news-api
```

#### Run Script

```
# If not yet, activate the virtual environment.
. ./env/bin/activate

python3 simple_scraper.py
```
You should be able to see 2 files generated:
* btc_prices.csv
* btc_news.csv