package searchengine.services.searchservice;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.dto.search.*;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexEntityRepository;
import searchengine.repositories.LemmaEntityRepository;
import searchengine.repositories.PageEntityRepository;
import searchengine.repositories.SiteEntityRepository;
import searchengine.services.startindexingservice.WordProcessing;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchServiceImplTwo implements SearchService {
    private static final int NOT_FOUND = -1;
    private static final int FIRST_CHARACTER = 0;
    private static final int AMOUNT_FOR_TEXT_TRIMMING = 10;
    private static final char DEFAULT_SYMBOL_FOR_FIND = ' ';


    private final SiteEntityRepository siteEntityRepository;
    private final WordProcessing wordProcessing;
    private final LemmaEntityRepository lemmaEntityRepository;
    private final IndexEntityRepository indexEntityRepository;
    private final PageEntityRepository pageEntityRepository;

    private SiteEntity siteEntity;
    private String query;

    @Override
    public SearchResponseDto getSearchResult(SearchRequest request) {
        SearchResponseDto searchResponseDto = new SearchResponseDto();
        if (checkRequest(request)) {
            Data[] data = getData();
            searchResponseDto.setCount(data.length);
            searchResponseDto.setData(data);
            searchResponseDto.setResult(true);
            return searchResponseDto;
        }
        searchResponseDto.setResult(false);
        searchResponseDto.setError("Задан пустой поисковый запрос");
        return searchResponseDto;
    }

    private boolean checkRequest(SearchRequest request) {
        if (request.getQuery().isEmpty()) {
            return false;
        }
        if (request.getSite() != null) {
            siteEntity = siteEntityRepository.findByUrl(request.getSite());
        }
        query = request.getQuery();
        return true;
    }


    private Data[] getData() {
        List<Data> dataList = searchSites();
        dataList.sort(Comparator.comparingDouble(Data::getRelevance));
        Data[] dataArray = dataList.toArray(new Data[0]);
        return dataArray;
    }

//    private List<Data> getDataList() {
//        if (siteEntity != null) {
//            return searchOnOneSite();
//
//        } else {
//            return searchAllSites();
//        }
//
//    }
//
//    private List<Data> searchAllSites() {
//        List<LemmaEntity> lemmaEntityList = getLemmaEntity();
//        List<Long> commonPagesId = getCommonPagesId(lemmaEntityList);
//        if (commonPagesId.isEmpty()) {
//            return new ArrayList<>();
//        }
//        List<IndexEntity> indexEntityList = getIndexEntity(lemmaEntityList, commonPagesId);
//        List<RelevanceDataDto> relevanceDataDto = getRelevanceDataDto(indexEntityList, commonPagesId);
//        List<Data> result = getDataObject(relevanceDataDto);
//        return result;
//    }

    private List<Data> searchSites() {
        List<LemmaEntity> lemmaEntityList = getLemmaEntity();

        if (lemmaEntityList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> commonPagesId = getCommonPagesId(lemmaEntityList);

        if (commonPagesId.isEmpty()) {
            return new ArrayList<>();
        }

        List<IndexEntity> indexEntityList = getIndexEntity(lemmaEntityList, commonPagesId);
        List<RelevanceDataDto> relevanceDataDto = getRelevanceDataDto(indexEntityList, commonPagesId);
        List<Data> result = getDataObject(relevanceDataDto);

        return result;
    }

    private List<LemmaEntity> getLemmaEntity() {
        List<LemmaEntity> lemmaEntityList = new ArrayList<>();
        Map<String, Integer> lemmasList = wordProcessing.processingWord(query);
        for (Map.Entry<String, Integer> lemmas : lemmasList.entrySet()) {
            LemmaEntity lemmaEntity;
            if (siteEntity == null) {
                List<LemmaEntity> lemmaEntities = lemmaEntityRepository.findByLemma(lemmas.getKey());
                lemmaEntityList.addAll(lemmaEntities);
            } else {
                lemmaEntity = lemmaEntityRepository.findBySiteIdAndLemma(siteEntity, lemmas.getKey());
                if (lemmaEntity != null) {
                    lemmaEntityList.add(lemmaEntity);
                }
            }

        }
        lemmaEntityList.sort(Comparator.comparingInt(LemmaEntity::getFrequency));
        return lemmaEntityList;
    }

    private List<Long> getCommonPagesId(List<LemmaEntity> lemmaEntityList) {
        List<Long> mainPagesId = getPageIdLemmas(lemmaEntityList.get(0));
        if (lemmaEntityList.size() > 1) {
            int counterEmptyList = 1;
            for (int i = 1; i < lemmaEntityList.size(); i++) {
                List<Long> currentPagesId = getPageIdLemmas(lemmaEntityList.get(i));
                currentPagesId.retainAll(mainPagesId);
                if (!currentPagesId.isEmpty()) {
                    mainPagesId = currentPagesId;
                } else {
                    counterEmptyList++;
                }
            }
            if (counterEmptyList == lemmaEntityList.size()) {
                mainPagesId.clear();
                return new ArrayList<>();
            }
            return mainPagesId;
        }
        return mainPagesId;
    }

    private List<IndexEntity> getIndexEntity(List<LemmaEntity> lemmaEntityList, List<Long> commonPagesId) {

        List<IndexEntity> indexEntityList = new ArrayList<>();

        for (LemmaEntity lemmaEntity : lemmaEntityList) {
            List<IndexEntity> currentIndexEntities = indexEntityRepository.findByLemmaId(lemmaEntity);
            for (IndexEntity indexEntity : currentIndexEntities) {
                long pageId = indexEntity.getPageId().getId();
                if (commonPagesId.contains(pageId)) {
                    indexEntityList.add(indexEntity);
                }
            }
        }
        return indexEntityList;
    }

    private List<RelevanceDataDto> getRelevanceDataDto(List<IndexEntity> indexEntityList, List<Long> commonPagesId) {

        List<RelevanceDataDto> relevanceDataDtoList = new ArrayList<>();
        for (Long pageId : commonPagesId) {
            List<LemmaEntity> lemmaEntityList = new ArrayList<>();
            RelevanceDataDto relevanceDataDto = new RelevanceDataDto();
            PageEntity pageEntity = new PageEntity();
            float absRelevance = 0;
            for (IndexEntity indexEntity : indexEntityList) {
                Long indexPageId = indexEntity.getPageId().getId();
                if (pageId.equals(indexPageId)) {
                    LemmaEntity lemmaEntity = indexEntity.getLemmaId();
                    pageEntity = indexEntity.getPageId();
                    lemmaEntityList.add(lemmaEntity);
                    absRelevance += indexEntity.getRank();
                }
            }
            relevanceDataDto.setLemmaEntityList(lemmaEntityList);
            relevanceDataDto.setAbsRelevance(absRelevance);
            relevanceDataDto.setPageEntity(pageEntity);
            relevanceDataDtoList.add(relevanceDataDto);
        }
        return relevanceDataDtoList;
    }

    private List<Data> getDataObject(List<RelevanceDataDto> relevanceDataDto) {
        List<Data> dataList = new ArrayList<>();
        double maxRelevance = relevanceDataDto.stream().mapToDouble(RelevanceDataDto::getAbsRelevance).max().orElse(0.0);
        for (RelevanceDataDto dto : relevanceDataDto) {

            Data data = new Data();
            if (siteEntity == null) {
                data.setSiteName(dto.getPageEntity().getSiteId().getName());
                data.setSite(dto.getPageEntity().getSiteId().getUrl());
            } else {
                data.setSite(siteEntity.getUrl());
                data.setSiteName(siteEntity.getName());
            }
            data.setUri(dto.getPageEntity().getPath());
            data.setRelevance(dto.getAbsRelevance() / maxRelevance);


            HtmlDataDto htmlDataDto = getHtmlData(dto.getPageEntity().getContent());
            data.setTitle(htmlDataDto.getTitle());
            data.setSnippet(htmlDataDto.getSnippet());
            dataList.add(data);


        }


        return dataList;
    }

    private HtmlDataDto getHtmlData(String htmlContent) {
        HtmlDataDto htmlDataDto = new HtmlDataDto();
        Document doc = Jsoup.parse(htmlContent);
        htmlDataDto.setTitle(doc.title());
        htmlDataDto.setSnippet(getSnippet(query, doc.text()));
        return htmlDataDto;
    }

    private String getSnippet(String query, String text) {
        if (text.contains(query)) {
            int startIndex = text.indexOf(query);
            int endIndex = startIndex + query.length();

            int firstIndex = findFirstIndex(text, startIndex);
            int lastIndex = findLastIndex(text, endIndex);

            String highlightedQuery = "<b>" + text.substring(startIndex, endIndex) + "</b>";

            return text.substring(firstIndex, startIndex) + highlightedQuery + text.substring(endIndex, lastIndex);

        }
        String error = "Запрос не найден на данной странице";
        return error;
    }

    private int findLastIndex(String text, int fromIndex) {
        int index = fromIndex;
        for (int i = 0; i < AMOUNT_FOR_TEXT_TRIMMING; i++) {
            index = text.indexOf(DEFAULT_SYMBOL_FOR_FIND, index + 1);
            if (index == NOT_FOUND) {
                return text.length();
            }
        }
        return index;
    }

    private int findFirstIndex(String text, int fromIndex) {
        int index = fromIndex;
        for (int i = 0; i < AMOUNT_FOR_TEXT_TRIMMING; i++) {
            index = text.lastIndexOf(DEFAULT_SYMBOL_FOR_FIND, index - 1);
            if (index == NOT_FOUND) {
                return FIRST_CHARACTER;
            }
        }
        return index;
    }


    private List<Long> getPageIdLemmas(LemmaEntity lemmaEntity) {
        List<Long> pagesId = new ArrayList<>();
        List<IndexEntity> indexEntityList = indexEntityRepository.findByLemmaId(lemmaEntity);
        for (IndexEntity indexEntity : indexEntityList) {
            long pageId = indexEntity.getPageId().getId();
            pagesId.add(pageId);
        }

        return pagesId;
    }


}
