package searchengine.dto.startindexing;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
public class IndexingResponse {

    private boolean result;
    private String error;
}
