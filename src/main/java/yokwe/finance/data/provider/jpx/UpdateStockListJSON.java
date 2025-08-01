package yokwe.finance.data.provider.jpx;

import java.io.File;
import java.util.ArrayList;

import yokwe.finance.data.provider.Makefile;
import yokwe.finance.data.provider.UpdateBase;
import yokwe.finance.data.type.CodeName;
import yokwe.finance.data.type.StockCodeJP;
import yokwe.util.FileUtil;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;


/*
public class UpdateStockListJSON extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input().
		output(StorageJPX.StockList).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	public static File[] getDownloadFiles() {
		return StorageJPX.StockListJSON.getDir().listFiles(o -> o.isFile() && o.getName().endsWith(".json"));
	}
	
	@Override
	public void update() {
		// delete files in StorageJPX.StockListJSON
		FileUtil.deleteFile(getDownloadFiles());
		
		// download files
		{
			int pagecount = 99;
			
			int count = 0;
			for(var page = 1; page <= pagecount; page++) {
				if ((++count % 10) == 1) logger.info("{}  /  {}", page, pagecount);

				var string = downloadFile(page);
				// save
				StorageJPX.StockListJSON.save(String.format("%03d", page), string);
				
				if (page == 1) {
					// set pagecount from page content
					var result = JSON.unmarshal(StockList.class, string);
					pagecount = result.section1.pagecount;
					logger.info("pagecount  {}", pagecount);
				}
			}
			StorageJPX.StockListJSON.touch();
		}
		
		// build CodeName from download files
		{
			var list = new ArrayList<CodeName>();
			
			for(var file: getDownloadFiles()) {
				var string = FileUtil.read().file(file);
				var result = JSON.unmarshal(StockList.class, string);
				for(var e: result.section1.data) {
					if (e.LISS.equals("TPM")) continue;
					var stockCode = StockCodeJP.toStockCode5(e.BICD);
					var name      = e.FLLN;
					list.add(new CodeName(stockCode, name));
				}
			}
			
			StorageJPX.StockListJSON.touch();
		}
	}
	private String downloadFile(int page) {
		var urlFormat = "https://quote.jpx.co.jp/jpxhp/jcgi/wrap/qjsonp.aspx?F=ctl/stock_list&page=%d&refindex=%%2BTTCODE&maxdisp=100";
		var referer   = "https://quote.jpx.co.jp/jpxhp/main/index.aspx?f=stock_list&key7=";
		var url       = String.format(urlFormat, page);
		
		return HttpUtil.getInstance().withReferer(referer).downloadString(url);
	}	
}
*/
