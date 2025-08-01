package yokwe.finance.data.provider.nasdaq;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.StockInfoUS;
import yokwe.util.Storage;

public class StorageNASDAQ {
	public static final Storage storage = StorageProvider.storage.getStorage("nasdaq");
	
	// stock-info-nasdaq
	public static final Storage.LoadSaveFileList<StockInfoUS> StockInfo =
		new Storage.LoadSaveFileList<StockInfoUS>(StockInfoUS.class, storage, "stock-info.csv");
	
	// nasdqqlisted
	public static final Storage.LoadSaveFileList<NasdaqListed> NasdaqListed =
		new Storage.LoadSaveFileList<NasdaqListed>(NasdaqListed.class, storage, "nasdaqlisted.csv");
	
	// otherlisted
	public static final Storage.LoadSaveFileList<OtherListed> OtherListed =
		new Storage.LoadSaveFileList<OtherListed>(OtherListed.class, storage, "otherlisted.csv");
}
