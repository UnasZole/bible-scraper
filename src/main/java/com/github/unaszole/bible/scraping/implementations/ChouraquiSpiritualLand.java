package com.github.unaszole.bible.scraping.implementations;

import com.github.unaszole.bible.CachedDownloader;
import com.github.unaszole.bible.datamodel.Context;
import com.github.unaszole.bible.datamodel.ContextMetadata;
import com.github.unaszole.bible.datamodel.ContextType;
import com.github.unaszole.bible.scraping.*;
import org.crosswire.jsword.versification.BibleBook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChouraquiSpiritualLand implements Scraper {
	
	private static final String URL_PREFIX = "https://www.spiritualland.org/Les%20Bibles%20Version%20Html/Bible%20-%20Version%20Chouraqui/";
	
	private static final Map<BibleBook, List<String>> BOOKS = new LinkedHashMap<>();

	static {
		// TORA
		BOOKS.put(BibleBook.GEN, List.of("Bereshit%20%20Genese.html"));
		BOOKS.put(BibleBook.EXOD, List.of("Noms%20%20Shemot%20Exode.html"));
		BOOKS.put(BibleBook.LEV, List.of("Il%20crie%20Vayiqra%20%20Levitique.html"));
		BOOKS.put(BibleBook.NUM, List.of("Au%20desert%20%20Bamidbar%20%20Nombres.html"));
		BOOKS.put(BibleBook.DEUT, List.of("Paroles%20Debarim%20%20Deuteronome.html"));

		// INSPIRES
		BOOKS.put(BibleBook.JOSH, List.of("Iehoshoua%20%20Josue.html"));
		BOOKS.put(BibleBook.JUDG, List.of("Suffetes%20%20Shophim%20Juges.html"));
		BOOKS.put(BibleBook.SAM1, List.of("Shemouel%201%20%20Samuel.html"));
		BOOKS.put(BibleBook.SAM2, List.of("Shemouel2%20%20Samuel.html"));
		BOOKS.put(BibleBook.KGS1, List.of("Rois%201%20%20Melakhim%20Rois.html"));
		BOOKS.put(BibleBook.KGS2, List.of("Rois%202%20Melakhim%20Rois%20.html"));
		BOOKS.put(BibleBook.ISA, List.of("Iesha%20yahou%20%20Isaie.html"));
		BOOKS.put(BibleBook.JER, List.of("Irmeyahou%20%20Jeremie.html"));
		BOOKS.put(BibleBook.EZEK, List.of("Iehezqel%20%20Ezechiel.html"));
		BOOKS.put(BibleBook.HOS, List.of("Hoshea%20%20Osee.html"));
		BOOKS.put(BibleBook.JOEL, List.of("Ioel%20Joel.html"));
		BOOKS.put(BibleBook.AMOS, List.of("Amos.html"));
		BOOKS.put(BibleBook.OBAD, List.of("Obadyah%20%20Abdias.html"));
		BOOKS.put(BibleBook.JONAH, List.of("Iona%20%20Jonas.html"));
		BOOKS.put(BibleBook.MIC, List.of("Liminaire%20pour%20Mikha.html"));
		BOOKS.put(BibleBook.NAH, List.of("Liminaire%20pour%20Nahoum.html"));
		BOOKS.put(BibleBook.HAB, List.of("Liminaire%20pour%20Habaqouq.html"));
		BOOKS.put(BibleBook.ZEPH, List.of("Sephanyah%20%20Sophonie.html"));
		BOOKS.put(BibleBook.HAG, List.of("Liminaire%20pour%20Hagai.html"));
		BOOKS.put(BibleBook.ZECH, List.of("Liminaire%20pour%20Zekharyah.html"));
		BOOKS.put(BibleBook.MAL, List.of("Liminaire%20pour%20Malakhi.html"));

		// ECRITS
		BOOKS.put(BibleBook.PS, List.of("Ketoubim%20%20Hagiographes%20Les%20Psaumes%201.html", "Ketoubim%20%20Hagiographes%20Les%20Psaumes%202.html"));
		BOOKS.put(BibleBook.PROV, List.of("Mishle%20%20Les%20Proverbes.html"));
		// Missing : IOB
		BOOKS.put(BibleBook.SONG, List.of("Shir%20Hashirim%20Le%20Cantique%20des%20Cantiques.html"));
		BOOKS.put(BibleBook.RUTH, List.of("Rout%20%20Routh%20%20Ruth.html"));
		BOOKS.put(BibleBook.LAM, List.of("Eikha%20%20Lamentations.html"));
		BOOKS.put(BibleBook.ECCL, List.of("Qohelet%20%20L%20Ecclesiaste.html"));
		/*
		BOOKS.put(BibleBook.ESTH, List.of("Ester%20%20Esther.html"));
		/*/
		BOOKS.put(BibleBook.ESTH, List.of("Liminaire%20pour%20ester%20grec.html"));
		//*/
		BOOKS.put(BibleBook.DAN, List.of("Daniel%20%20Daniel.html"));
		BOOKS.put(BibleBook.EZRA, List.of("Ezra%20%20Esdras.html"));
		BOOKS.put(BibleBook.NEH, List.of("Nehemyah%20%20Nehemie.html"));
		BOOKS.put(BibleBook.CHR1, List.of("Paroles%20des%20Jours%201%20%20.html"));
		BOOKS.put(BibleBook.CHR2, List.of("Paroles%20des%20Jours%202%20.html"));

		// DEUTERO
		BOOKS.put(BibleBook.TOB, List.of("Liminaire%20pour%20Tobyah.html"));
		BOOKS.put(BibleBook.JDT, List.of("Liminaire%20pour%20Iehoudit.html"));
		BOOKS.put(BibleBook.MACC1, List.of("Liminaire%20pour%201%20Hashmonaim.html"));
		BOOKS.put(BibleBook.MACC2, List.of("Liminaire%20pour%202%20Hashmonaim.html"));
		BOOKS.put(BibleBook.WIS, List.of("Liminaire%20pour%20Sagesse%20de%20Shelomo.html"));
		BOOKS.put(BibleBook.SIR, List.of("Liminaire%20pour%20Ben%20Sira.html"));
		BOOKS.put(BibleBook.BAR, List.of("Liminaire%20pour%20Baroukh.html"));
		BOOKS.put(BibleBook.EP_JER, List.of("Liminaire%20pour%20la%20lettre%20d%20Irmeyahou.html"));
		//**/BOOKS.put(BibleBook.ESTH_GR, List.of("Liminaire%20pour%20ester%20grec.html"));
		//**/BOOKS.put(BibleBook.PR_AZAR, List.of("Liminaire%20pour%20Daniel%20grec.html"));
		//**/BOOKS.put(BibleBook.SUS, List.of("Liminaire%20pour%20Daniel%20grec.html"));
		//**/BOOKS.put(BibleBook.BEL, List.of("Liminaire%20pour%20Daniel%20grec.html"));

		// PACTE NEUF
		BOOKS.put(BibleBook.MATT, List.of("Annonce%20de%20Matyah.html"));
		BOOKS.put(BibleBook.MARK, List.of("Annonce%20de%20Marcos.html"));
		BOOKS.put(BibleBook.LUKE, List.of("Annonce%20de%20Loucas.html"));
		BOOKS.put(BibleBook.JOHN, List.of("Annonce%20de%20Iohanan.html"));
		BOOKS.put(BibleBook.ACTS, List.of("Gestes%20d%20envoyes.html"));
		BOOKS.put(BibleBook.ROM, List.of("Lettres%20de%20Paulos.html"));
		BOOKS.put(BibleBook.COR1, List.of("Premiere%20lettre%20aux%20Corinthiens.html"));
		BOOKS.put(BibleBook.COR2, List.of("Deuxieme%20lettre%20aux%20Corinthiens.html"));
		BOOKS.put(BibleBook.GAL, List.of("Lettre%20aux%20Galates.html"));
		BOOKS.put(BibleBook.EPH, List.of("Lettre%20aux%20ephesiens.html"));
		BOOKS.put(BibleBook.PHIL, List.of("Lettre%20aux%20Philippiens.html"));
		BOOKS.put(BibleBook.COL, List.of("Lettre%20aux%20Colossiens.html"));
		BOOKS.put(BibleBook.THESS1, List.of("Premiere%20lettre%20aux%20Thessaloniciens.html"));
		BOOKS.put(BibleBook.THESS2, List.of("Deuxieme%20lettre%20aux%20Thessaloniciens.html"));
		BOOKS.put(BibleBook.TIM1, List.of("Premiere%20lettre%20a%20Timothee.html"));
		BOOKS.put(BibleBook.TIM2, List.of("Deuxieme%20lettre%20a%20Timotheos.html"));
		BOOKS.put(BibleBook.TITUS, List.of("Lettre%20a%20Titus.html"));
		BOOKS.put(BibleBook.PHLM, List.of("Lettre%20a%20Philemon.html"));
		BOOKS.put(BibleBook.HEB, List.of("Lettre%20aux%20Hebreux.html"));
		BOOKS.put(BibleBook.JAS, List.of("Lettre%20de%20Ia%20acob.html"));
		BOOKS.put(BibleBook.PET1, List.of("Lettres%20de%20Petros.html"));
		BOOKS.put(BibleBook.PET2, List.of("Deuxieme%20lettre%20de%20Petros.html"));
		BOOKS.put(BibleBook.JOHN1, List.of("Lettres%20de%20Iohanan.html"));
		BOOKS.put(BibleBook.JOHN2, List.of("Deuxieme%20lettre%20de%20Iohanan.html"));
		BOOKS.put(BibleBook.JOHN3, List.of("Troisieme%20lettre%20de%20Iohanan.html"));
		BOOKS.put(BibleBook.JUDE, List.of("Lettre%20de%20Iehouda.html"));
		BOOKS.put(BibleBook.REV, List.of("Decouvrement%20de%20Iohanan.html"));
	};
	
	private static URL getUrl(String urlPostfix) {
		try {
			return new URL(URL_PREFIX + urlPostfix);
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static List<URL> getPageUrls(BibleBook book) {
		return BOOKS.getOrDefault(book, Collections.emptyList())
			.stream()
			.map(p -> getUrl(p))
			.collect(Collectors.toList());
	}
	
	private static class ElementParser extends Parser<Element> {
		private final static String CHAPTER_TITLE_REGEXP = "^Chapitre (\\d+)\\.?$";
		private final static String S_CHAPTER_TITLE_CONTAINER = ":is(div,p,h3):matches(" + CHAPTER_TITLE_REGEXP + ")";
		private final static String S_NONCHAPTER_STRONG_CONTAINER = "div:not(" + S_CHAPTER_TITLE_CONTAINER + "):has(> strong)";
		private final static String S_SECTION_TITLE_CONTAINER = S_CHAPTER_TITLE_CONTAINER + " ~ " + S_NONCHAPTER_STRONG_CONTAINER;
		private final static String S_BOOK_INTRO_TITLE_CONTAINER = "strong:matches(^Liminaire )";
		private final static String S_BOOK_INTRO_CONTAINER = S_NONCHAPTER_STRONG_CONTAINER + ":not(" + S_CHAPTER_TITLE_CONTAINER + " ~ div)";
		private final static String VERSE_START_REGEXP = "^(\\d+[A-Z]?|[A-Z])\\.\\s+(.*)$";
		private final static String S_VERSE_START_CONTAINER = S_CHAPTER_TITLE_CONTAINER + " ~ div:matches(" + VERSE_START_REGEXP + ")";
		private final static String S_VERSE_CONTINUATION_CONTAINER = S_VERSE_START_CONTAINER + " ~ div:not(:is("
			+ S_CHAPTER_TITLE_CONTAINER + ", " + S_SECTION_TITLE_CONTAINER + ", " + S_VERSE_START_CONTAINER + "))";
		
		private final static Evaluator BOOK_TITLE_SELECTOR = QueryParser.parse(".text1titre div strong, body > table + div:has(strong)");
		private final static Evaluator BOOK_INTRO_SELECTOR = QueryParser.parse(S_BOOK_INTRO_CONTAINER + ":not(.text1titre *)");
		private final static Evaluator BOOK_INTRO_BR_SELECTOR = QueryParser.parse(S_BOOK_INTRO_CONTAINER + ":not(.text1titre *) + br");
		private final static Evaluator CHAPTER_TITLE_SELECTOR = QueryParser.parse(S_CHAPTER_TITLE_CONTAINER + ":not(.text1titre *)");
		private final static Evaluator SECTION_TITLE_SELECTOR = QueryParser.parse(S_SECTION_TITLE_CONTAINER + ":not(.text1titre *)");
		private final static Evaluator VERSE_START_SELECTOR = QueryParser.parse(S_VERSE_START_CONTAINER + ":not(.text1titre *)");
		private final static Evaluator VERSE_CONTINUATION_SELECTOR = QueryParser.parse(S_VERSE_CONTINUATION_CONTAINER + ":not(.text1titre *):not(:has(div))");
		
		private final static Pattern CHAPTER_TITLE_PATTERN = Pattern.compile(CHAPTER_TITLE_REGEXP);
		private final static Pattern VERSE_START_PATTERN = Pattern.compile(VERSE_START_REGEXP);
		
		private static String extract(Pattern pattern, int groupNb, String sourceString) {
			Matcher matcher = pattern.matcher(sourceString);
			if(matcher.matches()) {
				return matcher.group(groupNb);
			}
			throw new RuntimeException(sourceString + " did not match " + pattern);
		}

		private static boolean isBookIntroTitle(String title) {
			return title.startsWith("Liminaire ");
		}

		private static boolean isBookGroupTitle(String title) {
			return title.contains("TORA") || title.contains("Premiers Inspirés") || title.contains("Écrits");
		}

		private static int mapToOsisChapterNb(BibleBook book, String parsedNb) {
			if(book == BibleBook.ESTH && "0".equals(parsedNb)) {
				return 1;
			}
			return Integer.parseInt(parsedNb);
		}
		
		@Override
		protected Context readContext(Deque<ContextMetadata> ancestors, ContextType type, Element e) {
			ContextMetadata parent = ancestors.peekFirst();
			
			switch(type) {
				case CHAPTER:
					if(e.is(CHAPTER_TITLE_SELECTOR)) {
						String chapterNb = extract(CHAPTER_TITLE_PATTERN, 1, e.text());

						ContextMetadata chapterMeta = ContextMetadata.forChapter(parent.book,
								mapToOsisChapterNb(parent.book, chapterNb));

						return new Context(chapterMeta, chapterNb /*,
								new Context(
									ContextMetadata.forChapterTitle(parent.book, chapterNb),
									new Context(ContextMetadata.forText(), e.text())
								)*/
						);
					}
				
				case SECTION_TITLE:
					return e.is(SECTION_TITLE_SELECTOR) ? new Context(
						ContextMetadata.forSectionTitle(),
						new Context(ContextMetadata.forText(), e.text())
					) : null;
				
				case VERSE:
					if(e.is(VERSE_START_SELECTOR)) {
						String verseNb = extract(VERSE_START_PATTERN, 1, e.text());
						String verseText = extract(VERSE_START_PATTERN, 2, e.text());
						
						ContextMetadata verseMeta = ContextMetadata.forVerse(parent.book, parent.chapter,
								ParsingUtils.CatholicVersifications.mapVerseNbToOsisVerseNb(
										parent.book, parent.chapter, verseNb));
						
						return new Context(verseMeta, verseNb,
							new Context(
								ContextMetadata.forStructuredText(),
								new Context(ContextMetadata.forFlatText(),
										new Context(ContextMetadata.forText(), verseText)
								)
							)
						);
					}
				
				case PARAGRAPH_BREAK:
					
					if(hasAncestor(ContextType.BOOK_INTRO, ancestors)) {
						return e.is(BOOK_INTRO_BR_SELECTOR) ? new Context(
							ContextMetadata.forParagraphBreak()
						) : null;
					}
					
				case TEXT:
					
					if("".equals(e.text())) {
						// Do not create empty text nodes.
						return null;
					}
					if(hasAncestor(ContextType.BOOK_TITLE, ancestors)) {
						return e.is(BOOK_TITLE_SELECTOR) && !isBookIntroTitle(e.text()) && !isBookGroupTitle(e.text()) ? new Context(
									ContextMetadata.forText(), e.text()
						) : null;
					}
					if(hasAncestor(ContextType.BOOK_INTRO_TITLE, ancestors)) {
						return e.is(S_BOOK_INTRO_TITLE_CONTAINER) ? new Context(
								ContextMetadata.forText(), e.text()
						) : null;
					}
					if(hasAncestor(ContextType.BOOK_INTRO, ancestors) && hasAncestor(ContextType.FLAT_TEXT, ancestors)) {
						// Trying to find flat text within a book intro.
						return e.is(BOOK_INTRO_SELECTOR) ? new Context(
							ContextMetadata.forText(), e.text()
						) : null;
					}
					else if(isInVerseText(ancestors)) {
						// Trying to find additional text within a verse.
						return e.is(VERSE_CONTINUATION_SELECTOR) ? new Context(
							ContextMetadata.forText(), " " + e.text()
						) : null;
					}
				
				default:
					return null;
			}
		}
		
		//
		// External parsing to handle badly formatted pages like the end of the Gospel of Matthew.
		//
		
		private final static String UNFORMATTED_VERSE_REGEXP = "(\\d+)\\s+([^\\d]*)";
		private final static String S_UNFORMATTED_CHAPTER_CONTENT = S_CHAPTER_TITLE_CONTAINER + " + p:matches(^(" + UNFORMATTED_VERSE_REGEXP + ")+$)";
		private final static Evaluator UNFORMATTED_CHAPTER_CONTENT_SELECTOR = QueryParser.parse(S_UNFORMATTED_CHAPTER_CONTENT + ":not(.text1titre *)");
		private final static Pattern UNFORMATTED_VERSE_PATTERN = Pattern.compile(UNFORMATTED_VERSE_REGEXP);
		
		private static class UnformattedChapterParser extends Parser<MatchResult> {
			@Override
			protected Context readContext(Deque<ContextMetadata> ancestors, ContextType type, MatchResult verseMatch) {
				ContextMetadata parent = ancestors.peekFirst();
				switch(type) {
					case VERSE:
						String verseNb = verseMatch.group(1);
						ContextMetadata verseMeta = ContextMetadata.forVerse(
							parent.book,
							parent.chapter, 
							Integer.parseInt(verseNb)
						);
						return new Context(verseMeta, verseNb,
							new Context(
								ContextMetadata.forStructuredText(),
								new Context(ContextMetadata.forFlatText(),
										new Context(ContextMetadata.forText(), verseMatch.group(2))
								)
							)
						);
				}
				return null;
			}
		}
		
		@Override
		protected boolean parseExternally(Element e, Deque<Context> currentContextStack, ContextType maxDepth, ContextConsumer consumer) {
			
			Optional<Context> chapterContext = currentContextStack.stream()
				.filter(c -> c.metadata.type == ContextType.CHAPTER)
				.findFirst();
			
			if(chapterContext.isPresent() && e.is(UNFORMATTED_CHAPTER_CONTENT_SELECTOR)) {
				// We parse all unformatted verses and pass them to a dedicated parser.
				Matcher verseMatcher = UNFORMATTED_VERSE_PATTERN.matcher(e.text());
				new UnformattedChapterParser().parse(verseMatcher.results(), currentContextStack, maxDepth, consumer);
				
				return true;
			}
			return false;
		}
	}
	
	private final CachedDownloader downloader;
	
	public ChouraquiSpiritualLand(Path cachePath, String[] flags) throws IOException {
		this.downloader = new CachedDownloader(cachePath.resolve("ChouraquiSpiritualLand"));
	}
	
	private List<Document> getDocs(BibleBook book) {
		try {
			List<Document> docs = new ArrayList<>();
			for(URL pageUrl: getPageUrls(book)) {
				docs.add(Jsoup.parse(downloader.getFile(pageUrl).toFile()));
			}
			return docs;
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Stream<Element> getDocStream(BibleBook book) {
		Deque<Document> docs = new LinkedList<>(getDocs(book));
		Stream<Element> stream = docs.removeFirst().stream();
		
		while(!docs.isEmpty()) {
			stream = Stream.concat(stream, docs.removeFirst().stream());
		}
		
		return stream;
	}
	
	private Context extractFromBook(BibleBook book, ContextMetadata wantedContext, ContextType maxDepth) {
		if(!BOOKS.containsKey(book)) {
			return null;
		}

		return new ElementParser().extract(getDocStream(book),
			new Context(ContextMetadata.forBook(book)),
			maxDepth,
			wantedContext
		);
	}

	@Override
	public Context fetch(ContextMetadata wantedContext) {
		if(wantedContext.book != null) {
			// Fetching contents from a specific book.
			return extractFromBook(wantedContext.book, wantedContext, null);
		}
		else if(wantedContext.type == ContextType.BIBLE) {
			// Fetching all available bible contents.
			Context bibleContext = new Context(wantedContext);
			for(BibleBook book: BOOKS.keySet()) {
				bibleContext.addChild(fetch(ContextMetadata.forBook(book)));
			}
			return bibleContext;
		}
		return null;
	}
}