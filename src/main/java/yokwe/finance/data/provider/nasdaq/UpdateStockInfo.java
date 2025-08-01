package yokwe.finance.data.provider.nasdaq;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import yokwe.finance.data.provider.Makefile;
import yokwe.finance.data.provider.UpdateList;
import yokwe.finance.data.type.StockInfoUS;
import yokwe.finance.data.type.StockInfoUS.Market;
import yokwe.finance.data.type.StockInfoUS.Type;
import yokwe.util.CSVUtil;
import yokwe.util.FTPUtil;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class UpdateStockInfo extends UpdateList<NasdaqListed> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input().
		output(StorageNASDAQ.StockInfo).
		build();
	
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	protected List<NasdaqListed> downloadFile() {		
		String string;
		{
			byte[] data = FTPUtil.downloadRaw(URL_NASDAQLISTED);
			if (data == null) {
				logger.error("Download failed  {}", URL_NASDAQLISTED);
				throw new UnexpectedException("Download failed");
			}
			
			string = new String(data, StandardCharsets.US_ASCII);
			
			var file = StorageNASDAQ.NasdaqListed.getFile("nasdaqlisted.txt");
			FileUtil.write().file(file, string);
		}
		List<NasdaqListed>list;
		{
			String[] lines = string.split("[\\r\\n]+");
			
			// remove last line
			String csvString = String.join("\n", Arrays.copyOfRange(lines, 0, lines.length - 1)) + "\n";
			// read string as csv file
			list = CSVUtil.read(NasdaqListed.class).withSeparator('|').file(new StringReader(csvString));
		}
				
		checkDuplicateKey(list, o -> o.symbol);
		// save csv file
		StorageNASDAQ.NasdaqListed.save(list);
		return list;
	}
	public static final String URL_NASDAQLISTED = "ftp://ftp.nasdaqtrader.com/symboldirectory/nasdaqlisted.txt";
	
	@Override
	protected void updateFile(List<NasdaqListed> stockList) {
		List<StockInfoUS> list = new ArrayList<>();
		
		int countTotal = 0;
		int countSkip  = 0;
		
		for(var e: stockList) {
			countTotal++;
			
			// skip test issue, right, unit and warrant
			if (e.isTestIssue() || e.isRights() || e.isUnits() || e.isWarrant()) {
				countSkip++;
				continue;
			}
			// skip warrant and beneficial interest
			var upperCaseName = e.name.toUpperCase();
			if (upperCaseName.contains("WARRANT") || upperCaseName.contains("BENEFICIAL INTEREST")) {
				countSkip++;
				continue;
			}
			// skip financial status is not normal
//			if (!e.isFinancialNormal()) {
//				logger.info("skip  financial status  {}  {}  {}", e.financialStatus, e.symbol, e.name);
//				countSkip++;
//				continue;
//			}
			
			String symbol   = e.symbol;
			Market market   = Market.NASDAQ;
			Type   type     = e.etf.equals("Y") ? Type.ETF : Type.COMMON; // just ETF or COMMON for now
			String sector   = "*DUMMY*"; // set dummy value for now
			String industry = "*DUMMY*"; // set dummy value for now
			String name     = e.name.replace(",", "").toUpperCase(); // use upper case

			list.add(new StockInfoUS(symbol, market, type, sector, industry, name));
		}
		
		logger.info("total  {}", countTotal);
		logger.info("skip   {}", countSkip);
		
		// save
		checkAndSave(list, StorageNASDAQ.StockInfo);
	}
}
