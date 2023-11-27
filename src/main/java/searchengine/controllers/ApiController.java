package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.startIndexing.IndexingRequest;
import searchengine.dto.startIndexing.IndexingResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.startIndexingService.StartIndexingService;
import searchengine.services.StatisticsService;


@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final StartIndexingService startIndexingService;



    public ApiController(StatisticsService statisticsService, StartIndexingService startIndexingService) {
        this.statisticsService = statisticsService;
        this.startIndexingService = startIndexingService;
    }

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
        return  ResponseEntity.ok(startIndexingService.stopIndexing());
    }

//    @PostMapping("/indexPage")
//    public ResponseEntity<IndexingResponse> indexPage(@RequestBody IndexingRequest indexingRequest){
//        return ResponseEntity.ok(startIndexingService.indexPage(indexingRequest.getUrl()));
//    }





}
