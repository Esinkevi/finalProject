package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

import java.util.List;

@Repository
public interface PageEntityRepository extends JpaRepository<PageEntity, Long> {

    @Modifying
    @Query("DELETE FROM PageEntity pe WHERE pe.site.id = ?1")
    void deletePagesBySiteId(Long siteId);

    @Modifying
    @Query("DELETE FROM PageEntity pe WHERE pe.site.id IN :siteIds")
    void deletePagesBySiteIds(List<Long> siteIds);

    @Query("SELECT CASE WHEN COUNT(pe) > 0 THEN true ELSE false END FROM PageEntity pe WHERE pe.path = ?1")
    boolean existsByPath(String path);


}
