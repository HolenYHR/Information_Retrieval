package test;



import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
//import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.mysql.jdbc.Statement;

public class Indexer {
	
	// JDBC 驱动名及数据库 URL
	  static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	  static final String DB_URL = "jdbc:mysql://ip:3306/poem";
	 
	  // 数据库的用户名与密码，需要根据自己的设置
	  static final String USER = "xxxx";
	  static final String PASS = "xxxx";
	
	
	
    /*private String ids[]={"1","2","3"};
    private String citys[]={"青岛","南京","上海"};
    private String descs[]={
            "青岛是一个美丽的城市。",
            "南京是一个有文化的城市。南京是一个文化的城市南京，简称宁，是江苏省会，地处中国东部地区，长江下游，濒江近海。全市下辖11个区，总面积6597平方公里，2013年建成区面积752.83平方公里，常住人口818.78万，其中城镇人口659.1万人。[1-4] “江南佳丽地，金陵帝王州”，南京拥有着6000多年文明史、近2600年建城史和近500年的建都史，是中国四大古都之一，有“六朝古都”、“十朝都会”之称，是中华文明的重要发祥地，历史上曾数次庇佑华夏之正朔，长期是中国南方的政治、经济、文化中心，拥有厚重的文化底蕴和丰富的历史遗存。[5-7] 南京是国家重要的科教中心，自古以来就是一座崇文重教的城市，有“天下文枢”、“东南第一学”的美誉。截至2013年，南京有高等院校75所，其中211高校8所，仅次于北京上海；国家重点实验室25所、国家重点学科169个、两院院士83人，均居中国第三。[8-10] 。",
            "上海是一个繁华的城市。"
    };*/
    
    private IndexWriter writer;
    /**
     * 实例化IndexWriter，这里使用中文分词器SmartChineseAnalyzer
     * @param indexDir 存放索引的目录
     * @return
     * @throws IOException
     */
    public IndexWriter getIndexWriter(String indexDir) throws IOException {
        Directory dir=FSDirectory.open(Paths.get(indexDir));
//        Analyzer analyzer=new StandardAnalyzer();
        SmartChineseAnalyzer analyzer=new SmartChineseAnalyzer();
        IndexWriterConfig conf=new IndexWriterConfig(analyzer);
        IndexWriter writer=new IndexWriter(dir, conf);
        return writer;
    }
    
    public void index(String indexDir) throws IOException {
        this.writer=getIndexWriter(indexDir);
        
        Connection conn = null;
        Statement stmt = null;
        try{
            // 注册 JDBC 驱动
            Class.forName("com.mysql.jdbc.Driver");
        
            // 打开链接
            //System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
        
            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = (Statement) conn.createStatement();
            String sql;
            sql = "SELECT id, name, zuozhe, chaodai,content FROM my_poem";
            ResultSet rs = stmt.executeQuery(sql);
        
            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                int id  = rs.getInt("id");
                String name = rs.getString("name");
                String zuozhe = rs.getString("zuozhe");
                String chaodai=rs.getString("chaodai");
                String content=rs.getString("content");
                // 输出数据
                //System.out.print("id: " + id);
                //System.out.print(", 题目: " + name);
                //System.out.print(", 作者: " + zuozhe);
                //System.out.print(", 朝代: " + chaodai);
               // System.out.print(", 内容: " + content);
                //System.out.print("\n");
                
                addDoc(Integer.toString(id), name,zuozhe,chaodai,content);
                //addDoc(w, "Lucene for Dummies", "55320055Z");
                //addDoc(w, "Managing Gigabytes", "55063554A");
                //addDoc(w, "The Art of Computer Science", "9900333X");
                System.out.println("hello world");
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        System.out.println("Goodbye!");  
  	  
        writer.close();
    }
    
    private void addDoc(String id, String name,String zuozhe, String chaodai,String content) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("id",id, Field.Store.YES));
     
        // use a string field for isbn because we don't want it tokenized
        doc.add(new TextField("name", name, Field.Store.YES));
        doc.add(new TextField("zuozhe",zuozhe, Field.Store.YES));
        doc.add(new TextField("chaodai", chaodai, Field.Store.YES));
        doc.add(new TextField("content", content, Field.Store.YES));
        
        
        writer.addDocument(doc);
      }
    
    public static void main(String[] args) {
        try {
            new Indexer().index("index3");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
