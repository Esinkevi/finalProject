package searchengine.services.startindexingservice;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.config.WebCrawlingConfig;
import searchengine.model.*;
import searchengine.repositories.IndexEntityRepository;
import searchengine.repositories.LemmaEntityRepository;
import searchengine.repositories.PageEntityRepository;
import searchengine.repositories.SiteEntityRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class ForExample extends RecursiveAction {
    private static final String A = "a";
    private static final String HREF_ATTR = "href";
    private static final String CONTENT_START_TEXT = "text/";
    private static final int DELAY_BETWEEN_REQUESTS_MIN = 500;
    private static final int DELAY_BETWEEN_REQUESTS_MAX = 5000;
    //    private static AtomicBoolean interruptedFlag = new AtomicBoolean(false);
    private static volatile boolean interruptedFlag;
    private static final Logger logger = LoggerFactory.getLogger(ForExample.class);
    private static final int availableProcessors = Runtime.getRuntime().availableProcessors();
    private static final ForkJoinPool forkJoinpool = new ForkJoinPool(availableProcessors);
    private static final WordProcessing wordProcessing = new WordProcessing();

    private final SiteEntity siteEntity;
    private final String url;

    private final PageEntityRepository pageEntityRepository;
    private final WebCrawlingConfig webCrawlingConfig;
    private final SiteEntityRepository siteEntityRepository;
    private final LemmaEntityRepository lemmaEntityRepository;
    private final IndexEntityRepository indexEntityRepository;

    @Override
    protected void compute() {

        if (shouldInterrupt()) {
            return;
        }
        updateSiteStatus(SiteEntityStatus.INDEXING, "");

        Set<String> linksList = getLinks(url);
        processLinks(linksList);

        updateSiteStatus(SiteEntityStatus.INDEXED, "Индексация завершена");

    }

    private void processLinks(Set<String> linksList) {
        forkJoinpool.submit(() -> linksList.parallelStream().forEach(link -> {
            if (!shouldInterrupt()) {
                processLink(link);
            }
        })).invoke();
    }

    private void processLink(String link) {
        try {
            if (!pageEntityRepository.existsByPath(link)) {
                String absUrl = siteEntity.getUrl() + link;
                Connection.Response response = Jsoup.connect(absUrl).userAgent(webCrawlingConfig.getUserAgent()).referrer(webCrawlingConfig.getReferrer()).ignoreContentType(true).execute();

                if (response.contentType().startsWith(CONTENT_START_TEXT)) {

                    PageEntity pageEntity = createPageEntity(link, response);
                    pageEntityRepository.save(pageEntity);

                    List<LemmaEntity> lemmaEntityListForIndexTable = createLemmaEntity(pageEntity);
                    List<IndexEntity> indexEntityList = createIndexEntity(pageEntity, lemmaEntityListForIndexTable);

                    indexEntityRepository.saveAll(indexEntityList);


                    ForkJoinTask task = new ForExample(siteEntity, absUrl, pageEntityRepository, webCrawlingConfig, siteEntityRepository, lemmaEntityRepository, indexEntityRepository);
                    task.fork();
                }
                Thread.sleep(getRandomDelay());
            }

        } catch (IOException e) {
            handleError(link, e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<IndexEntity> createIndexEntity(PageEntity pageEntity, List<LemmaEntity> lemmaEntityList) {
        List<IndexEntity> indexEntityList = new ArrayList<>();
        for (LemmaEntity lemmaEntity : lemmaEntityList) {
            IndexEntity indexEntity = new IndexEntity();
            indexEntity.setPageId(pageEntity);
            indexEntity.setLemmaId(lemmaEntity);
            indexEntity.setRank(lemmaEntity.getFrequency());
            indexEntityList.add(indexEntity);
        }

        return indexEntityList;
    }


    private List<LemmaEntity> createLemmaEntity(PageEntity pageEntity) {
        List<LemmaEntity> lemmaEntityList = new ArrayList<>();
        List<LemmaEntity> lemmaEntityListForIndexTable = new ArrayList<>();
        Map<String, Integer> listWord = wordProcessing.processingWord(pageEntity.getContent());
        for (Map.Entry<String, Integer> words : listWord.entrySet()) {
            String word = words.getKey();
            Integer frequency = words.getValue();
            LemmaEntity lemmaEntityRepositoryBySiteIdAndLemma = lemmaEntityRepository.findBySiteIdAndLemma(siteEntity, word);
            if (lemmaEntityRepositoryBySiteIdAndLemma == null) {
                LemmaEntity lemmaEntity = new LemmaEntity();
                lemmaEntity.setSiteId(siteEntity);
                lemmaEntity.setLemma(word);
                lemmaEntity.setFrequency(frequency);
                lemmaEntityList.add(lemmaEntity);
                lemmaEntityListForIndexTable.add(lemmaEntity);
            } else {
                lemmaEntityRepositoryBySiteIdAndLemma.setFrequency(lemmaEntityRepositoryBySiteIdAndLemma.getFrequency() + frequency);
                lemmaEntityList.add(lemmaEntityRepositoryBySiteIdAndLemma);
                lemmaEntityRepositoryBySiteIdAndLemma.setFrequency(frequency);
                lemmaEntityListForIndexTable.add(lemmaEntityRepositoryBySiteIdAndLemma);
            }
        }
        lemmaEntityRepository.saveAll(lemmaEntityList);
        return lemmaEntityListForIndexTable;
    }

    private void handleError(String link, Exception e) {
        logger.error("Ошибка при индексации сайта {} по URL {}: {}", siteEntity.getName(), link, e.getMessage());
        updateSiteStatus(SiteEntityStatus.FAILED, "Индексация завершена с ошибкой " + e.getMessage());
        ForExample.stopProcessing();
    }

    private long getRandomDelay() {
        return DELAY_BETWEEN_REQUESTS_MIN + (int) (Math.random() * (DELAY_BETWEEN_REQUESTS_MAX - DELAY_BETWEEN_REQUESTS_MIN + 1));
    }

    private PageEntity createPageEntity(String link, Connection.Response response) {
        PageEntity pageEntity = new PageEntity();
        pageEntity.setSiteId(siteEntity);
        pageEntity.setPath(link);
        pageEntity.setCode(response.statusCode());
        pageEntity.setContent(response.body());
        return pageEntity;
    }


    private void updateSiteStatus(SiteEntityStatus siteEntityStatus, String indexingStatus) {
        LocalDateTime currentTime = LocalDateTime.now();
        siteEntityRepository.updateSiteStatus(siteEntity.getId(), siteEntityStatus, currentTime, indexingStatus);
    }


    private Set<String> getLinks(String url) {

        Set<String> links = new TreeSet<>();
        try {
            Document doc = Jsoup.connect(url).userAgent(webCrawlingConfig.getUserAgent()).referrer(webCrawlingConfig.getReferrer()).get();
            Elements elements = doc.select(A);
            for (Element element : elements) {
                String link = element.attr(HREF_ATTR);

                if (link.startsWith("/")) {
                    links.add(link);
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка при индексации сайта {} по URL {}", siteEntity.getName(), url, e);
        }
        return links;
    }

    public static void stopProcessing() {
        interruptedFlag = true;
        forkJoinpool.shutdownNow();

    }

    public static void startProcessing() {
        interruptedFlag = false;
    }

    private boolean shouldInterrupt() {
        if (interruptedFlag) {
            updateSiteStatus(SiteEntityStatus.FAILED, "Индексация остановлена пользователем");
            logger.info("Индексация остановлена пользователем");
            return true;
        }
        return false;
    }

}
