package yokwe.finance.data.provider.moneybu;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.util.Storage;

public class StorageMoneybu {
	public static final Storage storage = StorageProvider.storage.getStorage("moneybu");
	
	public static final Storage.LoadSaveFileList<ETFInfo> ETFInfo =
		new Storage.LoadSaveFileList<ETFInfo>(ETFInfo.class, storage, "etf-info.csv");
	
	public static final Storage.LoadSaveFileList<ETF> ETF =
		new Storage.LoadSaveFileList<ETF>(ETF.class, storage, "etf.csv");
}
