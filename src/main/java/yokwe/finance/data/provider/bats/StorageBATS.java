package yokwe.finance.data.provider.bats;

import yokwe.finance.data.provider.StorageProvider;
import yokwe.finance.data.type.StockInfoUS;
import yokwe.util.Storage;

public class StorageBATS {
	public static final Storage storage = StorageProvider.storage.getStorage("bats");
	
	// stock-info-bats
	public static final Storage.LoadSaveFileList<StockInfoUS> StockInfo =
		new Storage.LoadSaveFileList<StockInfoUS>(StockInfoUS.class, storage, "stock-info.csv");
	
	// listed-security-report
	public static final Storage.LoadSaveFileList<ListedSecurityReport> ListedSecurityReport =
		new Storage.LoadSaveFileList<ListedSecurityReport>(ListedSecurityReport.class, storage, "listed-security-report.csv");
}
