package searchengine.dto.startIndexing;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class IndexingRequest {

    private String url;
}
