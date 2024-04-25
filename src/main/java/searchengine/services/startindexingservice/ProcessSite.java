package searchengine.services.startindexingservice;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.SiteEntityStatus;
import searchengine.repositories.IndexEntityRepository;
import searchengine.repositories.LemmaEntityRepository;
import searchengine.repositories.PageEntityRepository;
import searchengine.repositories.SiteEntityRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProcessSite {

    private static final Logger logger = LoggerFactory.getLogger(StartIndexingServiceImpl.class);

    private final SiteEntityRepository siteEntityRepository;
    private final SitesList sitesList;
    private final PageEntityRepository pageEntityRepository;
    private final LemmaEntityRepository lemmaEntityRepository;
    private final IndexEntityRepository indexEntityRepository;

    @Transactional
    public void processSite() {
        clearTables();
        createAndSaveSiteEntity();
    }

    private void clearTables() {
        for (Site site : sitesList.getSites()) {
            SiteEntity siteEntity = siteEntityRepository.findByUrl(site.getUrl());
            if (siteEntity != null) {
                List<PageEntity> pageEntityList = pageEntityRepository.findBySiteId(siteEntity);
                for (PageEntity pageEntity : pageEntityList) {
                    indexEntityRepository.deleteByPageId(pageEntity);
                }
                lemmaEntityRepository.deleteBySiteId(siteEntity);
                pageEntityRepository.deleteBySiteId(siteEntity);
                siteEntityRepository.deleteSiteByUrl(site.getUrl());

            }
        }
    }

    private void createAndSaveSiteEntity() {
        List<SiteEntity> listSiteEntitySaveInData = new ArrayList<>();
        List<Site> sitesLists = sitesList.getSites();
        for (Site site : sitesLists) {
            String name = site.getName();
            String url = site.getUrl();

//            deleteSiteAndRelatedPages(url);

            SiteEntity siteEntity = new SiteEntity();
            siteEntity.setUrl(url);
            siteEntity.setName(name);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteEntity.setStatus(SiteEntityStatus.INDEXING);

            listSiteEntitySaveInData.add(siteEntity);
            logger.info("Created SiteEntity " + siteEntity.getName());
        }

        siteEntityRepository.saveAll(listSiteEntitySaveInData);
    }


    private void deleteSiteAndRelatedPages(String url) {
        SiteEntity siteEntity = siteEntityRepository.findByUrl(url);
        if (siteEntity != null) {
//            System.out.println("Вошел в метод удаления");
//            lemmaEntityRepository.deleteAllBySiteId(siteEntity);
//            List<PageEntity> pageEntityList = pageEntityRepository.findBySiteId(siteEntity);
//            for (PageEntity pageEntity : pageEntityList) {
//                indexEntityRepository.deleteByPageId(pageEntity);
//            }
            pageEntityRepository.deleteBySiteId(siteEntity);
            siteEntityRepository.deleteSiteByUrl(siteEntity.getUrl());
            logger.info("delete SiteEntity and Related Pages " + siteEntity.getName());
        }
    }
}
