# Декларативный трансформатор структур данных (T в ETL)

## Задача

Пусть есть xls файл вида
```
Код Идентификатор Название
1   1:1-1-1       Крас.Путь-ул.Фрунзе
2   2:2-1-1       Крас.Путь-ул.Рабиновича
```

Необходимо преобразовать этот файл к формату списка JSON-объектов:
```json
[
  {
    "Info": [
      {
        "Description": "Адрес размещения: крас.путь-ул.фрунзе",
        "Resource": [
          {
            "ResourceDesc": "RTSPArchive"
          }
        ]
      }
    ],
    "Code": "1:1-1-1"
  },
  {
    "Info": [
      {
        "Description": "Адрес размещения: крас.путь-ул.рабиновича",
        "Resource": [
          {
            "ResourceDesc": "RTSPArchive"
          }
        ]
      }
    ],
    "Code": "2:2-1-1"
  }
]
```

После этого полученный список отправить в JMS очередь, а также иметь возможность скачать в виде файла. 

## Требования

* веб-интерфейс
* разные форматы входных данных
* разные форматы выходных данных
* возможность преобразовывать значения входных данных
* возможность декларативно задавать структуры выходных данных
* возможность выгрузить результат в виде файла в браузер
* возможность сохранить результат на диск на сервере или отправить в JMS в виде текста

## Структура приложения

* Собственно spring-boot конфигурация (ru.otus.etl.boot)
* Конверторы входных файлов (ru.otus.etl.core.input)
* Команды - функции, которые можно применять к входным данным (ru.otus.etl.core.transform.cmd)
* Модель - описание правил и конфигурации маппинга (ru.otus.etl.core.model)
* Конверторы результата (ru.otus.etl.core.transform.output)
* Веб-контроллеры (ru.otus.etl.web)
* Сервисы, связывающие все части приложения (ru.otus.etl.services)
* Репозитории CRUD - хранение маппингов (ru.otus.etl.repository)

### Конверторы входных данных

Выполняют разбор файлов с входными данными и приводят их к унифицированному интерфейсу Extractable, позволяющему получать значения полей по ключу.  

#### Поддерживаемые форматы:
* CSV
* XML
* XLS/XLSX

### Команды

Команды выполняют преобразование входных значений согласно заданным правилам. Каждая команда представлена своим классом с именем, производным от имени команды. Во время вычисления команды загружаются рефлексивно, используя правило преобразования имени команды в имя класса. Например, для команды `now` будет загружен класс `FNow`.  
Все команды реализуют интерфейс `Cmd`, главный метод которого принимает строку из входного файла и возвращает результат выполнения команды в виде строки. Например, команда `const(ABC)` вернет строку `ABC`.  
Команды могут быть вложены друг в друга. Тогда они выполняются последовательно, заменяя свое объявление в исходной строке результатом выполнения, начиная с команды, находящейся на самом нижнем уровне вложения. 

#### Алгоритм выполнения цепочки:

```java
  // разбираем исходное выражение на отдельные команды
  Matcher m = CMD_SIGNATURE.matcher(expression);
  // пока есть команды
  while (m.find()) {
      StringBuffer sb = new StringBuffer();
      // выбрать очередную команду
      String singleCmdStr = m.group(1);
      // загрузить класс команды по имени команды
      Cmd cmd = loadCmd(fetchCmdName(singleCmdStr));
      // установить аргументы 
      cmd.setArgs(fetchCmdArgs(singleCmdStr));
      // выполнить команду над входной строкой
      String result = cmd.exec(srcDataRow);
      // заменить в исходном выражении объявление команды результатом выполнения
      m = m.appendReplacement(sb, result);
      m.appendTail(sb);
      // разобрать получившееся выражение
      m = CMD_SIGNATURE.matcher(sb.toString());
  }

```  

#### Поддерживаемые команды:
* значение поля (get или $)
* конкатенация строк (concat)
* соединение списка в строку с разделителем (join)
* текущее время (now)
* замена подстроки (replace)
* константа (const)
* к нижнему регистру (toLower)

Команда "значение поля" имеет алиас $, который используется без скобок. Остальные команды требуют обязательного заключения аргументов в круглые скобки. При интерпретации круглые скобки заменяются на фигурные во избежание конфликтов в регулярных выражениях.

