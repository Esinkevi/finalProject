package searchengine.dto.search;

import lombok.Getter;
import lombok.Setter;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;

import java.util.List;

@Getter
@Setter
public class RelevanceDataDto {

    private PageEntity pageEntity;

    private List<LemmaEntity> lemmaEntityList;

    private float absRelevance;
    
}
