package searchengine.dto.startIndexing;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class IndexingResponse {

    private boolean result;
    private String error;
}
