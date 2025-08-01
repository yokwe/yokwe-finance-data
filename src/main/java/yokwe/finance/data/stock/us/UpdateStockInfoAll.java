package yokwe.finance.data.stock.us;

import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.Makefile;
import yokwe.finance.data.provider.UpdateBase;
import yokwe.finance.data.provider.bats.StorageBATS;
import yokwe.finance.data.provider.nasdaq.StorageNASDAQ;
import yokwe.finance.data.provider.nyse.StorageNYSE;
import yokwe.finance.data.type.StockInfoUS;

public class UpdateStockInfoAll extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input(StorageBATS.StockInfo, StorageNASDAQ.StockInfo, StorageNYSE.StockInfo).
		output(StorageUS.StockInfoAll).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	public void update() {
		var listBATS   = StorageBATS.StockInfo.getList();
		var listNASDAQ = StorageNASDAQ.StockInfo.getList();
		var listNYSE   = StorageNYSE.StockInfo.getList();
		logger.info("BATS   {}", listBATS.size());
		logger.info("NASDAQ {}", listNASDAQ.size());
		logger.info("NYSE   {}", listNYSE.size());
		
		var set = new TreeSet<StockInfoUS>();
		set.addAll(listBATS);
		set.addAll(listNASDAQ);
		set.addAll(listNYSE);
		
		var list = set.stream().collect(Collectors.toList());
		
		checkDuplicateKey(list, o -> o.stockCode);
		checkAndSave(list, StorageUS.StockInfoAll);
	}
}
