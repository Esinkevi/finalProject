package searchengine.services.startindexingservice;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.config.WebCrawlingConfig;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexEntityRepository;
import searchengine.repositories.LemmaEntityRepository;
import searchengine.repositories.PageEntityRepository;
import searchengine.repositories.SiteEntityRepository;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

@Service
@RequiredArgsConstructor
public class ProcessPage {

    private static final Logger logger = LoggerFactory.getLogger(ProcessPage.class);


    private final SiteEntityRepository siteEntityRepository;
    private final SitesList sitesList;
    private final PageEntityRepository pageEntityRepository;
    private final WebCrawlingConfig webCrawlingConfig;
    private final IndexOnePage indexOnePage;
    private final LemmaEntityRepository lemmaEntityRepository;
    private final IndexEntityRepository indexEntityRepository;
    private final WordProcessing wordProcessing;


    public void processPages() {

        List<IndexingSite> indexingSites = new ArrayList<>();
        List<Site> siteList = sitesList.getSites();
        for (Site site : siteList) {
            SiteEntity siteEntity = siteEntityRepository.findByUrl(site.getUrl());
            IndexingSite indexingSite = new IndexingSite(siteEntityRepository, siteEntity, siteEntity.getUrl(), webCrawlingConfig, pageEntityRepository, wordProcessing, lemmaEntityRepository, indexEntityRepository);
            indexingSites.add(indexingSite);
        }
        ForkJoinTask.invokeAll(indexingSites);

    }

    public boolean processOnePage(String link) {
        try {
            URL url = new URL(link);
            URI uri = new URI(url.getProtocol(), url.getHost(), null, null);

            for (Site site : sitesList.getSites()) {
                URI uriSite = new URI(site.getUrl());
                if (uriSite.equals(uri)) {
                    String path = url.getPath().isEmpty() ? "/" : url.getPath();
                    indexOnePage.startProcessOnePage(site, path);
                    return true;
                }
            }
        } catch (MalformedURLException e) {
            logger.error("Ошибка при обработке URL: {}", link);
        } catch (URISyntaxException e) {
            logger.error("Ошибка при обработке URL: {}", link);
        }

        return false;
    }

}
