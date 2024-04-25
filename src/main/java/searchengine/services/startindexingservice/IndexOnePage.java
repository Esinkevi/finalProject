package searchengine.services.startindexingservice;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.WebCrawlingConfig;
import searchengine.model.*;
import searchengine.repositories.IndexEntityRepository;
import searchengine.repositories.LemmaEntityRepository;
import searchengine.repositories.PageEntityRepository;
import searchengine.repositories.SiteEntityRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class IndexOnePage {
    private static final Logger logger = LoggerFactory.getLogger(IndexOnePage.class);

    private final SiteEntityRepository siteEntityRepository;
    private final PageEntityRepository pageEntityRepository;
    private final WebCrawlingConfig webCrawlingConfig;
    private final LemmaEntityRepository lemmaEntityRepository;
    private final WordProcessing wordProcessing;
    private final IndexEntityRepository indexEntityRepository;

    public void startProcessOnePage(Site site, String path) {
        SiteEntity siteEntity = getSiteEntity(site);
        PageEntity pageEntity = getPageEntity(path, siteEntity);
        Map<String, Integer> kitLemmas = wordProcessing.processingWord(pageEntity.getContent());
        List<LemmaEntity> lemmaEntityList = filingLemmaTable(siteEntity, kitLemmas);
        filingIndexTable(lemmaEntityList, pageEntity);

    }

    private void filingIndexTable(List<LemmaEntity> lemmaEntityList, PageEntity pageEntity) {
        List<IndexEntity> indexEntityList = new ArrayList<>();
        for (LemmaEntity lemma : lemmaEntityList) {
            IndexEntity indexEntity = new IndexEntity();
            indexEntity.setLemmaId(lemma);
            indexEntity.setPageId(pageEntity);
            indexEntity.setRank((float) lemma.getFrequency());
            indexEntityList.add(indexEntity);
        }
        indexEntityRepository.saveAll(indexEntityList);
    }

    private List<LemmaEntity> filingLemmaTable(SiteEntity siteEntity, Map<String, Integer> kitLemma) {
        List<LemmaEntity> lemmaEntityList = new ArrayList<>();
        List<LemmaEntity> lemmaFordIndexTable = new ArrayList<>();
        for (Map.Entry<String, Integer> word : kitLemma.entrySet()) {
            String lemma = word.getKey();
            int frequency = word.getValue();
            LemmaEntity existingLemma = lemmaEntityRepository.findBySiteIdAndLemma(siteEntity, lemma);
            if (existingLemma != null) {
                existingLemma.setFrequency(existingLemma.getFrequency() + frequency);
                lemmaEntityList.add(existingLemma);
                existingLemma.setFrequency(frequency);
                lemmaFordIndexTable.add(existingLemma);
            } else {
                LemmaEntity lemmaEntity = new LemmaEntity();
                lemmaEntity.setSiteId(siteEntity);
                lemmaEntity.setLemma(lemma);
                lemmaEntity.setFrequency(frequency);
                lemmaEntityList.add(lemmaEntity);
                lemmaFordIndexTable.add(lemmaEntity);
            }
        }
        lemmaEntityRepository.saveAll(lemmaEntityList);
        return lemmaFordIndexTable;
    }

    private PageEntity getPageEntity(String path, SiteEntity siteEntity) {
        PageEntity pageEntityRepositoryByPath = pageEntityRepository.findBySiteIdAndPath(siteEntity, path);
        if (pageEntityRepositoryByPath == null) {
            PageEntity pageEntity = new PageEntity();
            try {
                String baseUrl = siteEntity.getUrl() + path;

                Connection.Response response = Jsoup.connect(baseUrl).referrer(webCrawlingConfig.getReferrer()).userAgent(webCrawlingConfig.getUserAgent()).execute();

                pageEntity.setCode(response.statusCode());
                pageEntity.setPath(path);
                pageEntity.setContent(response.body());
                pageEntity.setSiteId(siteEntity);
                pageEntityRepository.save(pageEntity);
                return pageEntity;

            } catch (IOException e) {
                logger.error("Error link: {}", siteEntity.getUrl() + path);
            }
        }
        clearTables(siteEntity, pageEntityRepositoryByPath);

        return pageEntityRepositoryByPath;
    }

    private void clearTables(SiteEntity siteEntity, PageEntity pageEntityRepositoryByPath) {
        List<IndexEntity> indexEntityRepositoryByPageId = indexEntityRepository.findByPageId(pageEntityRepositoryByPath);
        indexEntityRepository.deleteAll(indexEntityRepositoryByPageId);
        System.out.println("Сущности удаленны");
        List<LemmaEntity> lemmaEntityList = new ArrayList<>();
        for (IndexEntity indexEntity : indexEntityRepositoryByPageId) {
            LemmaEntity lemmaEntity = indexEntity.getLemmaId();
            lemmaEntityList.add(lemmaEntity);
        }
        lemmaEntityRepository.deleteAll(lemmaEntityList);
    }

    private SiteEntity getSiteEntity(Site site) {
        SiteEntity siteEntityRepositoryByUrl = siteEntityRepository.findByUrl(site.getUrl());
        if (siteEntityRepositoryByUrl == null) {
            SiteEntity siteEntity = new SiteEntity();
            siteEntity.setUrl(site.getUrl());
            siteEntity.setName(site.getName());
            siteEntity.setStatus(SiteEntityStatus.INDEXING);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteEntityRepository.save(siteEntity);
            return siteEntity;
        }
        return siteEntityRepositoryByUrl;
    }


}
