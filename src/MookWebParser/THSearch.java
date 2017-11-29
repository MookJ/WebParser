package MookWebParser;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class THSearch {

	public static void main(String [] args) throws IOException, ParseException{
		
		
		Path path = FileSystems.getDefault().getPath("dbnewsindex"); // use the index folder
		Directory dir = FSDirectory.open(path);
		Analyzer analyzer = new ThaiAnalyzer(); // must be the same analyzer as used in index
		IndexReader r = DirectoryReader.open(dir);
		
		IndexSearcher searcher = new IndexSearcher(r);
		
		String keyword = "การลงทุน";
		QueryParser qp = new QueryParser("content", analyzer); // find keyword in content field
		Query query = qp.parse(keyword);
		
		TopDocs tops = searcher.search(query, 5);
		ScoreDoc[] sd = tops.scoreDocs;
		
		// after found keyword in all content field, display its fields
		for(ScoreDoc s: sd) {
			Document d = searcher.doc(s.doc);
			String headline = d.get("headline");
			String content = d.get("content");
			System.out.println(headline);
			System.out.println(content);
		}
		
	}
}
