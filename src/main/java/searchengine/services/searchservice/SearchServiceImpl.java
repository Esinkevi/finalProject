package searchengine.services.searchservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.search.Data;
import searchengine.dto.search.SearchRequest;
import searchengine.dto.search.SearchResponseDto;

import java.util.ArrayList;
import java.util.List;



public class SearchServiceImpl  {

    public SearchResponseDto getSearchResult(SearchRequest request) {
        SearchResponseDto searchResponseDto = new SearchResponseDto();
        Data[] data = getData(request.getSite());
        searchResponseDto.setResult(true);
        searchResponseDto.setError("");
        searchResponseDto.setCount(2);
        searchResponseDto.setData(data);
        return searchResponseDto;
    }

    private Data[] getData(String site) {
        // Создаем массив данных
        Data[] data = new Data[2];
        Data dataItem = new Data();
        dataItem.setRelevance(1);
        dataItem.setUri("/part2");
        dataItem.setSite("My.site.com");
        dataItem.setTitle("бежит лошадь");
        String fragment = "Фрагмент текста, в котором найдены совпадения, <b>выделенные жирным</b>, в формате HTML";
        dataItem.setSnippet(fragment);
        dataItem.setSiteName(site);
        data[0] = dataItem;

        // Добавляем второй элемент
        Data dataItem2 = new Data();
        dataItem2.setRelevance(2);
        dataItem2.setUri("Другой путь к странице");
        dataItem2.setSite("Другое название сайта");
        dataItem2.setTitle("Другой заголовок страницы");
        dataItem2.setSnippet("Другой фрагмент текста");
        dataItem2.setSiteName("Другое имя сайта");
        data[1] = dataItem2;
        return data;
    }

    private List<Data> getDataItemList() {
        List<Data> dataItemList = new ArrayList<>();
        for (int i = 0; i > 2; i++){
            Data dataItem = new Data();
            dataItem.setRelevance(1 + i);
            dataItem.setUri("/part2");
            dataItem.setSite("My.site.com");
            dataItem.setTitle("бежит лошадь");
            String fragment = "Фрагмент текста, в котором найдены совпадения, <b>выделенные жирным</b>, в формате HTML";
            dataItem.setSnippet(fragment);
            dataItem.setSiteName("mySiteName");
            dataItemList.add(dataItem);
        }
        return dataItemList;
    }
}
