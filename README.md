### Идея
Простейший индексатор. Клиент написан на html, javascript, jQuery. Сервер написан на Java, используя Spring фрейморк и MySQL в качестве базы данных.
### Принцип работы
На сервере в конфигурации указывается список главных страниц сайтов, которые мы хотим проиндексировать. Далее сервер проходит по каждой странице связанной с сайтом, парся html и записывая информацию о встречаюшихся словах. Для решения проблемы разных форм слов, например лошад**ь** - лошад**ей**, использвалась библиотека лемматизации - _Lucene_.

На клиенте у нас есть три окна:

- **Dashboard**. В нем мы выводим инфорамацию по всем проиндексированым сайтам - количество лемм, количество страниц и т.д.
  ![image](https://github.com/Esinkevi/finalProject/assets/151565951/de668aca-5c8e-4cfd-bfa8-830cd14295c1)

- **Managment**. В нем мы можем посмотреть информацию для конкретной страницы, которая связана с сайтом. К примеру у нас в конфигурации указан сайт _example.com_. Во вкладке **Managment**, мы можем написать _example.com/1_ и нам будет выведена информация по этой, конкретной странице
  ![image](https://github.com/Esinkevi/finalProject/assets/151565951/783e5d0b-5258-4569-aa45-555777ef95d3)

- **Search**. В нем мы можем сделать поиск по конкретному слову. Поиск может быть выполнен по всем сайтам указаным в конфигурации или по какому-то конкретному.
![image](https://github.com/Esinkevi/finalProject/assets/151565951/c967cb9a-d253-497a-8107-ea9df06a807a)
