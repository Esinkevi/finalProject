package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageEntityRepository extends JpaRepository<PageEntity, Long> {

    void deleteBySiteId(SiteEntity site);

    boolean existsByPath(String path);

    PageEntity findBySiteIdAndPath(SiteEntity siteId, String path);

    List<PageEntity> findBySiteId(SiteEntity siteEntity);

    boolean existsByPathAndSiteId(String path, SiteEntity siteId);
    long countBySiteId(SiteEntity siteId);
//
    PageEntity findById(long pageId);
//
//    List<PageEntity> findByPageId(IndexEntity pageId);

}
