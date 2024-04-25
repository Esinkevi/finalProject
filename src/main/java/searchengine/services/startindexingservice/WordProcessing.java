package searchengine.services.startindexingservice;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class WordProcessing {

    private static final LuceneMorphology luceneMorphology;

    static {
        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public Map<String, Integer> processingWord(String text) {

        Map<String, Integer> lemmasList = getLemmas(text);

        return lemmasList;
    }

    private Map<String, Integer> getLemmas(String text) {
        String[] arrayWords = clearingTextFromHtmlCode(text);
        Map<String, Integer> lemmasList = new HashMap<>();

        for (String word : arrayWords) {
            String wordLowerCase = word.toLowerCase(Locale.ROOT);

            if (luceneMorphology.checkString(wordLowerCase) && clearingPartOfSpeech(wordLowerCase)) {
                String normalForms = getNormalForms(wordLowerCase);

                if (lemmasList.containsKey(normalForms)) {
                    int currentValue = lemmasList.get(normalForms);
                    lemmasList.put(normalForms, currentValue + 1);
                } else {
                    lemmasList.put(normalForms, 1);
                }
            }
        }
        return lemmasList;
    }

    private String getNormalForms(String word) {
        String normalForms = "";
        List<String> wordBaseForm = luceneMorphology.getNormalForms(word);
        if (wordBaseForm.size() > 1) {
            for (String baseForm : wordBaseForm) {
                normalForms += baseForm + " ";
            }
            return normalForms.trim();
        }
        return wordBaseForm.get(0).trim();
    }

    private boolean clearingPartOfSpeech(String word) {
        Set<String> excludedPartsOfSpeech = new HashSet<>(Arrays.asList("МЕЖД", "СОЮЗ", "ПРЕДЛ"));

        List<String> words = luceneMorphology.getMorphInfo(word);
        for (String part : words) {

            String[] partOfSearch = part.split("\\|");
            String[] secondParts = partOfSearch[1].trim().split("\\s+");
            String partOfSpeech = secondParts[1];

            if (!excludedPartsOfSpeech.contains(partOfSpeech)) {
                return true;
            }

        }
        return false;
    }


    private String[] clearingTextFromHtmlCode(String text) {
        Document document = Jsoup.parse(text);
        String[] arrayWords = document.text().split("[,\\s]+");
        return arrayWords;
    }
}
