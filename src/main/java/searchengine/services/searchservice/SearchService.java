package searchengine.services.searchservice;

import searchengine.dto.search.SearchRequest;
import searchengine.dto.search.SearchResponseDto;

public interface SearchService {

    SearchResponseDto getSearchResult(SearchRequest request);
}
