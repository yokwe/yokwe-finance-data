package yokwe.finance.data.provider.yahoo;

import java.util.Collections;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.Makefile;
import yokwe.finance.data.provider.UpdateBase;
import yokwe.finance.data.stock.us.StorageUS;
import yokwe.finance.data.type.CompanyInfo;
import yokwe.finance.data.type.StockInfoUS;
import yokwe.util.Storage;


public class UpdateCompanyInfoUS extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
//		input(StorageUS.StockInfo). // FIXME circular dependency
		output(StorageYahoo.CompanyInfoUS).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		Storage.initialize();
		
		for(int take = 1; take < 9; take++) {
			logger.info("start take {}", take);
			int countMod = updateCompanyInfo();
			if (countMod == 0) break;
			try {
				Thread.sleep(SLEEP_IN_MILLI);
			} catch (InterruptedException e) {
				//
			}
		}
	}
	private static final long    SLEEP_IN_MILLI = 1500;
	
	private int updateCompanyInfo() {
		// read existing data
		var list = StorageYahoo.CompanyInfoUS.getList().stream().collect(Collectors.toList());
		logger.info("companyInfo  {}", list.size());
		// remove if sector or industry is empty
		list.removeIf(o -> o.sector.isEmpty() || o.industry.isEmpty());
		logger.info("companyInfo  {}  remove if sector or industry is empty", list.size());
		
		// set of required stockCode
		var stockInfoList = StorageUS.StockInfo.getList();
		logger.info("stockInfo    {}", stockInfoList.size());
		// remove if not stock
		stockInfoList.removeIf(o -> !o.type.isStock());
		logger.info("stockInfo    {}  remove if not stock", stockInfoList.size());
		// remove if already processed
		{
			var set = list.stream().map(o -> o.stockCode).collect(Collectors.toSet());
			stockInfoList.removeIf(o -> set.contains(o.stockCode));
		}
		logger.info("stockInfo    {}  remove if already processed", stockInfoList.size());
		
		Collections.shuffle(stockInfoList);
		int count = 0;
		int countMod = 0;
		for(var stockInfo: stockInfoList) {
			if ((++count % 100) == 1) logger.info("{}  /  {}", count, stockInfoList.size());
			
			try {
				Thread.sleep(SLEEP_IN_MILLI);
			} catch (InterruptedException e) {
				//
			}
			
			var stockCode = stockInfo.stockCode;
			
			var companyInfo = CompanyInfoYahoo.getInstance(StockInfoUS.toYahooSymbol(stockCode));
			if (companyInfo == null) continue;
			
			var sector   = companyInfo.sector.replace(",", "").replace("—", "-");
			var industry = companyInfo.industry.replace(",", "").replace("—", "-");
			
			// skipe if sector or industry is empty
			if (sector.isEmpty() || industry.isEmpty()) continue;
			
			list.add(new CompanyInfo(stockCode, sector, industry));
			countMod++;
			
			if ((countMod % 10) == 1) StorageYahoo.CompanyInfoUS.save(list);
		}
		
		logger.info("countMod  {}", countMod);
		checkDuplicateKey(list, o -> o.stockCode);
		checkAndSave(list, StorageYahoo.CompanyInfoUS);
		
		return countMod;
	}
}
