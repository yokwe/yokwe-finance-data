package yokwe.finance.data.provider;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.jita.StorageJITA;
import yokwe.finance.data.provider.jpx.StorageJPX;
import yokwe.finance.data.stock.us.StorageUS;
import yokwe.util.ClassUtil;
import yokwe.util.FileUtil;
import yokwe.util.StackWalkerUtil;
import yokwe.util.Storage.LoadSaveFileList;
import yokwe.util.UnexpectedException;

public abstract class UpdateBase {
	static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	protected static final int EX_OK    = 0;
	protected static final int EX_ERROR = 1;
	
	public abstract void update();
	
	public static void callUpdate() {
		Class<?> callerClass = StackWalkerUtil.getCallerStackFrame(StackWalkerUtil.OFFSET_CALLER).getDeclaringClass();		
		// sanity check
		if (!UpdateBase.class.isAssignableFrom(callerClass)) {
			logger.error("Unexpctected callerClass");
			logger.error("  callerClass  {}", callerClass.getTypeName());
			throw new UnexpectedException("Unexpctected callerClass");
		}
		
		callUpdate((UpdateBase)ClassUtil.getInstance(callerClass));
	}
	public static void callUpdate(UpdateBase updateBase) {
		logger.info("START    {}", updateBase.getClass().getTypeName());
		int exitCode = EX_ERROR;
		try {
			updateBase.update();
			exitCode = EX_OK;
		} catch (Throwable e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e.toString());
			var sw = new StringWriter();
			var pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			logger.info("====");
			logger.error("{}", sw.toString());
			logger.info("====");
		}
		logger.info("STOP  {}  {}", exitCode, updateBase.getClass().getTypeName());
		System.exit(exitCode);
	}
	
	protected Duration gracePeriod = Duration.ofHours(3);
	protected boolean needsUpdate(File file) {
		if (file.exists()) {
			var fileDuration = FileUtil.getLastModifiedDuration(file);
			if (fileDuration.compareTo(gracePeriod) < 0) return false; // skip this file. because the file is in grace period
		}
		return true;
	}
	
	public <E> void checkDuplicateKey(List<E> list, Function<E, String> opKey) {
		var map = new HashMap<String, E>();
		for(var e: list) {
			var key = opKey.apply(e);
			if (map.containsKey(key)) {
				// duplicate
				var oldValue = map.get(key);
				var newValue = e;
				logger.error("Duplicate key");
				logger.error("  new  {}", newValue.toString());
				logger.error("  old  {}", oldValue.toString());
				throw new UnexpectedException("Duplicate key");
			} else {
				map.put(key, e);
			}
		}
	}
	
	
	//
	// save list to LoadSaveFileList
	//
	public <E extends Comparable<E>> void checkAndSave(List<E> list, LoadSaveFileList<E> loadSave) {
		var oldString = loadSave.read();
		var newString = loadSave.read(list);
		
		if (newString.equals(oldString)) {
			logger.info("no needs to update file");
		} else {
			logger.info("copy to  {}", loadSave.getOldFile());
			loadSave.copyToOldFile();

			save(list, loadSave);
		}
	}
	public <E extends Comparable<E>> void checkAndSave(Collection<E> collection, LoadSaveFileList<E> loadSave) {
		checkAndSave(new ArrayList<E>(collection), loadSave);
	}
	
	public <E extends Comparable<E>> void save(List<E> list, LoadSaveFileList<E> loadSave) {
		logger.info("save  {}  {}", list.size(), loadSave.getFile());
		loadSave.save(list);
	}
	public <E extends Comparable<E>> void save(Collection<E> collection, LoadSaveFileList<E> loadSave) {
		save(new ArrayList<E>(collection), loadSave);
	}

	
	private <E> void checkCode(Set<String> set, List<E> list, Function<E, String> opCode) {
		for(var e: list) {
			var code = opCode.apply(e);
			if (set.contains(code)) continue;
			logger.error("Unexpected code");
			logger.error("  code   {}", code);
			logger.error("  valeu  {} ", e.toString());
			throw new UnexpectedException("Unexpected code");
		}
	}
	protected <E> void checkStockCodeJP(List<E> list, Function<E, String> opCode) {
		var set = StorageJPX.StockCodeName.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
		checkCode(set, list, opCode);
	}
	protected <E> void checkStockCodeUS(List<E> list, Function<E, String> opCode) {
		var set = StorageUS.StockInfo.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
		checkCode(set, list, opCode);
	}
	protected <E> void checkFundCodeJP(List<E> list, Function<E, String> opCode) {
		var set = StorageJITA.FundInfo.getList().stream().map(o -> o.fundCode).collect(Collectors.toSet());
		checkCode(set, list, opCode);
	}
}
