package com.github.unaszole.bible.implementations;

import com.github.unaszole.bible.CachedDownloader;

import org.crosswire.jsword.versification.BibleBook;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.net.URL;
import java.util.Map;

public class Chouraqui {
	
	private static final String URL_PREFIX = "https://www.spiritualland.org/Les%20Bibles%20Version%20Html/Bible%20-%20Version%20Chouraqui/";
	
	private static final Map<BibleBook, String> FULL_BOOK_PAGES = Map.ofEntries(
		// TORA
		Map.entry(BibleBook.GEN, "Bereshit%20%20Genese.html"),
		Map.entry(BibleBook.EXOD, "Noms%20%20Shemot%20Exode.html"),
		Map.entry(BibleBook.LEV, "Il%20crie%20Vayiqra%20%20Levitique.html"),
		Map.entry(BibleBook.NUM, "Au%20desert%20%20Bamidbar%20%20Nombres.html"),
		Map.entry(BibleBook.DEUT, "Paroles%20Debarim%20%20Deuteronome.html"),
		
		// INSPIRES
		Map.entry(BibleBook.JOSH, "Iehoshoua%20%20Josue.html"),
		Map.entry(BibleBook.JUDG, "Suffetes%20%20Shophim%20Juges.html"),
		Map.entry(BibleBook.SAM1, "Shemouel%201%20%20Samuel.html"),
		Map.entry(BibleBook.SAM2, "Shemouel2%20%20Samuel.html"),
		Map.entry(BibleBook.KGS1, "Rois%201%20%20Melakhim%20Rois.html"),
		Map.entry(BibleBook.KGS2, "Rois%202%20Melakhim%20Rois%20.html"),
		Map.entry(BibleBook.ISA, "Iesha%20yahou%20%20Isaie.html"),
		Map.entry(BibleBook.JER, "Irmeyahou%20%20Jeremie.html"),
		Map.entry(BibleBook.EZEK, "Iehezqel%20%20Ezechiel.html"),
		Map.entry(BibleBook.HOS, "Hoshea%20%20Osee.html"),
		Map.entry(BibleBook.JOEL, "Ioel%20Joel.html"),
		Map.entry(BibleBook.AMOS, "Amos.html"),
		Map.entry(BibleBook.OBAD, "Obadyah%20%20Abdias.html"),
		Map.entry(BibleBook.JONAH, "Iona%20%20Jonas.html"),
		Map.entry(BibleBook.MIC, "Liminaire%20pour%20Mikha.html"),
		Map.entry(BibleBook.NAH, "Liminaire%20pour%20Nahoum.html"),
		Map.entry(BibleBook.HAB, "Liminaire%20pour%20Habaqouq.html"),
		Map.entry(BibleBook.ZEPH, "Sephanyah%20%20Sophonie.html"),
		Map.entry(BibleBook.HAG, "Liminaire%20pour%20Hagai.html"),
		Map.entry(BibleBook.ZECH, "Liminaire%20pour%20Zekharyah.html"),
		Map.entry(BibleBook.MAL, "Liminaire%20pour%20Malakhi.html"),
		
		// ECRITS
		/**/Map.entry(BibleBook.PS, "Ketoubim%20%20Hagiographes%20Les%20Psaumes%201.html"),
		Map.entry(BibleBook.PROV, "Mishle%20%20Les%20Proverbes.html"),
		
		// IOB
		Map.entry(BibleBook.SONG, "Shir%20Hashirim%20Le%20Cantique%20des%20Cantiques.html"),
		Map.entry(BibleBook.RUTH, "Rout%20%20Routh%20%20Ruth.html"),
		Map.entry(BibleBook.LAM, "Eikha%20%20Lamentations.html"),
		Map.entry(BibleBook.ECCL, "Qohelet%20%20L%20Ecclesiaste.html"),
		Map.entry(BibleBook.ESTH, "Ester%20%20Esther.html"),
		Map.entry(BibleBook.DAN, "Daniel%20%20Daniel.html"),
		Map.entry(BibleBook.EZRA, "Ezra%20%20Esdras.html"),
		Map.entry(BibleBook.NEH, "Nehemyah%20%20Nehemie.html"),
		Map.entry(BibleBook.CHR1, "Paroles%20des%20Jours%201%20%20.html"),
		Map.entry(BibleBook.CHR2, "Paroles%20des%20Jours%202%20.html"),
		
		// DEUTERO
		Map.entry(BibleBook.TOB, "Liminaire%20pour%20Tobyah.html"),
		Map.entry(BibleBook.JDT, "Liminaire%20pour%20Iehoudit.html"),
		Map.entry(BibleBook.MACC1, "Liminaire%20pour%201%20Hashmonaim.html"),
		Map.entry(BibleBook.MACC2, "Liminaire%20pour%202%20Hashmonaim.html"),
		Map.entry(BibleBook.WIS, "Liminaire%20pour%20Sagesse%20de%20Shelomo.html"),
		Map.entry(BibleBook.SIR, "Liminaire%20pour%20Ben%20Sira.html"),
		Map.entry(BibleBook.BAR, "Liminaire%20pour%20Baroukh.html"),
		Map.entry(BibleBook.EP_JER, "Liminaire%20pour%20la%20lettre%20d%20Irmeyahou.html"),
		Map.entry(BibleBook.ADD_ESTH, "Liminaire%20pour%20ester%20grec.html"),
		/**/Map.entry(BibleBook.PR_AZAR, "Liminaire%20pour%20Daniel%20grec.html"),
		/**/Map.entry(BibleBook.SUS, "Liminaire%20pour%20Daniel%20grec.html"),
		/**/Map.entry(BibleBook.BEL, "Liminaire%20pour%20Daniel%20grec.html"),
		
		// PACTE NEUF
		Map.entry(BibleBook.MATT, "Annonce%20de%20Matyah.html"),
		Map.entry(BibleBook.MARK, "Annonce%20de%20Marcos.html"),
		Map.entry(BibleBook.LUKE, "Annonce%20de%20Loucas.html"),
		Map.entry(BibleBook.JOHN, "Annonce%20de%20Iohanan.html"),
		Map.entry(BibleBook.ACTS, "Gestes%20d%20envoyes.html"),
		Map.entry(BibleBook.ROM, "Lettres%20de%20Paulos.html"),
		Map.entry(BibleBook.COR1, "Premiere%20lettre%20aux%20Corinthiens.html"),
		Map.entry(BibleBook.COR2, "Deuxieme%20lettre%20aux%20Corinthiens.html"),
		Map.entry(BibleBook.GAL, "Lettre%20aux%20Galates.html"),
		Map.entry(BibleBook.EPH, "Lettre%20aux%20ephesiens.html"),
		Map.entry(BibleBook.PHIL, "Lettre%20aux%20Philippiens.html"),
		Map.entry(BibleBook.COL, "Lettre%20aux%20Colossiens.html"),
		Map.entry(BibleBook.THESS1, "Premiere%20lettre%20aux%20Thessaloniciens.html"),
		Map.entry(BibleBook.THESS2, "Deuxieme%20lettre%20aux%20Thessaloniciens.html"),
		Map.entry(BibleBook.TIM1, "Premiere%20lettre%20a%20Timothee.html"),
		Map.entry(BibleBook.TIM2, "Deuxieme%20lettre%20a%20Timotheos.html"),
		Map.entry(BibleBook.TITUS, "Lettre%20a%20Titus.html"),
		Map.entry(BibleBook.PHLM, "Lettre%20a%20Philemon.html"),
		Map.entry(BibleBook.HEB, "Lettre%20aux%20Hebreux.html"),
		Map.entry(BibleBook.JAS, "Lettre%20de%20Ia%20acob.html"),
		Map.entry(BibleBook.PET1, "Lettres%20de%20Petros.html"),
		Map.entry(BibleBook.PET2, "Deuxieme%20lettre%20de%20Petros.html"),
		Map.entry(BibleBook.JOHN1, "Lettres%20de%20Iohanan.html"),
		Map.entry(BibleBook.JOHN2, "Deuxieme%20lettre%20de%20Iohanan.html"),
		Map.entry(BibleBook.JOHN3, "Troisieme%20lettre%20de%20Iohanan.html"),
		Map.entry(BibleBook.JUDE, "Lettre%20de%20Iehouda.html"),
		Map.entry(BibleBook.REV, "Decouvrement%20de%20Iohanan.html")
	);
	
	
	public static void main(String[] args) throws Exception {
		Path cachePath = Files.createDirectories(Paths.get(args[0]));
		CachedDownloader downloader = new CachedDownloader(cachePath);
		for(String url: FULL_BOOK_PAGES.values()) {
			downloader.getFile(new URL(URL_PREFIX + url));
		}
	}
}