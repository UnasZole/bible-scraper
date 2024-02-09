# Bible scraper

The bible scraper is a tool that allows retrieving the contents from bibles
freely available online, and convert it into standard formats (OSIS / USFM)
easily processed by bible study software.

In short, you can scrape bibles from any website to integrate them into your
favourite bible study app.

## Features

- **Embedded scrapers for many bibles** : Just run the program to extract a bible
from one of the websites already implemented.
- **Generic HTML scrapers** : Extract bibles from a custom website, that we do not
support yet, by providing a configuration file describing the structure of the
website.
- **USFM or OSIS output formats** : [USFM](https://ubsicap.github.io/usfm/) uses simple
markers in text to be more easily manipulated, while [OSIS](https://wiki.crosswire.org/OSIS)
uses a very detailed XML schema to represent more accurately the structure of
the original document.
- **Preserve introductions, sections and notes** : If the website provides them,
a scraper can extract non-canonical contents from the bible, not just the verses
themselves.
- **Edit bible structure** : In some websites, chapters may be missing and verse 
numbers might be shifted from the ones you expect. A scraper can perform some
transformations on the bible to adapt its structure to a certain extent.
- **Typography fixers** : Online bibles often do not follow the traditional typography
rules for written documents, in particular regarding usage of non-breakable spaces.
The scraper can enforce these rules for you. (only French supported as of now)

## Usage

### Building from source

Since this software is still in its infancy, we haven't published a compiled
version yet. Therefore, you need to compile it first from the source code.

#### Prerequisites

The only prerequisites are :
- Git. See https://git-scm.com/
- OpenJDK 11 or later. See for example https://adoptium.net/ 
- Maven 3.6 or later. See https://maven.apache.org/

All these programs may also be available in your operating system's software
repositories.

#### Build process

- Perform a git clone of this repository.
- Open a terminal in the directory containing the present file.
- Run the command `mvn clean install`

### Running the program

After a local maven build, you can run the program using the `run.sh` script.

The following sub commands are available :

- `./run.sh help scraper-list` : list the available embedded scrapers.
- `./run.sh help scraper-help -s <ScraperName>` : Get information about this scraper, and if it requires any additional inputs.
You can also specify additional inputs within this help command to check if they are valid, and get more specific help if available.
For example `scraper-help -s GenericHtml -i TheoPlace`.
- `./run.sh scrape` : Fetch and convert a remote bible. This command has many parameters :
  - `-s <ScraperName>` to specify the scraper, and `-i <input>` to specify scraper inputs (repeated if several)
  - `-b <osisBook> -c <chapterNb>` to specify the passage of the bible to extract, or `--fullBible` if you want to extract the complete bible.
  - `-w <outFormat> -o <outFileOrFolder>` to specify the output format (OSIS or USFM), and where to write the result.
For OSIS, a file will be created (or overwritten if exists) at the given path.
For USFM, the path must point to an existing empty folder.

Full example :

`./run.sh scrape -s GenericHtml -i Recatho -i glaire-et-vigouroux --fullBible -w USFM -o outGlaireVigouroux/`
will extract the full "Glaire et Vigouroux" bible from the website recatho.com, and output it in USFM in the given folder.

## Legal disclaimer

Bible Scraper is not affiliated with any of the rights holder of the bibles you may scrape.

Bible Scraper does not host or redistribute any content of the referenced bibles : it is
only a technical tool for format conversion, with the conversion process occurring on your
machine and at your initiative only.

As such, it is still your responsibility to make sure that your scraping of these bibles is
not forbidden by the applicable intellectual property laws if they are protected by copyright.

### French law / loi française

Sous la loi française, l'utilisation de Bible Scraper est permise pour un usage personnel et
familial au titre de la copie privée.

En effet, les articles [L.122-5](https://www.legifrance.gouv.fr/codes/article_lc/LEGIARTI000037388886)
et [L.211-3](https://www.legifrance.gouv.fr/codes/article_lc/LEGIARTI000038835794) du code de
la propriété intellectuelle spécifie que sont autorisées "Les copies ou reproductions réalisées à partir
d’une source licite et strictement réservées à l’usage privé du copiste et non destinées à une utilisation
collective".

Par ailleurs, Bible Scraper ne fournit par lui-même l'accès à aucun contenu, mais ne contient que des
liens vers un contenu disponible publiquement. Sous réserve que les sites aient eux-mêmes les autorisations
pour diffuser ces textes, ils constituent alors des "sources licites" au sens de la loi.

De plus, un [arrêt du 13 février 2014 de la Cour de Justice de l'Union Européenne](https://curia.europa.eu/juris/document/document.jsf?docid=147847)
confirme que la fourniture d'un lien vers un contenu public, si celui-ci ne contourne pas des mesures
de protection ou de restriction d'accès spécifiques, ne peut être assimilée à une contrefaçon.
