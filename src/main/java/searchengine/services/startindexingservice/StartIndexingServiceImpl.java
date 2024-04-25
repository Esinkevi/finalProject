package searchengine.services.startindexingservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.startindexing.IndexingResponse;


import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class StartIndexingServiceImpl implements StartIndexingService {


    private boolean indexingInProgress;

    private final ProcessSite processSite;
    private final ProcessPage processPage;



    @Override
    public IndexingResponse startIndexing() {
        IndexingResponse indexingResponse = new IndexingResponse();
        if (indexingInProgress) {
            indexingResponse.setResult(false);
            indexingResponse.setError("Индексация уже запущена");
            return indexingResponse;
        }
        indexingInProgress = true;

        CompletableFuture.runAsync(() -> {
            processSite.processSite();
            processPage.processPages();
        });

        indexingResponse.setResult(true);
        return indexingResponse;
    }

    @Override
    public IndexingResponse stopIndexing() {
        IndexingResponse indexingResponse = new IndexingResponse();
        if (!indexingInProgress) {
            indexingResponse.setResult(false);
            indexingResponse.setError("Индексация не выполняется");
            return indexingResponse;
        }

        IndexingSite.stopProcessing();

        indexingResponse.setResult(true);
        indexingResponse.setError("");
        indexingInProgress = false;
        return indexingResponse;
    }

    @Override
    public IndexingResponse indexOnePage(String url) {
        IndexingResponse indexingResponse = new IndexingResponse();

        if (!processPage.processOnePage(url)) {
            indexingResponse.setResult(false);
            indexingResponse.setError("Данная страница находится за пределами сайтов," + "указанных в конфигурационном файле");
            System.out.println(url);
            return indexingResponse;
        }

        indexingResponse.setResult(true);


        return indexingResponse;
    }

}
