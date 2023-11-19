package searchengine.services.startIndexingService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.config.WebCrawlingConfig;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageEntityRepository;
import searchengine.repositories.SiteEntityRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

@Component
@RequiredArgsConstructor
public class ProcessPageEntity {



    private final PageEntityRepository pageEntityRepository;
    private final SiteEntityRepository siteEntityRepository;
    private final SitesList sitesList;
    private final WebCrawlingConfig webCrawlingConfig;

    public void processPage() {
        List<ForkJoinTask<Void>> tasks = new ArrayList<>();

        for (Site site : sitesList.getSites()){
            SiteEntity siteEntity = siteEntityRepository.findByUrl(site.getUrl());
            if (siteEntity != null){
                CrawlSite crawlSite = new CrawlSite(siteEntity,siteEntity.getUrl(),pageEntityRepository,webCrawlingConfig,siteEntityRepository);
                tasks.add(crawlSite);
            }
        }

        ForkJoinTask.invokeAll(tasks);


    }
}
