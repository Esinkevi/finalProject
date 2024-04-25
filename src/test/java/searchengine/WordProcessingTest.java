package searchengine;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import searchengine.Application;
import searchengine.model.PageEntity;
import searchengine.repositories.PageEntityRepository;
import searchengine.services.startindexingservice.WordProcessing;

import static org.junit.jupiter.api.Assertions.assertFalse;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WordProcessingTest {


    @Test
    public void testClearingTextFromHtmlCode() {
        WordProcessing wordProcessing = new WordProcessing();
        String url = "https://www.playback.ru/basket.html";
        try {
            Connection.Response response = Jsoup.connect(url).execute();
            String htmlText = response.body();
            Map<String,Integer> words = wordProcessing.processingWord(htmlText);
            for (Map.Entry<String, Integer> s : words.entrySet()){
                String key = s.getKey();
                int value = s.getValue();
                System.out.println("key: " + key  + "," +  " value: " + value);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }




    }
}
