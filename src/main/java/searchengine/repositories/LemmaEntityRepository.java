package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.List;

@Repository
public interface LemmaEntityRepository extends JpaRepository<LemmaEntity,Long> {

    LemmaEntity findBySiteIdAndLemma(SiteEntity siteId, String lemma);

    void deleteBySiteId(SiteEntity siteEntity);


    @Query("SELECT SUM(l.frequency) FROM LemmaEntity l WHERE l.siteId = :siteId")
    Long sumFrequencyBySiteId(@Param("siteId") SiteEntity siteId);

    @Query("SELECT SUM(l.frequency) FROM LemmaEntity l")
    long sumAllFrequency();

    List<LemmaEntity> findByLemma(String lemma);


}
