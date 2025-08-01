package yokwe.finance.data.provider;

import yokwe.finance.data.StorageFinanceData;
import yokwe.util.Storage;

public class StorageProvider {
	public static final Storage storage = StorageFinanceData.storage.getStorage("provider");
}