package searchengine.services.startindexingservice;

import lombok.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class IndexingSite extends RecursiveAction {

    private static final int DELAY_BETWEEN_REQUESTS_MIN = 500;
    private static final int DELAY_BETWEEN_REQUESTS_MAX = 5000;
    private static final String CONTENT_START_TEXT = "text/";
    private static final String A = "a";
    private static final int availableProcessors = Runtime.getRuntime().availableProcessors();
    private static final ForkJoinPool forkJoinpool = new ForkJoinPool(availableProcessors);
    private static final String HREF_ATTR = "href";
    private static volatile boolean interruptedFlag;
    private static final Logger logger = LoggerFactory.getLogger(IndexingSite.class);

    private final SiteEntityRepository siteEntityRepository;
    private final SiteEntity siteEntity;
    private final String url;
    private final WebCrawlingConfig webCrawlingConfig;
    private final PageEntityRepository pageEntityRepository;
    private final WordProcessing wordProcessing;
    private final LemmaEntityRepository lemmaEntityRepository;
    private final IndexEntityRepository indexEntityRepository;


    @Override
    protected void compute() {
        Set<String> links = getLinks(url);
        crawlLinks(links);
        if (!shouldInterrupt()) {
            updateSiteStatus(SiteEntityStatus.INDEXED, "Индексация завершена");
        }
    }

    private void crawlLinks(Set<String> links) {
        for (String link : links) {
            if (!shouldInterrupt()) {
                crawlLink(link);
            }
        }
    }

    private void crawlLink(String link) {
        try {
            if (!pageEntityRepository.existsByPathAndSiteId(link, siteEntity)) {
                String absUrl = siteEntity.getUrl() + link;
                Connection.Response response = jsoupConnect(absUrl);
                if (response.contentType().startsWith(CONTENT_START_TEXT)) {
                    PageEntity pageEntity = createdPageEntity(link, response);
                    List<LemmaEntity> lemmaEntityList = getLemmaEntity(pageEntity);
                    fillingTheIndexTable(pageEntity, lemmaEntityList);

                    Thread.sleep(getRandomDelay());


                    IndexingSite newTask = new IndexingSite(siteEntityRepository, siteEntity, absUrl, webCrawlingConfig, pageEntityRepository, wordProcessing, lemmaEntityRepository, indexEntityRepository);
                    forkJoinpool.invoke(newTask);


                }
            }
        } catch (InterruptedException e) {
            handleError(link, e);
        }
    }

    private void fillingTheIndexTable(PageEntity pageEntity, List<LemmaEntity> lemmaEntityList) {
        List<IndexEntity> indexEntityList = new ArrayList<>();
        for (LemmaEntity lemmaEntity : lemmaEntityList) {
            IndexEntity indexEntity = new IndexEntity();
            indexEntity.setRank((float) lemmaEntity.getFrequency());
            indexEntity.setPageId(pageEntity);
            indexEntity.setLemmaId(lemmaEntity);
            indexEntityList.add(indexEntity);
        }
        indexEntityRepository.saveAll(indexEntityList);
        indexEntityList.clear();
    }

    private List<LemmaEntity> getLemmaEntity(PageEntity pageEntity) {
        List<LemmaEntity> lemmaEntityListForSaveRepository = new ArrayList<>();
        List<LemmaEntity> lemmaEntityListForNextProcess = new ArrayList<>();
        Map<String, Integer> words = wordProcessing.processingWord(pageEntity.getContent());
        for (Map.Entry<String, Integer> maps : words.entrySet()) {
            String lemma = maps.getKey();
            int frequency = maps.getValue();
            LemmaEntity lemmaEntityInRepository = lemmaEntityRepository.findBySiteIdAndLemma(siteEntity, lemma);
            if (lemmaEntityInRepository == null) {
                LemmaEntity lemmaEntity = createdLemmaEntity(lemma, frequency);
                lemmaEntityListForNextProcess.add(lemmaEntity);
                lemmaEntityListForSaveRepository.add(lemmaEntity);
            } else {
                lemmaEntityInRepository.setFrequency(lemmaEntityInRepository.getFrequency() + frequency);
                lemmaEntityListForSaveRepository.add(lemmaEntityInRepository);

                lemmaEntityInRepository.setFrequency(frequency);
                lemmaEntityListForNextProcess.add(lemmaEntityInRepository);
            }
        }
        lemmaEntityRepository.saveAll(lemmaEntityListForSaveRepository);
        lemmaEntityListForSaveRepository.clear();
        return lemmaEntityListForNextProcess;
    }

    private LemmaEntity createdLemmaEntity(String lemma, int frequency) {
        LemmaEntity lemmaEntity = new LemmaEntity();
        lemmaEntity.setSiteId(siteEntity);
        lemmaEntity.setLemma(lemma);
        lemmaEntity.setFrequency(frequency);
        return lemmaEntity;
    }

    private Connection.Response jsoupConnect(String absUrl) {
        try {
            Connection.Response response = Jsoup.connect(absUrl).userAgent(webCrawlingConfig.getUserAgent()).referrer(webCrawlingConfig.getReferrer()).execute();
            return response;
        } catch (IOException e) {
            logger.error("Не подключились к ссылке " + absUrl);
        }
        return null;
    }

    private PageEntity createdPageEntity(String link, Connection.Response response) {
        PageEntity pageEntity = new PageEntity();
        pageEntity.setSiteId(siteEntity);
        pageEntity.setPath(link);
        pageEntity.setContent(response.body());
        pageEntity.setCode(response.statusCode());
        return pageEntityRepository.save(pageEntity);
    }


    public static void stopProcessing() {
        interruptedFlag = true;
        forkJoinpool.shutdownNow();


    }

    private boolean shouldInterrupt() {
        if (interruptedFlag) {

            forkJoinpool.awaitQuiescence(1,TimeUnit.MINUTES);
            updateSiteStatus(SiteEntityStatus.FAILED, "Индексация остановлена пользователем");
            logger.info("Индексация остановлена пользователем");
            return true;
        }
        return false;
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

    private long getRandomDelay() {
        return DELAY_BETWEEN_REQUESTS_MIN + (int) (Math.random() * (DELAY_BETWEEN_REQUESTS_MAX - DELAY_BETWEEN_REQUESTS_MIN + 1));
    }

    private void handleError(String link, Exception e) {
        logger.error("Ошибка при индексации сайта {} по URL {}: {}", siteEntity.getName(), link, e.getMessage());
        updateSiteStatus(SiteEntityStatus.FAILED, "Индексация завершена с ошибкой " + e.getMessage());
        IndexingSite.stopProcessing();
    }
}
