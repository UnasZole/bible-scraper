package com.github.unaszole.bible.implementations;

import com.github.unaszole.bible.CachedDownloader;
import com.github.unaszole.bible.osisbuilder.parser.Context;
import com.github.unaszole.bible.osisbuilder.parser.ContextMetadata;
import com.github.unaszole.bible.osisbuilder.parser.ContextType;
import com.github.unaszole.bible.osisbuilder.parser.Parser;
import com.github.unaszole.bible.osisbuilder.versification.characteristics.VersificationCharacteristics;

import org.crosswire.jsword.versification.BibleBook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Chouraqui implements VersificationCharacteristics {
	
	/*
		Section 1 : Specify all the pages to download.
		For this bible, it's 1 or 2 web pages per book.
	*/
	
	private static final String URL_PREFIX = "https://www.spiritualland.org/Les%20Bibles%20Version%20Html/Bible%20-%20Version%20Chouraqui/";
	
	private static Map<BibleBook, List<String>> linkedHashMapOfEntries(Map.Entry<BibleBook, List<String>>... entries) {
		LinkedHashMap<BibleBook, List<String>> map = new LinkedHashMap<>();
		for(Map.Entry<BibleBook, List<String>> entry: entries) {
			map.put(entry.getKey(), entry.getValue());
		}
		return Collections.unmodifiableMap(map);
	}
	
	private static final Map<BibleBook, List<String>> BOOK_PAGES = linkedHashMapOfEntries(
		// TORA
		Map.entry(BibleBook.GEN, List.of("Bereshit%20%20Genese.html")),
		Map.entry(BibleBook.EXOD, List.of("Noms%20%20Shemot%20Exode.html")),
		Map.entry(BibleBook.LEV, List.of("Il%20crie%20Vayiqra%20%20Levitique.html")),
		Map.entry(BibleBook.NUM, List.of("Au%20desert%20%20Bamidbar%20%20Nombres.html")),
		Map.entry(BibleBook.DEUT, List.of("Paroles%20Debarim%20%20Deuteronome.html")),
		
		// INSPIRES
		Map.entry(BibleBook.JOSH, List.of("Iehoshoua%20%20Josue.html")),
		Map.entry(BibleBook.JUDG, List.of("Suffetes%20%20Shophim%20Juges.html")),
		Map.entry(BibleBook.SAM1, List.of("Shemouel%201%20%20Samuel.html")),
		Map.entry(BibleBook.SAM2, List.of("Shemouel2%20%20Samuel.html")),
		Map.entry(BibleBook.KGS1, List.of("Rois%201%20%20Melakhim%20Rois.html")),
		Map.entry(BibleBook.KGS2, List.of("Rois%202%20Melakhim%20Rois%20.html")),
		Map.entry(BibleBook.ISA, List.of("Iesha%20yahou%20%20Isaie.html")),
		Map.entry(BibleBook.JER, List.of("Irmeyahou%20%20Jeremie.html")),
		Map.entry(BibleBook.EZEK, List.of("Iehezqel%20%20Ezechiel.html")),
		Map.entry(BibleBook.HOS, List.of("Hoshea%20%20Osee.html")),
		Map.entry(BibleBook.JOEL, List.of("Ioel%20Joel.html")),
		Map.entry(BibleBook.AMOS, List.of("Amos.html")),
		Map.entry(BibleBook.OBAD, List.of("Obadyah%20%20Abdias.html")),
		Map.entry(BibleBook.JONAH, List.of("Iona%20%20Jonas.html")),
		Map.entry(BibleBook.MIC, List.of("Liminaire%20pour%20Mikha.html")),
		Map.entry(BibleBook.NAH, List.of("Liminaire%20pour%20Nahoum.html")),
		Map.entry(BibleBook.HAB, List.of("Liminaire%20pour%20Habaqouq.html")),
		Map.entry(BibleBook.ZEPH, List.of("Sephanyah%20%20Sophonie.html")),
		Map.entry(BibleBook.HAG, List.of("Liminaire%20pour%20Hagai.html")),
		Map.entry(BibleBook.ZECH, List.of("Liminaire%20pour%20Zekharyah.html")),
		Map.entry(BibleBook.MAL, List.of("Liminaire%20pour%20Malakhi.html")),
		
		// ECRITS
		Map.entry(BibleBook.PS, List.of("Ketoubim%20%20Hagiographes%20Les%20Psaumes%201.html", "Ketoubim%20%20Hagiographes%20Les%20Psaumes%202.html")),
		Map.entry(BibleBook.PROV, List.of("Mishle%20%20Les%20Proverbes.html")),
		
		// IOB
		Map.entry(BibleBook.SONG, List.of("Shir%20Hashirim%20Le%20Cantique%20des%20Cantiques.html")),
		Map.entry(BibleBook.RUTH, List.of("Rout%20%20Routh%20%20Ruth.html")),
		Map.entry(BibleBook.LAM, List.of("Eikha%20%20Lamentations.html")),
		Map.entry(BibleBook.ECCL, List.of("Qohelet%20%20L%20Ecclesiaste.html")),
		Map.entry(BibleBook.ESTH, List.of("Ester%20%20Esther.html")),
		Map.entry(BibleBook.DAN, List.of("Daniel%20%20Daniel.html")),
		Map.entry(BibleBook.EZRA, List.of("Ezra%20%20Esdras.html")),
		Map.entry(BibleBook.NEH, List.of("Nehemyah%20%20Nehemie.html")),
		Map.entry(BibleBook.CHR1, List.of("Paroles%20des%20Jours%201%20%20.html")),
		Map.entry(BibleBook.CHR2, List.of("Paroles%20des%20Jours%202%20.html")),
		
		// DEUTERO
		Map.entry(BibleBook.TOB, List.of("Liminaire%20pour%20Tobyah.html")),
		Map.entry(BibleBook.JDT, List.of("Liminaire%20pour%20Iehoudit.html")),
		Map.entry(BibleBook.MACC1, List.of("Liminaire%20pour%201%20Hashmonaim.html")),
		Map.entry(BibleBook.MACC2, List.of("Liminaire%20pour%202%20Hashmonaim.html")),
		Map.entry(BibleBook.WIS, List.of("Liminaire%20pour%20Sagesse%20de%20Shelomo.html")),
		Map.entry(BibleBook.SIR, List.of("Liminaire%20pour%20Ben%20Sira.html")),
		Map.entry(BibleBook.BAR, List.of("Liminaire%20pour%20Baroukh.html")),
		Map.entry(BibleBook.EP_JER, List.of("Liminaire%20pour%20la%20lettre%20d%20Irmeyahou.html")),
		Map.entry(BibleBook.ADD_ESTH, List.of("Liminaire%20pour%20ester%20grec.html")),
		/**/Map.entry(BibleBook.PR_AZAR, List.of("Liminaire%20pour%20Daniel%20grec.html")),
		/**/Map.entry(BibleBook.SUS, List.of("Liminaire%20pour%20Daniel%20grec.html")),
		/**/Map.entry(BibleBook.BEL, List.of("Liminaire%20pour%20Daniel%20grec.html")),
		
		// PACTE NEUF
		Map.entry(BibleBook.MATT, List.of("Annonce%20de%20Matyah.html")),
		Map.entry(BibleBook.MARK, List.of("Annonce%20de%20Marcos.html")),
		Map.entry(BibleBook.LUKE, List.of("Annonce%20de%20Loucas.html")),
		Map.entry(BibleBook.JOHN, List.of("Annonce%20de%20Iohanan.html")),
		Map.entry(BibleBook.ACTS, List.of("Gestes%20d%20envoyes.html")),
		Map.entry(BibleBook.ROM, List.of("Lettres%20de%20Paulos.html")),
		Map.entry(BibleBook.COR1, List.of("Premiere%20lettre%20aux%20Corinthiens.html")),
		Map.entry(BibleBook.COR2, List.of("Deuxieme%20lettre%20aux%20Corinthiens.html")),
		Map.entry(BibleBook.GAL, List.of("Lettre%20aux%20Galates.html")),
		Map.entry(BibleBook.EPH, List.of("Lettre%20aux%20ephesiens.html")),
		Map.entry(BibleBook.PHIL, List.of("Lettre%20aux%20Philippiens.html")),
		Map.entry(BibleBook.COL, List.of("Lettre%20aux%20Colossiens.html")),
		Map.entry(BibleBook.THESS1, List.of("Premiere%20lettre%20aux%20Thessaloniciens.html")),
		Map.entry(BibleBook.THESS2, List.of("Deuxieme%20lettre%20aux%20Thessaloniciens.html")),
		Map.entry(BibleBook.TIM1, List.of("Premiere%20lettre%20a%20Timothee.html")),
		Map.entry(BibleBook.TIM2, List.of("Deuxieme%20lettre%20a%20Timotheos.html")),
		Map.entry(BibleBook.TITUS, List.of("Lettre%20a%20Titus.html")),
		Map.entry(BibleBook.PHLM, List.of("Lettre%20a%20Philemon.html")),
		Map.entry(BibleBook.HEB, List.of("Lettre%20aux%20Hebreux.html")),
		Map.entry(BibleBook.JAS, List.of("Lettre%20de%20Ia%20acob.html")),
		Map.entry(BibleBook.PET1, List.of("Lettres%20de%20Petros.html")),
		Map.entry(BibleBook.PET2, List.of("Deuxieme%20lettre%20de%20Petros.html")),
		Map.entry(BibleBook.JOHN1, List.of("Lettres%20de%20Iohanan.html")),
		Map.entry(BibleBook.JOHN2, List.of("Deuxieme%20lettre%20de%20Iohanan.html")),
		Map.entry(BibleBook.JOHN3, List.of("Troisieme%20lettre%20de%20Iohanan.html")),
		Map.entry(BibleBook.JUDE, List.of("Lettre%20de%20Iehouda.html")),
		Map.entry(BibleBook.REV, List.of("Decouvrement%20de%20Iohanan.html"))
	);
	
	private static final List<BibleBook> BOOKS = new ArrayList<BibleBook>(BOOK_PAGES.keySet());
	
	private static URL getUrl(String urlPostfix) {
		try {
			return new URL(URL_PREFIX + urlPostfix);
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static List<URL> getPageUrls(BibleBook book) {
		return BOOK_PAGES.getOrDefault(book, Collections.emptyList())
			.stream()
			.map(p -> getUrl(p))
			.collect(Collectors.toList());
	}
	
	/*
		Section 2 : Specify how to parse all items in a given page.
	*/
	
	private static class ElementParser extends Parser<Element> {
		private final static String CHAPTER_TITLE_REGEXP = "^Chapitre (\\d+)\\.$";
		private final static String S_CHAPTER_TITLE_CONTAINER = ":is(div,p):matches(" + CHAPTER_TITLE_REGEXP + ")";
		private final static String S_NONCHAPTER_STRONG_CONTAINER = "div:not(" + S_CHAPTER_TITLE_CONTAINER + "):has(> strong)";
		private final static String S_SECTION_TITLE_CONTAINER = S_CHAPTER_TITLE_CONTAINER + " ~ " + S_NONCHAPTER_STRONG_CONTAINER;
		private final static String S_BOOK_INTRO_CONTAINER = S_NONCHAPTER_STRONG_CONTAINER + ":not(" + S_CHAPTER_TITLE_CONTAINER + " ~ div)";
		private final static String VERSE_START_REGEXP = "^(\\d+)\\.\\s+(.*)$";
		private final static String S_VERSE_START_CONTAINER = S_CHAPTER_TITLE_CONTAINER + " ~ div:matches(" + VERSE_START_REGEXP + ")";
		private final static String S_VERSE_CONTINUATION_CONTAINER = S_VERSE_START_CONTAINER + " ~ div:not(:is("
			+ S_CHAPTER_TITLE_CONTAINER + ", " + S_SECTION_TITLE_CONTAINER + ", " + S_VERSE_START_CONTAINER + "))";
		
		private final static Evaluator BOOK_TITLE_SELECTOR = QueryParser.parse(".text1titre div strong");
		private final static Evaluator BOOK_INTRO_SELECTOR = QueryParser.parse("table:has(.text1titre) ~ div " + S_BOOK_INTRO_CONTAINER);
		private final static Evaluator CHAPTER_TITLE_SELECTOR = QueryParser.parse("table:has(.text1titre) ~ div " + S_CHAPTER_TITLE_CONTAINER);
		private final static Evaluator SECTION_TITLE_SELECTOR = QueryParser.parse("table:has(.text1titre) ~ div " + S_SECTION_TITLE_CONTAINER);
		private final static Evaluator VERSE_START_SELECTOR = QueryParser.parse("table:has(.text1titre) ~ div " + S_VERSE_START_CONTAINER);
		private final static Evaluator VERSE_CONTINUATION_SELECTOR = QueryParser.parse("table:has(.text1titre) ~ div " + S_VERSE_CONTINUATION_CONTAINER);
		
		private final static Pattern CHAPTER_TITLE_PATTERN = Pattern.compile(CHAPTER_TITLE_REGEXP);
		private final static Pattern VERSE_START_PATTERN = Pattern.compile(VERSE_START_REGEXP);
		
		private static String extract(Pattern pattern, int groupNb, String sourceString) {
			Matcher matcher = pattern.matcher(sourceString);
			if(matcher.matches()) {
				return matcher.group(groupNb);
			}
			throw new RuntimeException(sourceString + " did not match " + pattern);
		}
		
		protected ContextMetadata readContext(ContextMetadata parent, ContextType type, Element e) {
			switch(type) {
				case BOOK:
					return e.is(BOOK_TITLE_SELECTOR) ? ContextMetadata.forBook(parent.book) : null;
				
				case BOOK_TITLE_TEXT:
					return e.is(BOOK_TITLE_SELECTOR) ? ContextMetadata.forBookTitleText(parent.book) : null;
				
				case BOOK_INTRO:
					return e.is(BOOK_INTRO_SELECTOR) ? ContextMetadata.forBookIntro(parent.book) : null;
				
				case BOOK_INTRO_TEXT:
					return e.is(BOOK_INTRO_SELECTOR) ? ContextMetadata.forBookIntroText(parent.book) : null;
				
				case CHAPTER:
					return e.is(CHAPTER_TITLE_SELECTOR) ? ContextMetadata.forChapter(parent.book,
						Integer.valueOf(extract(CHAPTER_TITLE_PATTERN, 1, e.text()))) : null;
				
				case SECTION:
					return e.is(SECTION_TITLE_SELECTOR) ? ContextMetadata.forSection(parent.book,
						parent.chapter) : null;
				
				case VERSE:
					return e.is(VERSE_START_SELECTOR) ? ContextMetadata.forVerse(parent.book, parent.chapter,
						Integer.valueOf(extract(VERSE_START_PATTERN, 1, e.text()))) : null;
				
				case VERSE_TEXT:
					return e.is(VERSE_CONTINUATION_SELECTOR) ? ContextMetadata.forVerseText(parent.book,
						parent.chapter, parent.verse) : null;
				
				default:
					return null;
			}
		}
		
		protected String readContent(ContextMetadata context, Element e) {
			switch(context.type) {
				case BOOK_INTRO:
				case BOOK_INTRO_TEXT:
				case SECTION:
				case VERSE_TEXT:
					return e.text();
				
				case VERSE:
					return extract(VERSE_START_PATTERN, 2, e.text());
				
				default:
					return null;
			}
		}
	}
	
	private final CachedDownloader downloader;
	
	public Chouraqui(Path cachePath) {
		this.downloader = new CachedDownloader(cachePath);
	}
	
	@Override
	public Boolean containsBook(BibleBook book) {
		return BOOKS.contains(book);
	}
	
	@Override
	public Integer getOrdinal(BibleBook book) {
		return BOOKS.indexOf(book);
	}
	
	@Override
	public Integer getLastChapter(BibleBook book) {
		
		int maxChapter = 0;
		
		return maxChapter;
	}
	
	@Override
	public Integer getLastVerse(BibleBook book, int chapter) {
		return null;
	}
	
	private void downloadAll() throws IOException {
		for(BibleBook book: BOOKS) {
			for(URL pageUrl: getPageUrls(book)) {
				downloader.getFile(pageUrl);
			}
		}
	}
	
	private void dumpBookData(BibleBook book) throws Exception {
		for(URL pageUrl: getPageUrls(book)) {
			Path localPath = downloader.getFile(pageUrl);
			
			System.out.println(localPath);
			
			Document doc = Jsoup.parse(localPath.toFile());
			
			Context context = new ElementParser().extract(doc.stream(),
				new ContextMetadata(ContextType.DOCUMENT, book, 0, 0),
				ContextMetadata.forBook(book),
				ContextType.SECTION
			);
			
			System.out.println(context);
			
			
			//System.out.println("Book title : " + doc.select(BOOK_TITLE_SELECTOR).eachText());
			//System.out.println("Book intro : " + doc.select(BOOK_INTRO_SELECTOR).eachText());
			//System.out.println("Chapter title : " + doc.select(CHAPTER_TITLE_SELECTOR).eachText());
			//System.out.println("Section title : " + doc.select(SECTION_TITLE_SELECTOR).eachText());
		}
	}
	
	public static void main(String[] args) throws Exception {
		Path cachePath = Files.createDirectories(Paths.get(args[0]));
		Chouraqui bible = new Chouraqui(cachePath);
		bible.downloadAll();
		
		bible.dumpBookData(BibleBook.valueOf(args[1]));
	}
}