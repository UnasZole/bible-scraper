package com.github.unaszole.bible.scraping.implementations;

import com.github.unaszole.bible.datamodel.*;
import com.github.unaszole.bible.datamodel.contexttypes.BibleContainers;
import com.github.unaszole.bible.datamodel.contexttypes.FlatText;
import com.github.unaszole.bible.datamodel.contexttypes.StructureMarkers;
import com.github.unaszole.bible.datamodel.idtypes.BibleIdFields;
import com.github.unaszole.bible.datamodel.idtypes.BibleIds;
import com.github.unaszole.bible.parsing.Context;
import com.github.unaszole.bible.writing.datamodel.DocumentMetadata;
import com.github.unaszole.bible.downloading.CachedDownloader;
import com.github.unaszole.bible.parsing.Parser;
import com.github.unaszole.bible.parsing.PositionBufferedParserCore;
import com.github.unaszole.bible.stream.ContextStream;
import com.github.unaszole.bible.scraping.*;
import com.github.unaszole.bible.stream.ContextStreamEditor;
import com.github.unaszole.bible.stream.StreamUtils;
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

import static com.github.unaszole.bible.parsing.ContextReaderListBuilder.context;

public class ChouraquiSpiritualLand extends Scraper {

	public static Help getHelp(String[] inputs) {
		return new Help("Bible d'André Chouraqui (spiritualland.org)", List.of(
				Map.entry("variant (optional)", "Variante de versification. " +
						"Indiquer 'Catholic' pour insérer les passages deutérocanoniques d'Esther et Daniel dans leurs chapitres d'origine.")
		), inputs.length == 0 || (inputs.length == 1 && Objects.equals(inputs[0], "Catholic")));
	}
	
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
		BOOKS.put(BibleBook.ESTH, List.of("Ester%20%20Esther.html"));
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
		BOOKS.put(BibleBook.ESTH_GR, List.of("Liminaire%20pour%20ester%20grec.html"));
		BOOKS.put(BibleBook.ADD_DAN, List.of("Liminaire%20pour%20Daniel%20grec.html"));

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
			.map(ChouraquiSpiritualLand::getUrl)
			.collect(Collectors.toList());
	}
	
	private static class ElementParser extends PositionBufferedParserCore<Element> {
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
		
		@Override
		protected List<ContextReader> readContexts(List<Context> ancestors, ContextType type,
									  ContextMetadata previousOfType, Element e) {

            if (type.equals(BibleContainers.BOOK_TITLE)) {
                return e.is(BOOK_TITLE_SELECTOR) && !isBookIntroTitle(e.text())
                        && !isBookGroupTitle(e.text()) ?
                        context(
                                new ContextMetadata(BibleContainers.BOOK_TITLE),
                                context(new ContextMetadata(FlatText.TEXT), e.text())
                        ).build() : List.of();
            } else if (type.equals(BibleContainers.BOOK_INTRO_TITLE)) {
                return e.is(S_BOOK_INTRO_TITLE_CONTAINER) ?
                        context(
                                new ContextMetadata(BibleContainers.BOOK_INTRO_TITLE),
                                context(new ContextMetadata(FlatText.TEXT), e.text())
                        ).build() : List.of();
            } else if (type.equals(BibleContainers.CHAPTER)) {
                if (e.is(CHAPTER_TITLE_SELECTOR)) {
                    String chapterNbStr = extract(CHAPTER_TITLE_PATTERN, 1, e.text());
                    int chapterNb = Integer.parseInt(chapterNbStr);

                    ContextId expectedId = BibleIds.CHAPTER_ID.getNewId(previousOfType, ancestors).get();

                    if (expectedId.get(BibleIdFields.BOOK) == BibleBook.ESTH_GR && chapterNb == 0) {
                        // The first chapter parsed for Esther greek is chapter 0.
                        // That's impossible in OSIS : consider it prefix of chapter 1.
                        chapterNb = 1;
                        chapterNbStr = "0-1";
                    }
                    if (previousOfType != null && chapterNb == previousOfType.id.get(BibleIdFields.CHAPTER)) {
                        // If chapter is the same as previous, ignore.
                        return List.of();
                    }

                    ContextMetadata chapterMeta = ScrapingUtils.forChapter(expectedId.get(BibleIdFields.BOOK), chapterNb);

                    return context(chapterMeta, chapterNbStr).build();
                }


                return e.is(SECTION_TITLE_SELECTOR) ?
                        context(new ContextMetadata(StructureMarkers.SECTION_TITLE),
                                context(new ContextMetadata(FlatText.TEXT), e.text())
                        ).build() : List.of();
            } else if (type.equals(StructureMarkers.SECTION_TITLE)) {
                return e.is(SECTION_TITLE_SELECTOR) ?
                        context(new ContextMetadata(StructureMarkers.SECTION_TITLE),
                                context(new ContextMetadata(FlatText.TEXT), e.text())
                        ).build() : List.of();
            } else if (type.equals(BibleContainers.VERSE)) {
                if (e.is(VERSE_START_SELECTOR)) {
                    String verseNb = extract(VERSE_START_PATTERN, 1, e.text());
                    String verseText = extract(VERSE_START_PATTERN, 2, e.text());

                    ContextMetadata verseMeta = ScrapingUtils.getVerseMetadata(ancestors, previousOfType, verseNb);

                    return context(verseMeta, verseNb,
                            context(new ContextMetadata(FlatText.FLAT_TEXT),
                                    context(new ContextMetadata(FlatText.TEXT), verseText)
                            )
                    ).build();
                }


                if (ScrapingUtils.hasAncestor(BibleContainers.BOOK_INTRO, ancestors)) {
                    return e.is(BOOK_INTRO_BR_SELECTOR) ?
                            context(new ContextMetadata(StructureMarkers.PARAGRAPH_BREAK)).build() : List.of();
                }


                if ("".equals(e.text())) {
                    // Do not create empty text nodes.
                    return List.of();
                }

                if (ScrapingUtils.hasAncestor(BibleContainers.BOOK_INTRO, ancestors)
                        && !ScrapingUtils.hasAncestor(BibleContainers.BOOK_INTRO_TITLE, ancestors)
                        && ScrapingUtils.hasAncestor(FlatText.FLAT_TEXT, ancestors)) {
                    // Trying to find flat text within a book intro.
                    return e.is(BOOK_INTRO_SELECTOR) ?
                            context(new ContextMetadata(FlatText.TEXT), e.text()).build() : List.of();
                } else if (ScrapingUtils.isInVerseText(ancestors)) {
                    // Trying to find additional text within a verse.
                    return e.is(VERSE_CONTINUATION_SELECTOR) ?
                            context(new ContextMetadata(FlatText.TEXT), " " + e.text()).build() : List.of();
                }


                return List.of();
            } else if (type.equals(StructureMarkers.PARAGRAPH_BREAK)) {
                if (ScrapingUtils.hasAncestor(BibleContainers.BOOK_INTRO, ancestors)) {
                    return e.is(BOOK_INTRO_BR_SELECTOR) ?
                            context(new ContextMetadata(StructureMarkers.PARAGRAPH_BREAK)).build() : List.of();
                }


                if ("".equals(e.text())) {
                    // Do not create empty text nodes.
                    return List.of();
                }

                if (ScrapingUtils.hasAncestor(BibleContainers.BOOK_INTRO, ancestors)
                        && !ScrapingUtils.hasAncestor(BibleContainers.BOOK_INTRO_TITLE, ancestors)
                        && ScrapingUtils.hasAncestor(FlatText.FLAT_TEXT, ancestors)) {
                    // Trying to find flat text within a book intro.
                    return e.is(BOOK_INTRO_SELECTOR) ?
                            context(new ContextMetadata(FlatText.TEXT), e.text()).build() : List.of();
                } else if (ScrapingUtils.isInVerseText(ancestors)) {
                    // Trying to find additional text within a verse.
                    return e.is(VERSE_CONTINUATION_SELECTOR) ?
                            context(new ContextMetadata(FlatText.TEXT), " " + e.text()).build() : List.of();
                }


                return List.of();
            } else if (type.equals(FlatText.TEXT)) {
                if ("".equals(e.text())) {
                    // Do not create empty text nodes.
                    return List.of();
                }

                if (ScrapingUtils.hasAncestor(BibleContainers.BOOK_INTRO, ancestors)
                        && !ScrapingUtils.hasAncestor(BibleContainers.BOOK_INTRO_TITLE, ancestors)
                        && ScrapingUtils.hasAncestor(FlatText.FLAT_TEXT, ancestors)) {
                    // Trying to find flat text within a book intro.
                    return e.is(BOOK_INTRO_SELECTOR) ?
                            context(new ContextMetadata(FlatText.TEXT), e.text()).build() : List.of();
                } else if (ScrapingUtils.isInVerseText(ancestors)) {
                    // Trying to find additional text within a verse.
                    return e.is(VERSE_CONTINUATION_SELECTOR) ?
                            context(new ContextMetadata(FlatText.TEXT), " " + e.text()).build() : List.of();
                }


                return List.of();
            }
            return List.of();
        }
		
		//
		// External parsing to handle badly formatted pages like the end of the Gospel of Matthew.
		//
		
		private final static String UNFORMATTED_VERSE_REGEXP = "(\\d+)\\s+([^\\d]*)";
		private final static String S_UNFORMATTED_CHAPTER_CONTENT = S_CHAPTER_TITLE_CONTAINER + " + p:matches(^(" + UNFORMATTED_VERSE_REGEXP + ")+$)";
		private final static Evaluator UNFORMATTED_CHAPTER_CONTENT_SELECTOR = QueryParser.parse(S_UNFORMATTED_CHAPTER_CONTENT + ":not(.text1titre *)");
		private final static Pattern UNFORMATTED_VERSE_PATTERN = Pattern.compile(UNFORMATTED_VERSE_REGEXP);
		
		private static class UnformattedChapterParser extends PositionBufferedParserCore<MatchResult> {

			@Override
			protected List<ContextReader> readContexts(List<Context> ancestors, ContextType type,
										  ContextMetadata previousOfType, MatchResult verseMatch) {
                if (type.equals(BibleContainers.VERSE)) {
                    String verseNb = verseMatch.group(1);
                    ContextId expectedVerseId = BibleIds.VERSE_ID.getNewId(
                            previousOfType, ancestors
                    ).orElseThrow(() -> new IllegalStateException("plop"));

                    ContextMetadata verseMeta = ScrapingUtils.forVerse(
                            expectedVerseId.get(BibleIdFields.BOOK),
                            expectedVerseId.get(BibleIdFields.CHAPTER),
                            Integer.parseInt(verseNb)
                    );

                    return context(verseMeta, verseNb,
                            context(new ContextMetadata(FlatText.FLAT_TEXT),
                                    context(new ContextMetadata(FlatText.TEXT), verseMatch.group(2))
                            )
                    ).build();
                }
				return List.of();
			}
		}
		
		@Override
		public Parser<?> parseExternally(Element e, Deque<Context> currentContextStack) {
			if(ScrapingUtils.hasAncestor(BibleContainers.CHAPTER, currentContextStack) && e.is(UNFORMATTED_CHAPTER_CONTENT_SELECTOR)) {
				// If lexeme matches as an unformatted sequence of verses, we parse them and pass them to a dedicated parser.
				Matcher verseMatcher = UNFORMATTED_VERSE_PATTERN.matcher(e.text());
				return new Parser<>(new UnformattedChapterParser(), verseMatcher.results().iterator(), currentContextStack);
			}
			return null;
		}
	}
	
	private final CachedDownloader downloader;
	private final Variant variant;
	
	public ChouraquiSpiritualLand(Path cachePath, String[] inputs) throws IOException {
		this.downloader = new CachedDownloader(cachePath.resolve("ChouraquiSpiritualLand"));
		if(inputs.length >= 1 && Objects.equals(inputs[0], "Catholic")) {
			this.variant = new CatholicVariant();
		}
		else {
			this.variant = new DefaultVariant();
		}
	}
	
	private Document getDoc(URL url) {
		try {
			return Jsoup.parse(downloader.getFile(url).toFile());
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Stream<Element> getDocStream(BibleBook book) {
		return StreamUtils.concatStreams(getPageUrls(book).stream()
				.map(url -> StreamUtils.deferredStream(
						() -> getDoc(url).stream()
				))
				.collect(Collectors.toList())
		);
	}

	private interface Variant {
		String getRefSystem();
		BibleBook getMappedBook(BibleBook books);
		ContextStreamEditor<ContextStream.Single> editBook(BibleBook book, ContextStreamEditor<ContextStream.Single> editor);
	}

	private static class DefaultVariant implements Variant {
		@Override
		public String getRefSystem() {
			return "Bible";
		}

		@Override
		public BibleBook getMappedBook(BibleBook book) {
			// Keep all books as-is.
			return book;
		}

		@Override
		public ContextStreamEditor<ContextStream.Single> editBook(BibleBook book, ContextStreamEditor<ContextStream.Single> editor) {
			// No change.
			return editor;
		}
	}

	private class CatholicVariant implements Variant {
		@Override
		public String getRefSystem() {
			return "Bible.Catholic";
		}

		@Override
		public BibleBook getMappedBook(BibleBook book) {
			switch(book) {
				// Book of Esther is replaced by the Greek version.
				case ESTH:
					return BibleBook.ESTH_GR;
				// Greek Esther and Daniel are removed (Daniel additions are included in the main book)
				case ESTH_GR:
				case ADD_DAN:
					return null;
				// Other books are included.
				default:
					return book;
			}
		}

		@Override
		public ContextStreamEditor<ContextStream.Single> editBook(BibleBook book, ContextStreamEditor<ContextStream.Single> editor) {
			// Book of Daniel
			if(book == BibleBook.DAN) {
				// Chapter 3 : Inject deuterocanonical additions after verse 23, updating the OSIS book reference.
				editor.inject(ContextStreamEditor.InjectionPosition.AFTER, ScrapingUtils.forVerse(BibleBook.DAN, 3, 23),
						getBookStream(BibleBook.ADD_DAN)
								.extractStream(
										ScrapingUtils.forVerse(BibleBook.ADD_DAN, 3, 24),
										ScrapingUtils.forVerse(BibleBook.ADD_DAN, 3, 91)
								).edit().mergeSiblings(
										ScrapingUtils.forVerse(BibleBook.ADD_DAN, 3, 90),
										ScrapingUtils.forVerse(BibleBook.ADD_DAN, 3, 91)
								).process()
								.edit().updateContextsUntilTheEnd(
										new VersificationUpdater().book(m -> BibleBook.DAN)
								).process()
				);
				// Chapter 3 : all verses after the deuterocanonical additions are shifted by 67.
				editor.updateContexts(
						ScrapingUtils.forVerse(BibleBook.DAN, 3, 24),
						ScrapingUtils.forVerse(BibleBook.DAN, 3, 33),
						new VersificationUpdater()
								.verseNbs(m -> m.id.get(BibleIdFields.VERSES).stream()
										.map(v -> v + 67)
										.collect(Collectors.toList()))
								.verseValue(m -> m.id.get(BibleIdFields.VERSES).stream()
										.map(v -> v + 67)
										.map(Object::toString)
										.collect(Collectors.joining("-")))
				);
				// Chapters 13 and 14 from deuterocanonical additions are appended, updating the OSIS book reference.
				editor.inject(
						ContextStreamEditor.InjectionPosition.AT_END, ScrapingUtils.forBook(BibleBook.DAN),
						getBookStream(BibleBook.ADD_DAN).extractStream(ScrapingUtils.forChapter(BibleBook.ADD_DAN, 13))
								.edit().updateContextsUntilTheEnd(
										new VersificationUpdater().book(m -> BibleBook.DAN)
								).process(),
						getBookStream(BibleBook.ADD_DAN).extractStream(ScrapingUtils.forChapter(BibleBook.ADD_DAN, 14))
								.edit().updateContextsUntilTheEnd(
										new VersificationUpdater().book(m -> BibleBook.DAN)
								).process()
				);
			}
			else if(book == BibleBook.ESTH_GR) {
				// Move to chapter 5, verse 2B. Remove the second part of verse 2B, and verse 2C, which should not be
				// in the Greek version of Esther.
				editor.doNothingUntil(ContextStreamEditor.InjectionPosition.AT_START,
						(m, v) -> m.type == BibleContainers.VERSE && m.id.get(BibleIdFields.CHAPTER) == 5 && Objects.equals(v, "2B")
				);
				editor.remove(
						(m, v) -> m.type == FlatText.TEXT && ((String)v).startsWith(" Èstér revêt"),
						(m, v) -> m.type == FlatText.TEXT && ((String)v).endsWith("la maison.")
				);
				editor.remove((m, v) -> m.type == BibleContainers.VERSE && m.id.get(BibleIdFields.CHAPTER) == 5 && Objects.equals(v, "2C"));

				// Shift chapter 9 verses 20 to 32 by 1, but not touching the values.
				// (In catholic bibles, a verse 19A is present, which is missing here.)
				editor.updateContexts(
						ScrapingUtils.forVerse(BibleBook.ESTH_GR, 9, 20),
						ScrapingUtils.forVerse(BibleBook.ESTH_GR, 9, 32),
						new VersificationUpdater()
								.verseNbs(m -> m.id.get(BibleIdFields.VERSES).stream()
										.map(v -> v + 1)
										.collect(Collectors.toList()))
				);
			}

			return editor;
		}
	}

	public List<BibleBook> getBookList() {
		return new ArrayList<>(BOOKS.keySet());
	}

	private ContextStream.Single getBookStream(BibleBook book) {
		Context bookCtx = new Context(ScrapingUtils.forBook(book), book.getOSIS());
		return variant.editBook(book,
				new Parser.TerminalParser<>(new ElementParser(), getDocStream(book).iterator(), bookCtx).asContextStream().edit()
		).process();
	}

	@Override
	public DocumentMetadata getMeta() {
		return new DocumentMetadata(Locale.FRENCH, "freCHUsl", "Bible d'André Chouraqui", variant.getRefSystem());
	}

	@Override
	public ContextStream.Single getContextStreamFor(ContextMetadata rootContextMeta) {
		switch ((BibleContainers) rootContextMeta.type) {
			case BOOK:
				BibleBook mappedBook = variant.getMappedBook(rootContextMeta.id.get(BibleIdFields.BOOK));
				if(mappedBook == null) {
					// Book is removed from the variant. Return an empty stream
					return new ContextStream.Single(rootContextMeta, Stream.of());
				}
				else {
					if(mappedBook != rootContextMeta.id.get(BibleIdFields.BOOK)) {
						// Book is remapped : update versification of its contents.
						return getBookStream(mappedBook).edit().updateContextsUntilTheEnd(
								new VersificationUpdater().book(b-> rootContextMeta.id.get(BibleIdFields.BOOK))
						).process();
					}
					else {
						return getBookStream(rootContextMeta.id.get(BibleIdFields.BOOK));
					}
				}
			case BIBLE:
				return autoGetBibleStream(getBookList());
		}
		return null;
	}
}