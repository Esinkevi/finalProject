package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.repositories.LemmaEntityRepository;
import searchengine.repositories.PageEntityRepository;
import searchengine.repositories.SiteEntityRepository;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final Random random = new Random();
    private final SitesList sites;
    private final SiteEntityRepository siteEntityRepository;
    private final PageEntityRepository pageEntityRepository;
    private final LemmaEntityRepository lemmaEntityRepository;

//    @Override
//    public StatisticsResponse getStatistics() {
//        String[] statuses = { "INDEXED", "FAILED", "INDEXING" };
//        String[] errors = {
//                "Ошибка индексации: главная страница сайта не доступна",
//                "Ошибка индексации: сайт не доступен",
//                ""
//        };
//
//        TotalStatistics total = new TotalStatistics();
//        total.setSites(sites.getSites().size());
//        total.setIndexing(true);
//
//        List<DetailedStatisticsItem> detailed = new ArrayList<>();
//        List<Site> sitesList = sites.getSites();
//        for(int i = 0; i < sitesList.size(); i++) {
//            Site site = sitesList.get(i);
//            DetailedStatisticsItem item = new DetailedStatisticsItem();
//            item.setName(site.getName());
//            item.setUrl(site.getUrl());
//            int pages = random.nextInt(1_000);
//            int lemmas = pages * random.nextInt(1_000);
//            item.setPages(pages);
//            item.setLemmas(lemmas);
//            item.setStatus(statuses[i % 3]);
//            item.setError(errors[i % 3]);
//            item.setStatusTime(System.currentTimeMillis() -
//                    (random.nextInt(10_000)));
//            total.setPages(total.getPages() + pages);
//            total.setLemmas(total.getLemmas() + lemmas);
//            detailed.add(item);
//        }
//
//        StatisticsResponse response = new StatisticsResponse();
//        StatisticsData data = new StatisticsData();
//        data.setTotal(total);
//        data.setDetailed(detailed);
//        response.setStatistics(data);
//        response.setResult(true);
//        return response;
//    }

    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics totalStatistics = getTotalStatics();

        List<DetailedStatisticsItem> detailedStatisticsItemList = getListDetailedStaticsItem();

        StatisticsData statisticsData = new StatisticsData();
        statisticsData.setDetailed(detailedStatisticsItemList);
        statisticsData.setTotal(totalStatistics);

        StatisticsResponse response = new StatisticsResponse();
        response.setStatistics(statisticsData);
        response.setResult(true);

        return response;
    }

    private List<DetailedStatisticsItem> getListDetailedStaticsItem() {
        List<DetailedStatisticsItem> detailedStatisticsItemList = new ArrayList<>();
        List<SiteEntity> siteEntityList = siteEntityRepository.findAll();
        for (SiteEntity siteEntity : siteEntityList) {
            SiteEntity site = siteEntity;
            DetailedStatisticsItem detailedStatisticsItem = getDetailedStatisticsItem(site);
            detailedStatisticsItemList.add(detailedStatisticsItem);
        }
        return detailedStatisticsItemList;
    }

    private DetailedStatisticsItem getDetailedStatisticsItem(SiteEntity site) {
        DetailedStatisticsItem detailedStatisticsItem = new DetailedStatisticsItem();
        detailedStatisticsItem.setError(site.getLastError());

        Long sumLemmaFrequency = lemmaEntityRepository.sumFrequencyBySiteId(site);
        detailedStatisticsItem.setLemmas(sumLemmaFrequency != null ? sumLemmaFrequency.intValue() : 0);

        detailedStatisticsItem.setUrl(site.getUrl());
        detailedStatisticsItem.setStatus(String.valueOf(site.getStatus()));
        detailedStatisticsItem.setName(site.getName());
        detailedStatisticsItem.setPages((int) pageEntityRepository.countBySiteId(site));
        long millis = site.getStatusTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        detailedStatisticsItem.setStatusTime(millis);

        return detailedStatisticsItem;
    }

    private TotalStatistics getTotalStatics() {
        TotalStatistics totalStatistics = new TotalStatistics();
        totalStatistics.setIndexing(true);
        totalStatistics.setLemmas((int) lemmaEntityRepository.sumAllFrequency());
        totalStatistics.setSites((int) siteEntityRepository.count());
        totalStatistics.setPages((int) pageEntityRepository.count());
        return totalStatistics;
    }


}
