package yokwe.finance.data.provider.nyse;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.StockInfoUS;
import yokwe.util.Storage;

public class StorageNYSE {
	public static final Storage storage = StorageProvider.storage.getStorage("nyse");
	
	// stock-info-nasdaq
	public static final Storage.LoadSaveFileList<StockInfoUS> StockInfo =
		new Storage.LoadSaveFileList<StockInfoUS>(StockInfoUS.class, storage, "stock-info.csv");
	
	// stock-info-nasdaq
	public static final Storage.LoadSaveFileList<Filter> Filter =
		new Storage.LoadSaveFileList<Filter>(Filter.class,  storage, "filter.csv");
}
