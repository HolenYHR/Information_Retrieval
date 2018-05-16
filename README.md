# Information_Retrieval
It is information retrieval project about poem retrieval.
 
We crawl poems from the website https://www.gushiwen.org/ and then we store them in a database. So if you want to run the code, you need to have your own database. 

Dependencies:
1. python3
2. scrapy
3. flask 
4. java
5. lucene

How to run?

1. run **retrieval/begin.py** 爬取古诗
2. run **Hello/src/test/Indexer.java** 构建索引
3. run **retrieval/SearchEngine.py** 进行搜索得到结果
