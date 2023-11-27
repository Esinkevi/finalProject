package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

import java.util.List;

@Repository
public interface PageEntityRepository extends JpaRepository<PageEntity, Long> {

    void deleteBySite_Id(Long siteId);

    boolean existsByPath(String path);


}
