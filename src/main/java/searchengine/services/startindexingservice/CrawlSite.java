package searchengine.services.startindexingservice;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.config.WebCrawlingConfig;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.SiteEntityStatus;
import searchengine.repositories.PageEntityRepository;
import searchengine.repositories.SiteEntityRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class CrawlSite extends RecursiveAction {

    private static final String A = "a";
    private static final String HREF_ATTR = "href";
    private static final Object lock = new Object();
    private static boolean interrupted;
    private static final Logger logger = LoggerFactory.getLogger(CrawlSite.class);
    private static final int availableProcessors = Runtime.getRuntime().availableProcessors();
    private static final ForkJoinPool forkJoinpool = new ForkJoinPool(availableProcessors);

    private final SiteEntity siteEntity;
    private final String url;

    private final PageEntityRepository pageEntityRepository;
    private final WebCrawlingConfig webCrawlingConfig;
    private final SiteEntityRepository siteEntityRepository;

    @Override
    protected void compute() {

        Set<String> linksList = getLinks(url);

        forkJoinpool.submit(() -> linksList.parallelStream().forEach(link -> {

            if (interrupted) {
                siteEntityRepository.updateSiteStatus(siteEntity.getId(), SiteEntityStatus.FAILED, LocalDateTime.now(), "Индексация остановлена пользователем");
                return;
            }

            String absUrl = siteEntity.getUrl() + link;
            try {
                Connection.Response response = Jsoup.connect(absUrl).userAgent(webCrawlingConfig.getUserAgent()).referrer(webCrawlingConfig.getReferrer()).ignoreContentType(true).execute();
                if (response.contentType().startsWith("text/")) {
                    PageEntity pageEntity = new PageEntity();
                    pageEntity.setSiteId(siteEntity);
                    pageEntity.setPath(link);
                    pageEntity.setContent(response.body());
                    pageEntity.setCode(response.statusCode());

                    synchronized (lock) {
                        if (!pageEntityRepository.existsByPath(link)) {
                            pageEntityRepository.save(pageEntity);
                            siteEntityRepository.updateSiteStatus(siteEntity.getId(), SiteEntityStatus.INDEXING, LocalDateTime.now(), "");

                        }
                    }

                    ForkJoinTask task = new CrawlSite(siteEntity, absUrl, pageEntityRepository, webCrawlingConfig, siteEntityRepository);
                    task.fork();
                }

            } catch (IOException e) {
                logger.error("Ошибка при индексации сайта {} по URL {}", siteEntity.getName(), url, e);
            }

        })).invoke();


        siteEntityRepository.updateSiteStatus(siteEntity.getId(), SiteEntityStatus.INDEXED, LocalDateTime.now(), "Индексация завершена");

    }

    public static void interrupt() {
        interrupted = true;
    }

    private Set<String> getLinks(String url) {

        Set<String> links = new TreeSet<>();
        try {
            Document doc = Jsoup.connect(url).userAgent(webCrawlingConfig.getUserAgent()).referrer(webCrawlingConfig.getReferrer()).get();
            Elements elements = doc.select(A);
            for (Element element : elements) {
                String link = element.attr(HREF_ATTR);

                if (link.startsWith("/") && !pageEntityRepository.existsByPath(link)) {
                    links.add(link);
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка при индексации сайта {} по URL {}", siteEntity.getName(), url, e);
        }
        return links;
    }


}
