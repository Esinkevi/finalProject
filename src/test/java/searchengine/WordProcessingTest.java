import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import searchengine.Application;
import searchengine.model.PageEntity;
import searchengine.repositories.PageEntityRepository;
import searchengine.services.startindexingservice.WordProcessing;

import java.util.Map;
import java.util.Optional;

@SpringBootTest(classes = Application.class)

public class WordProcessingTest {

    @Autowired
    private  PageEntityRepository pageEntityRepository;

    @Test
    public void testClearingTextFromHtmlCode(){
        Long pageId = 1L;
        Optional<PageEntity> pageEntityOptional = pageEntityRepository.findById(pageId);
        if (pageEntityOptional.isPresent()){
            PageEntity pageEntity = pageEntityOptional.get();
            String htmlText = pageEntity.getContent();
            WordProcessing wordProcessing = new WordProcessing(htmlText);
            Map<String, Integer> result = wordProcessing.processingWord();

            for (Map.Entry<String, Integer> entry : result.entrySet()) {
                System.out.println("Word: " + entry.getKey() + ", Count: " + entry.getValue());
            }
            System.out.println(result.size());
        } else {
            System.out.println("Page not found for id: " + pageId);
        }



    }
}
