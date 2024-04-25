package searchengine.dto.search;

import java.util.List;

@lombok.Data
public class SearchResponseDto {
    private boolean result;
    private int count;
    private Data[] data;
    private String error;
}
