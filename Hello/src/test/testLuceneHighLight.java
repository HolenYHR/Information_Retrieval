package test;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import com.mysql.jdbc.Statement;

import java.awt.print.Printable;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
 
public class testLuceneHighLight {
	
  // JDBC 驱动名及数据库 URL
  static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
  static final String DB_URL = "jdbc:mysql://101.76.204.9:3306/poem";
 
  // 数据库的用户名与密码，需要根据自己的设置
  static final String USER = "haoran";
  static final String PASS = "yanghaoran2015";
    
  public static void main(String[] args) throws IOException, ParseException {
    // 0. Specify the analyzer for tokenizing text.
    //    The same analyzer should be used for indexing and searching
	  
	  StandardAnalyzer analyzer = new StandardAnalyzer();
	  
	  // 1. create the index
	  //Directory index = new RAMDirectory();
	  Directory index = FSDirectory.open(Paths.get("/tmp/testindex"));
	  IndexWriterConfig config = new IndexWriterConfig(analyzer);
	    
	  IndexWriter w = new IndexWriter(index, config);
	  
	  
	  
	  
	  
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
              
              addDoc(w,Integer.toString(id), name,zuozhe,chaodai,content);
              //addDoc(w, "Lucene for Dummies", "55320055Z");
              //addDoc(w, "Managing Gigabytes", "55063554A");
              //addDoc(w, "The Art of Computer Science", "9900333X");
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
      //System.out.println("Goodbye!");  
	  
	
	  
   
     
    w.close();
 
    // 2. query
    String querystr = args.length > 0 ? args[0] : "春";
 
    // the "title" arg specifies the default field to use
    // when no field is explicitly specified in the query.
    Query q = new QueryParser("content", analyzer).parse(querystr);
   
    // 3. search
    int hitsPerPage = 20;
    IndexReader reader = DirectoryReader.open(index);
    
    
   
    
    IndexSearcher searcher = new IndexSearcher(reader);
    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
    searcher.search(q, collector);
    ScoreDoc[] hits = collector.topDocs().scoreDocs;
    
    // 4. display results
    //System.out.println("Found " + hits.length + " hits.");
    for(int i=0;i<hits.length;++i) {
      int docId = hits[i].doc;
      Document d = searcher.doc(docId);
      System.out.println((i + 1) + ". " + d.get("id") + "\t" + d.get("name")+"\t"+ d.get("content"));
    }
 
    // reader can only be closed when there
    // is no need to access the documents any more.
    reader.close();
   
  }
 
  private static void addDoc(IndexWriter w, String id, String name,String zuozhe, String chaodai,String content) throws IOException {
    Document doc = new Document();
    doc.add(new TextField("id",id, Field.Store.YES));
 
    // use a string field for isbn because we don't want it tokenized
    doc.add(new TextField("name", name, Field.Store.YES));
    doc.add(new TextField("zuozhe",zuozhe, Field.Store.YES));
    doc.add(new TextField("chaodai", chaodai, Field.Store.YES));
    doc.add(new TextField("content", content, Field.Store.YES));
    
    
    w.addDocument(doc);
  }
}