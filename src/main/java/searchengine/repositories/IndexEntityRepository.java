package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;

import java.util.List;

public interface IndexEntityRepository extends JpaRepository<IndexEntity, Long> {

    void deleteByPageId(PageEntity pageId);

    List<IndexEntity> findByPageId(PageEntity pageId);


    List<IndexEntity> findByLemmaId(LemmaEntity lemmaEntity);
//
//    IndexEntity findByPageIdAndLemmaId(PageEntity pageEntity, LemmaEntity lemmaEntity);
}
