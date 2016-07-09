package crawler.example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.abola.crawler.CrawlerPack;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedMap;

/**
 * 補充教材 蘋果即時新聞
 * 重點：
 * 	1. regular expression 使用
 * 	2. 使用Guava套件，強化許多難以實現的功能
 * 
 * 
 * @author Abola Lee
 *
 */
/*Testtt*/
public class AppleRealtime {
	
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH:mm");
	
	public static void main(String[] args) throws Exception{
		
		// 遠端資料路徑
		String uri = "http://www.appledaily.com.tw/realtimenews/section/new/";

//		取出即時新聞列表
//		data sample
//		----
//		<li class="rtddt polit">
//			<a href="/realtimenews/article/politics/20160703/899807/【更新】最新民調　74.6認同飛彈誤射是國軍螺絲鬆" target="_blank">
//			<time>13:11</time>
//			<h2>政治</h2>
//			<h1><font color="#ff0000">【更新】最新民調　74.6%認同飛彈誤射...(4580)</font></h1>
//			</a>
//		</li>

		Elements newsList = CrawlerPack.start()
		    .getFromHtml(uri)
		    .select(".rtddt");
		
		// Guava 專有物件，可為Map key排序
		// 稍後用於排序還原比重後的新聞排序
		ImmutableSortedMap.Builder<String, String> newsMap =
				ImmutableSortedMap.reverseOrder();
		
		for( Element news: newsList){
//			 資料sample
//			<li class="rtddt sport even">
//	            <a href="/realtimenews/article/sports/20160325/824605/【更新】好兆頭！　陽岱鋼單場第2安出爐" target="_blank">
//	                <time>19:54</time>
//	                <h2>體育</h2>
//	                <h1><font color="#ff0000"> 【更新】好兆頭！　陽岱鋼單場第2安出爐(7234)</font></h1>
//	            </a>
//            </li>		

			// 如果不想要更新的資料
			if ( news.select("h1 > font:matchesOwn(【更新】)").size() > 0 ) continue; 
			
			// 取出年月日時分
			String yyyymmdd = news.select("a").attr("href").replaceAll("^(.*\\/)([0-9]{8})(\\/.*)$", "$2") ;
			String hhmm = news.select("time").text();
			
			// 計算發佈時間與目前時間的差異分鐘數
			Date newTime = sdf.parse(yyyymmdd + hhmm);
			Integer diff = (int)(long)( new Date().getTime() - newTime.getTime() )/1000/60 +1 ;
				
			// 取出目前點閱數
			Integer counter = 0;
			String count = news.select("h1 > font").text().replaceAll("^(.*\\()([0-9]+)(\\).*)$", "$2") ;

			// 空白或無點閱數資料處理
			if ( count == null || count.trim() == ""  )
				counter = 0;
			else{
				try {
					counter = Integer.valueOf( count );
				}catch(java.lang.NumberFormatException nfe){
					counter = 0;
				}
			}

			// 計算簡易效率值
			Integer effect =  counter / diff;

			
			// 新聞標題
			String title = news.select("h1 > font").text();
			
			// Guava 強化 function  Strings.padStart 
			//       用來補足字串左方不足的字元
			//       ex:    1234
			//           0001234
			String key = Strings.padStart(effect.toString(), 6, '0')
						 // 加上title 以防key 值重覆
					     +title;
			
			newsMap.put( key, effect+":"+title);
			
		}
		
		Map<String, String> orderedNew = newsMap.build();
		
		// 遂行印出排序後的新聞
		for(String key: orderedNew.keySet() ){
			System.out.println( orderedNew.get(key) );
		}
		
	}
	
}
