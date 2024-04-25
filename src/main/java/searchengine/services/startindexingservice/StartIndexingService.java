package searchengine.services.startindexingservice;

import searchengine.dto.startindexing.IndexingResponse;

public interface StartIndexingService {

    IndexingResponse startIndexing();

    IndexingResponse stopIndexing();

    IndexingResponse indexOnePage(String url);
}
