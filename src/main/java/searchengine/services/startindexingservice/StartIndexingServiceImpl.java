package searchengine.services.startIndexingService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.dto.startIndexing.IndexingResponse;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class StartIndexingServiceImpl implements StartIndexingService {


    private boolean indexingInProgress;

    private final IndexingResponse indexingResponse;
    private final ProcessSiteEntity processSiteEntity;
    private final ProcessPageEntity processPageEntity;


    @Override
    public IndexingResponse startIndexing() {
        if (indexingInProgress) {
            indexingResponse.setResult(false);
            indexingResponse.setError("Индексация уже запущена");
            return indexingResponse;
        }

        indexingInProgress = true;

        CompletableFuture.runAsync(() -> {
            processSiteEntity.processSite();
            processPageEntity.processPage();
        });

        indexingResponse.setResult(true);
        return indexingResponse;
    }

    @Override
    public IndexingResponse stopIndexing() {
        if (!indexingInProgress) {
            indexingResponse.setResult(false);
            indexingResponse.setError("Индексация не выполняется");
            return indexingResponse;
        }

        CrawlSite.interrupt();

        indexingResponse.setResult(true);
        indexingResponse.setError("");
        return indexingResponse;
    }

    @Override
    public IndexingResponse indexPage(String url) {
        IndexingResponse indexingResponse = new IndexingResponse();
        if (!isValidUrl(url)) {
            indexingResponse.setResult(false);
            indexingResponse.setError("Данная страница находится за пределами сайтов," + "указанных в конфигурационном файле");
            return indexingResponse;
        }

        indexingResponse.setResult(true);

//        processIndexing.processIndexingPage(url);


        return indexingResponse;
    }

    private boolean isValidUrl(String url) {
        return false;
    }

}
