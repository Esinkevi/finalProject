package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.search.SearchRequest;
import searchengine.dto.search.SearchResponseDto;
import searchengine.dto.startindexing.IndexingResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.searchservice.SearchService;
import searchengine.services.startindexingservice.StartIndexingService;
import searchengine.services.StatisticsService;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final StartIndexingService startIndexingService;
    private final SearchService searchService;


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing() {
        return ResponseEntity.ok(startIndexingService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing() {
        return ResponseEntity.ok(startIndexingService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> indexPage(@RequestParam("url") String url) {
        return ResponseEntity.ok(startIndexingService.indexOnePage(url));
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponseDto> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String site,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setSite(site);
        request.setOffset(offset);
        request.setLimit(limit);
        return ResponseEntity.ok(searchService.getSearchResult(request));
    }
}