#### Канонический синтаксис команды:
```
<команда> ::= <название команды>{<аргумент 1>, | <аргумент 2>, | ... | <аргумент n>}
<название команды> ::= <строка>
<аргумент> ::= <строка> | <команда>
```


#### Пример одиночных команд: 
```
$Какое-то_поле_из_исх_документа
const(ABC)
```

#### Пример вложенных команд:
```
concat(Адрес размещения: , toLower($Название))
```
что эквивалентно
```
concat{Адрес размещения: , toLower{get{Название}}}
```

#### Порядок вычисления цепочки:
```
concat{Адрес размещения: , toLower{get{Название}}} =

get -> toLower -> concat = 

concat{Адрес размещения: , toLower{Крас.Путь-ул.Фрунзе}} -> 
concat{Адрес размещения: , крас.путь-ул.фрунзе} -> 
Адрес размещения: крас.путь-ул.фрунзе
```

### Модель 

Модель включает две сущности: конфигурацию маппинга и правила преобразования. 

#### Конфигурация
Конфигурация описывает общие сведения о маппинге: где находится файл входных данных, его детали (например, разделитель для CSV, есть ли заголовки и т.п.), какие данные ожидаются на выходе, что следует сделать после преобразования.

#### Правила
Правила описывают непосредственно преобразование. Правило состоит из левой и правой части. В левой части содержится имя поля выходной структуры. В правой - цепочка команд для вычисления значения.  
В левой части можно указывать составные поля и массивы. Такие нотации будут преобразованы в объекты в случае преобразования в JSON. Для "плоских" выходных форматов преобразование выполняться не будет.

#### Пример правил:
```
Info[0].Description = concat(Адрес размещения: , toLower($Название))
Info[0].Resource[0].ResourceDesc = const(RTSPArchive)
Code = $Идентификатор
```

#### Пример модели (конфигурация и правила)

```json
{
  "id": "www",
  "name": "www",
  "delimiter": "",
  "unmappedFieldName": "",
  "destUrl": "queue:test",
  "sourceUrl": "file:///tmp/list.xls",
  "sourceFilename": "list.xls",
  "destType": "JMS",
  "resultType": "JSON",
  "encoding": "utf-8",
  "firstRowAsHeader": true,  
  "rules": [
    {
      "left": "Info[0].Resource[0].ResourceDesc",
      "right": "const(RTSPArchive)"
    },
    {
      "left": "Info[0].Description",
      "right": "concat(Адрес размещения: , toLower($Название))"
    },
    {
      "left": "Code",
      "right": "$Идентификатор"
    }
  ]
}
```

### Конверторы результата  

Конверторы результата принимают на вход список Extractable строк исходного документа и применяют к ним правила маппинга, возвращая структуру заданного формата.

#### Поддерживаемые форматы:
* properties (ключ = значение)
* json
* csv (разделителем запятая)
* scsv (разделителем точка с запятой)

#### Ограничения плоских форматов:
* можно преобразовать только однострочные документы
* нет возможности задать поле для "несмаппированных" исходных ключей

### Веб-контроллеры

Используется два типа контроллеров: `@RestController` и `@Controller`. Последний используется для загрузки файлов на сервер. Для обработки исключений используется `@ControllerAdvice`, выдающий наружу только обработанный текст ошибок.

### Сервисы

В приложении всего два сервиса. `MappingService` отвечает за работу с маппингами, выступая фасадом репозитория и инкапсулируя часть логики по обработке маппинга. `FileService` отвечает за запись файлов на диск. 

### Репозитории

Приложение, в целом, не требуют БД, поскольку операции по управлению маппингами простейшие CRUD. Можно было бы обойтись файловым хранилищем. Тем не менее, в демонстрационных целях введена БД PostgreSQL. Доступ осуществляется через `CrudRepository`. 

### JMS

В качестве JMS используется activemq (требование ТЗ). Для работы с JMS используется встроенный `JmsTemplate`.

## Обработка ошибок

Все сервисы стараются перехватить и обработать все ошибки своих операций. Наружу выдается унифицированный `EtlException` либо его потомок, не содержащий технические подробности (такие как stacktrace) оригинальной ошибки. 

## Веб-интерфейс

![Веб-интерфейс](maket.png?raw=true "Веб-интерфейс")

