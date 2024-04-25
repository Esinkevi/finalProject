package searchengine.services.startindexingservice;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteEntity;
import searchengine.model.SiteEntityStatus;
import searchengine.repositories.PageEntityRepository;
import searchengine.repositories.SiteEntityRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class ProcessSiteEntity {

    private static final Logger logger = LoggerFactory.getLogger(StartIndexingServiceImpl.class);

    private final SiteEntityRepository siteEntityRepository;
    private final SitesList sitesList;
    private final PageEntityRepository pageEntityRepository;

    @Transactional
    public void processSite() {
        createAndSaveSiteEntity();
    }

    private void createAndSaveSiteEntity() {
        List<SiteEntity> listSiteEntitySaveInData = new ArrayList<>();
        List<Site> sitesLists = sitesList.getSites();
        for (Site site : sitesLists) {
            String name = site.getName();
            String url = site.getUrl();

            deleteSiteAndRelatedPages(url);

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
            pageEntityRepository.deleteBySite_Id(siteEntity.getId());

            siteEntityRepository.deleteSiteByUrl(siteEntity.getUrl());
            logger.info("delete SiteEntity and Related Pages " + siteEntity.getName());
        }
    }
}
