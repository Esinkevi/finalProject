package searchengine.services.startIndexingService;

import searchengine.dto.startIndexing.IndexingResponse;

public interface StartIndexingService {

    IndexingResponse startIndexing();

    IndexingResponse stopIndexing();

    IndexingResponse indexPage(String url);
}
