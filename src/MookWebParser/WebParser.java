package MookWebParser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class WebParser {
	
	public PrintWriter log;
	
	public WebParser() throws FileNotFoundException {
		
		log = new PrintWriter("thairath_news.doc");
	}
	
	public static void main(String[] args) throws IOException {
		
		String webname = "http://www.thairath.co.th/lifestyle/tech";
//		String webname = "https://jsoup.org/";
		
		WebParser w = new WebParser();
		ArrayList<WebContent> webInfo = w.getHeadLine(webname);
		
//		w.printLog(webInfo);
			
//		w.putDB(webInfo);
		
		index(webInfo);
		
		
		
		(w.log).flush();
		(w.log).close();
		
		System.out.println("Done.");
				
				
	}
	
	public ArrayList<WebContent> getHeadLine(String url) throws IOException {
		
		org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
		
		ArrayList<WebContent> infoList = new ArrayList<WebContent>();
		
		Elements headlines = doc.select("div.lastestNews div.cardRow div.card-block h4");
//		Elements contlines = doc.select("div.lastestNews div.cardRow div.card-block p");
		Elements headURLs = doc.select("div.lastestNews div.cardRow div.card-block h4 a");
//		Elements contURLs = doc.select("div.lastestNews div.cardRow div.card-block p a");
		
		for(int i = 0; i < headlines.size(); i++) {
			
			String absHref = headURLs.get(i).attr("abs:href");
			
			WebContent news = new WebContent();
			news.setHeadline(headlines.get(i).text());
			news.setURL(absHref);
			goToWeb(absHref,news);
			infoList.add(news);
			
		}

		return infoList;
	}
	
	public WebContent goToWeb(String url, WebContent news) throws IOException {
		
		org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
		
		Elements date = doc.select("section#headerContent time");
		
//		Date and Time
			news.setDate(date.text());
		
//		Content
		Elements content = doc.select("div.row section#mainContent article p");
			news.setContent(content.text());

//		Tags and URLs
		Elements tags = doc.select("div.row section#mainContent ul li");
		Elements tagURLs = doc.select("div.row section#mainContent ul li a");
		for(int i = 0; i < tags.size(); i++) {
			
			String absHref = tagURLs.get(i).attr("abs:href");
			news.addTags(tags.get(i).text(),absHref);
			
		}
		
//		Content images
		Elements images = doc.select("div.row section#mainContent img");
		
		for(int i = 0; i < images.size(); i++) {
			
			String absHref = images.get(i).attr("abs:src");
//			System.out.println("\t" + absHref);
			news.addPhotoURLs(absHref);
		}
		
		System.out.println("Done get News");
		
		return news;
	}
	
	public void printLog(ArrayList<WebContent> webInfo) {
		
		for(int i = 0; i < webInfo.size(); i++) {
			
			log.printf("%d. %s\n", (i+1), (webInfo.get(i)).getHeadline());
			log.printf("\tURL: %s\n", (webInfo.get(i)).getURL());
			log.printf("\tContent: %s\n", (webInfo.get(i)).getContent());
			log.printf("\tDate/Time: %s \n", (webInfo.get(i)).getDate());
			log.printf("\tImage URLs: \n");
			for(String s: (webInfo.get(i)).getPhotoURLs()) {
				log.printf("%s\n", s);
			}
			log.printf("\tTags and Tag URLs: \n");
			int j = 1;
			for(String key: ((webInfo.get(i)).getTags()).keySet()){
				log.printf("%d. %s\n\t%s\n", j, key, ((webInfo.get(i)).getTags()).get(key));
				j++;
			}
			
			log.printf("\n");
			
		}
	}
	
	public void putDB(ArrayList<WebContent> webInfo) {
		
		MongoClient mongoClient = new MongoClient();	
		MongoDatabase database = mongoClient.getDatabase("thairathNews");
		MongoCollection<org.bson.Document> collection = database.getCollection("News");
		
		for(int i = 0; i < webInfo.size(); i++) {
			
			List<Integer> books = Arrays.asList(27464, 747854);
			org.bson.Document article = new org.bson.Document("headline", (webInfo.get(i)).getHeadline()).append("URL", (webInfo.get(i)).getURL())
	                .append("content", (webInfo.get(i)).getContent())
	                .append("date-time", (webInfo.get(i)).getDate());
			
			System.out.println("collecting article");;
			collection.insertOne(article);
			
		}
		
		mongoClient.close();
	}
	
	public static void index(ArrayList<WebContent> webInfo) throws IOException {
		
		Path path = FileSystems.getDefault().getPath("dbnewsindex");
		Directory dir = FSDirectory.open(path);
		
		/*set analyzer to analyze contents*/
		Analyzer analyzer = new ThaiAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		
		/*always replace old index*/
		iwc.setOpenMode(OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(dir,iwc);
		
		System.out.println("writing index");
		
		for(int i = 0; i < webInfo.size(); i++) {
			org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
			
			doc.add(new org.apache.lucene.document.Field("headline", (webInfo.get(i)).getHeadline(), TextField.TYPE_STORED));
			doc.add(new org.apache.lucene.document.Field("url", (webInfo.get(i)).getURL(), StringField.TYPE_STORED));
			doc.add(new org.apache.lucene.document.Field("date_time", (webInfo.get(i)).getDate(), StringField.TYPE_STORED));
			doc.add(new org.apache.lucene.document.Field("content", (webInfo.get(i)).getContent(), TextField.TYPE_STORED));
					
			writer.addDocument(doc);
			
		}
		 
		
		writer.close();
	}
}
