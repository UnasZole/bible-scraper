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
- `./run.sh help scraper-help -s <ScraperName>` : Get information about this scraper, and if it requires any additional inputs. \
You can also specify additional inputs within this help command to check if they are valid, and get more specific help if available.
For example `scraper-help -s GenericHtml -i TheoPlace`.
- `./run.sh scrape` : Fetch and convert a remote bible. This command has many parameters :
  - `-s <ScraperName>` to specify the scraper, and `-i <input>` to specify scraper inputs (repeated if several)
  - `-b <osisBook> -c <chapterNb>` to specify the passage of the bible to extract, or `--fullBible` if you want to extract the complete bible.
  - `-w <outFormat> -o <outFileOrFolder>` to specify the output format (OSIS or USFM), and where to write the result. \
For OSIS, a file will be created (or overwritten if exists) at the given path. \
For USFM, the path must point to an existing empty folder. \
If the "-o" option is omitted, the output will be printed in your terminal (useful for testing when you're writing a new scraper).
  - `--typographyFixer <FRENCH or NONE>` to enforce some typography rules on the extracted text before printing it to the output document.

Examples :

- Scrape the full bible of André Chouraqui, applying transformations to integrate deuterocanonical additions in the catholic way, outputting an OSIS XML file with corrected French typography : \
  `./run.sh scrape -s ChouraquiAggregated -i Catholic --fullBible -w OSIS -o andreChouraqui.xml --typographyFixer FRENCH`
- Extract the book of Genesis from recatho.com's "Glaire et Vigouroux" bible using an embedded generic HTML scraper, outputting USFM files in a given folder : \
`./run.sh scrape -s GenericHtml -i Recatho -i glaire-et-vigouroux -b Gen -w USFM -o outGlaireVigouroux/`
- Extract Revelation 1 from a custom website using the generic scraper, and output in USFM on your terminal's STDOUT (usually to test your custom scraper) : \
`./run.sh scrape -s GenericHtml -i path/to/custom/scraperconfig.yaml -b Rev -c 1 -w USFM`

### Scraping a custom website with the generic HTML scraper

If you need to scrape a website which is not supported by any of the embedded scrapers, you may be able to make use of
the generic HTML scraper.
This is a scraper which is fully configured with a YAML configuration file, where you define the structure of the bible
(specifically the URLs to fetch its contents and the rules to parse all pages), thus avoiding you the need to write Java code at all.

#### YAML configuration file

You may look at the [embedded generic scrapers](src/main/resources/scrapers/GenericHtml/) for examples of generic scraper
configuration files.

The general structure of the configuration file is explained here.

##### Patterns and arguments

- A `patterns` section specifies patterns used to build the document structure. All of these patterns can contain
references to arguments in the form `{ARGUMENT_NAME}`, which will be substituted by the value of the argument at runtime. \
By default, 3 pattern names are used :
  - `bookUrl`, if provided, will be used to fetch pages that initialise the book context. That's what you should use if
all chapters of a book are listed on a single page, or if there is a dedicated page for a book's introduction.
  - `chapterUrl`, if provided, will be used to fetch pages that contain only one chapter. When scraping the full book,
the scraped content of these pages will be appended at the end of the book context initialised with `bookUrl`.
  - `chapterPublishedNumber` should be provided along chapterUrl if you want to preserve original chapter numbers from
the bible. For example, if the bible uses the old psalm numbering (9A/9B, etc.). If unspecified, the OSIS chapter numbering
will be used.
- An `args` section specifies the values for arguments to be substituted in the patterns. \
Note that, while argument values will generally be substituted directly without processing, there is one exception.
If the argument value starts with `=` and contains a basic arithmetic expression of the form `= $i`, `= $i - 1`, `= $i + 1`,
then it will be evaluated when the pattern is used under a "chapter sequence" (see below), using the osis chapter number
as `$i`. This is to be able to share the same argument value definition across multiple chapters.
- The `input` section specifies that some argument values have to be provided as scraper input.

The usual setup is to specify the "patterns" globally at the top of the file (as the URL structure are usually consistent
within a website), and specify the "args" values for each book or chapter. However, it is also possible to override the
patterns for one specific book or chapter if needed.

##### Book and chapter structure

The books included in the bible must be specified as a list in the `books` root field. \
For each book, the following information is provided :
- The `osis` field identifies the book being scraped by its [book osisID](https://wiki.crosswire.org/OSIS_Book_Abbreviations).
- The `args` field allows you to set argument values at book level (typically the book name used by the website in its URLs).
- If a `pages` argument is provided, then it's a list of pages to scrape for starting the book context. For each page,
you can specify different patterns and arguments. If omitted but a `bookUrl` pattern is set, then the scraper will fetch
one single page using the book's args.
- If a `chapters` argument is provided, then it's a description of the chapter pages to scrape for this book. This is
only relevant if the book has separate pages for each chapter. In order to avoid listing each and every chapter individually
this property actually contains a list of "chapter sequences".

A "chapter sequence" describes several chapters sharing the same patterns and mostly the same arguments. It contains the
following information :
- The `from` and `to` properties are the OSIS number of the first and last chapter covered by this sequence. All numbers
in between will be scraped with the same configuration as well. If this sequence contains only one chapter, the `at`
property may be used instead.
- The `args` field allows you to set argument values for this whole sequence.
- If a `pages` argument is provided, then it's a list of pages to scrape for each chapter within this sequence. For each
page, you can specify different patterns and arguments. If omitted but a `chapterUrl` pattern is set, then the scraper
will fetch one single page using the chapter sequence's args.

##### Page parsing rules

While all the properties described above serve to build the list of HTML pages to fetch, this last section is required to
let the scraper know how these pages should be parsed.

In order to write these rules, you need to understand the overall parsing logic - which may be slightly counter-intuitive,
but allows parsing even documents with ambiguous formatting.

- The overall goal of the parser is to check all HTML elements in the page, and build a context tree (an internal
structured representation of bible contents) out of it.
- The [definition of all context types](src/main/java/com/github/unaszole/bible/datamodel/ContextType.java) specifies
what each context can contain. Thus, at any point in time, the parser itself knows what types of context it may or may
not open next. 
The goal the parsing rules is therefore to answer the following two questions :
  - Given the current position in the context tree, can a given HTML element open a new context ?
  - If so, how do we extract the HTML element's contents to fill the new context ?

HTML element parsing rules are specified in the `elements` section of the YAML configuration. Each
element parsing rule has the following contents :
- The `selector` property is a [JSoup CSS selector](https://jsoup.org/apidocs/org/jsoup/select/Selector.html), evaluated from the root of the HTML document, to match an
input HTML element. If the current HTML fails to match this selector, then the rule is ignored.
- The `withAncestors` and `withoutAncestors` properties are optional lists of context types that will be matched against
the context stack when a rule is evaluated. If any of the `withAncestors` context types is missing, or if
any of the `withoutAncestors` is present in the context stack, the rule is ignored.
This allows restricting the applicability of a rule, in case a same HTML element should be interpreted differently
depending on the context. For example "A \<p\> element should open a TEXT context, but only
if we are in a book introduction".
- The `contexts` property contains a sequence of context extractors, meaning a sequence of contexts to be built from
the selected HTML element and instructions to read the contents from the element. Each context extractor has the
following properties.
  - The `type` property designates which [context type](src/main/java/com/github/unaszole/bible/datamodel/ContextType.java) is opened by this extractor.
  If this context types requires a context value, the following properties specify how to read the context value from the
  HTML element.
  - The `selector` property is an optional [JSoup CSS selector](https://jsoup.org/apidocs/org/jsoup/select/Selector.html), evaluated from the rule's selected HTML element,
  to fetch one of its descendants. If unspecified, the following properties apply directly to the rule's selected element.
  - The `linkTargetSelector` property is an optional [JSoup CSS selector](https://jsoup.org/apidocs/org/jsoup/select/Selector.html), evaluated from the target of the
  previously selected HTML element, assuming it was a link to an anchor in the same page. This is typically used to
  resolve footnotes, for which the marker in the original text is usually a link to a separate container.
  - The `op` property is an operator to extract the context value from the selected element. May be one of
    - `text`: Full text contents of the element and its descendants.
    - `ownText`: Text contents of only this element excluding its descendants.
    - `attribute=<attributeName>`: Text value of an HTML attribute of the element.
    - `literal=<value>`: Hardcoded text value.
  - The `regexp` property allows using a [Java regular expression](https://dev.java/learn/regex/), containing a single
  capturing group, to extract only a portion of the text returned by the operator.
- The `descendants` property is a sequence of similar context extractors to create other contexts within this one, based
on the same element selected by the rule.

Elements which text nodes need to be processed in order can be managed using the `nodeParsers` list.
Each node parser has the following contents :
- The `selector` property is a [JSoup CSS selector](https://jsoup.org/apidocs/org/jsoup/select/Selector.html), evaluated from the root of the HTML document, that determines
which elements will use this specific parser.
- The `withAncestors`/`withoutAncestors` directives that enable this specific parsing method conditionally depending 
on the current position in the context tree.
- The `elements` property is a list of element parsing rules. The structure is identical to that defined above for the
root level `elements` property.
- The `nodes` property is a list of text node parsing rules. Each text node parsing rule has the following contents:
  - The `whitespaceProcessing` optional property defines how whitespaces in the text node will be treated for this rule.
  The value `CSS` (default) will attempt to remove all whitespaces that would not be displayed in the web browser as
  specified in the CSS3 specifications, whereas the value `PRESERVE` will keep all whitespaces that are present in the
  source document.
  - The optional `regexp` property is a regular expression evaluated on the node's text content. If the regexp does not
  match, the rule is ignored. If the regexp matches and contains a capturing group, only the content of this capturing
  group will be sent to the context extractors.
  - The `contexts` property contains a sequence of context extractors, meaning a sequence of contexts to be built from
    the selected text node and instructions to read the contents from the node. Each context extractor has the
    following properties.
    - The `type` property designates which [context type](src/main/java/com/github/unaszole/bible/datamodel/ContextType.java) is opened by this extractor.
    - The `regexp` property to select which part of the contents to use as context value. To use the full text contents
    of the text node (or the portion given by the regexp at rule level), you should use `(.*)`.
    - The `descendants` property is a sequence of similar context extractors to create other contexts within this one,
    based on the same text node selected by the rule.

#### Limitations

Due to its much simpler setup, the generic HTML scraper has some limitations compared to what can be done with native
Java scrapers.
Notably :
- It takes rules for each HTML element in isolation. Therefore, the structure of the HTML pages must
be consistent enough that you only need to check the element itself (its contents, its attributes, but NOT its siblings
or parents) to know what type of context this element may open.
- It can take additional inputs, allowing it to support websites that propose many bibles - however it
does not provide a way to specify a different page structure for each bible, so you need all bibles reached by this scraper
to follow a consistent URL pattern and consistent structure in terms of books and chapters.

These limitations are highlighted within [the example TheoPlace configuration file](src/main/resources/scrapers/GenericHtml/TheoPlace.yaml),
which you may compare with the [native TheoPlace scraper](src/main/java/com/github/unaszole/bible/scraping/implementations/TheoPlace.java)
to see how the Java API allows more accurate control in these cases.

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
