package searchengine.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;
import searchengine.model.SiteEntityStatus;

import java.time.LocalDateTime;

@Repository
public interface SiteEntityRepository extends JpaRepository<SiteEntity, Long> {

    void deleteSiteByUrl(String url);

    SiteEntity findByUrl(String url);

    @Modifying
    @Transactional
    @Query("UPDATE SiteEntity s SET s.status = :status, s.statusTime = :statusTime, s.lastErrorText = COALESCE(:lastErrorText, s.lastErrorText) WHERE s.id = :siteId")
    void updateSiteStatus(
            @Param("siteId") Long siteId,
            @Param("status") SiteEntityStatus status,
            @Param("statusTime") LocalDateTime statusTime,
            @Param("lastErrorText") String lastErrorText
    );

}
