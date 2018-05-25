import scrapy
import pymysql

class DmozItem(scrapy.Item):
    title = scrapy.Field()
    zuozhe = scrapy.Field()
    chaodai = scrapy.Field()
    content=scrapy.Field()

class PoemSpider(scrapy.Spider):
    name="myPoemSpider"
    start_urls=['https://www.gushiwen.org/']
    def __init__(self):
        self.count=1
        self.conn=pymysql.connect("ip","username","password","poem",use_unicode=True, charset="utf8",port=3306)
        self.cursor = self.conn.cursor()
    def parse(self,response):

        NEXT_PAGE_SELECTOR='//div[@class="right"]/div[@class="sons"]/div[@class="cont"]/a[text()!="更多>>"]/@href'

        next_page = response.xpath(NEXT_PAGE_SELECTOR).extract()


        if next_page:
            for url in next_page:
                yield scrapy.Request(
                    response.urljoin(url),
                    callback=self.parse_element
            )

    def parse_element(self,response):
        NAME_DIV_SELECTOR='//div[@class="main3"]/div[@class="left"]/div[@class="sons"]/div[@class="typecont"]/span/a/@href'
        GUSHI_URL=response.xpath(NAME_DIV_SELECTOR).extract()

        if GUSHI_URL:
            for url in GUSHI_URL:
                yield scrapy.Request(
                    response.urljoin(url),
                    callback=self.parse_poem
                )

    def parse_poem(self,response):
        POEM_THEME_SELECTOR='//div[@class="main3"]/div[@class="left"]/div[@class="sons"]/div[@class="cont"]/h1/text()'
        POEM_ZUOZHE_CHAODAI_SELECTOR='//div[@class="main3"]/div[@class="left"]/div[@class="sons"]/div[@class="cont"]/p/a/text()'
        POEM_CONTENT_SELECTOR='//div[@class="main3"]/div[@class="left"]/div[@class="sons"]/div[@class="cont"]/div[@class="contson"]/text()'
        Item=DmozItem()
        Item["title"]=response.xpath(POEM_THEME_SELECTOR).extract()
        Item["zuozhe"] = response.xpath(POEM_ZUOZHE_CHAODAI_SELECTOR).extract()[1]
        Item["chaodai"] = response.xpath(POEM_ZUOZHE_CHAODAI_SELECTOR).extract()[0]
        Item["content"] = response.xpath(POEM_CONTENT_SELECTOR).extract()
        string=Item["content"]
        string = str(string)
        string = string.replace('\'', ' ');
        string = string.replace('[', ' ');
        string = string.replace(',', ' ');
        string = string.replace('(', ' ');
        string = string.replace(']', ' ');
        string = string.replace(')', ' ');
        string = string.replace('\\n', ' ');
        string = string.replace(' ', '');
        string.strip()
        print("title",Item["title"])
        print("zuozhe", Item["zuozhe"])
        print("chaodai", Item["chaodai"])
        print("content", Item["content"])
        print("---------------------------------------------")
        try:

            self.cursor.execute("""INSERT INTO my_poem (name, zuozhe,chaodai,content)
                                            VALUES (%s, %s,%s,%s)""",
                                            (Item["title"],
                                             Item['zuozhe'],
                                             Item['chaodai'],
                                             string))
            self.conn.commit()
        except pymysql.Error as e:
            print("Error %d: %s" % (e.args[0], e.args[1]))
