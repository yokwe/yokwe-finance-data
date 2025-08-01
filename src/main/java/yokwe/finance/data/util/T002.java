package yokwe.finance.data.util;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import yokwe.finance.data.provider.Makefile;
import yokwe.util.FileUtil;
import yokwe.util.Storage;

public class T002 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void analyze(List<Makefile> list) {
		var outputSet = new TreeSet<File>();
		var inputSet  = new TreeSet<File>();
		for(var e: list) {
			outputSet.addAll(Arrays.asList(e.outputs));
			inputSet.addAll(Arrays.asList(e.inputs));
		}
		
		{
			var s = new TreeSet<File>(outputSet);
			s.removeAll(inputSet);
			
			if (!s.isEmpty()) {
				logger.info("appeared only outputs");
				for(var e: s) {
					logger.info(" {}", e);
				}
			}
		}
		{
			var s = new TreeSet<File>(inputSet);
			s.removeAll(outputSet);
			if (!s.isEmpty()) {
				logger.info("appeared only inputs");
				for(var e: s) {
					logger.info(" {}", e);
				}
			}
		}
	}
	
	public static String generateMakefile(List<Makefile> list) {
		var storageFinance = Storage.storage.getStorage("finance");
		logger.info("storageFinance  {}", storageFinance.getFile());
		
		var groupNameSet = new TreeSet<String>();
		list.stream().forEach(o -> groupNameSet.add(o.group));
		var groupUpdateSet = new TreeSet<String>();
		groupNameSet.forEach(o -> groupUpdateSet.add("update-" + o));
		
		var sw = new StringWriter();		
		try (var out = new PrintWriter(sw)) {
			out.println(
			"""
#
#
#
DATA_PATH_FILE := data/DataPathLocation
DATA_PATH_     := $(shell cat $(DATA_PATH_FILE))
FINANCE_PATH   := $(DATA_PATH_)finance


all: check-finance-path
	@echo "DATA_PATH                 $(DATA_PATH_)"
	@echo "FINANCE_PATH              $(FINANCE_PATH)"

check-finance-path:
#	@echo "DATA_PATH_FILE  !$(DATA_PATH_FILE)!"
#	@echo "DATA_PATH       !$(DATA_PATH)!"
	@if [ ! -d $(FINANCE_PATH) ]; then \
		echo "FINANCE_PATH  no directory  !${FINANCE_PATH}!" ; \
		exit 1 ; \
	fi

			""");
			
			
			
			out.println("#");
			out.println("# update-all");
			out.println("#");
			out.println("update-all: \\");
			out.println("\t" + String.join(" \\\n\t", groupUpdateSet));
			out.println();
			out.println();
			
			for(var group: groupNameSet) {
				var makeList = list.stream().filter(o -> o.group.equals(group)).toList();
				var outFileSet = new TreeSet<String>();
				for(var e: makeList) {
					Arrays.stream(e.outputs).forEach(o -> outFileSet.add(o.getAbsolutePath()));
				}
				
				out.println("#");
				out.println("# " + group);
				out.println("#");
				out.println("update-" + group + ": \\");
				out.println("\t" + String.join(" \\\n\t", outFileSet));
				out.println();
				
				for(var e: makeList) {
					var iList = Arrays.stream(e.inputs).map(o -> o.getAbsolutePath()).toList();
					var oList = Arrays.stream(e.outputs).map(o -> o.getAbsolutePath()).toList();
					
					if (iList.isEmpty()) {
						out.println(String.join(" ", oList) + ":");
					} else {
						out.println(String.join(" ", oList) + ": \\");
						out.println("\t" + String.join(" \\\n\t", iList));
					}
					out.println("\tant " + e.target);
				}
				
				out.println();
				out.println();
			}
		}
		
		var string = sw.toString().replace(storageFinance.getFile().getAbsolutePath(), "$(FINANCE_PATH)");

		return string;
	}
	public static void main(String[] args) {
		var moduleName  = "yokwe.finance.data";
		
		var list = Makefile.scanModule(moduleName);
		logger.info("list  {}", list.size());
		
		{
			var file = new File("tmp/ant-target.make");
			var string = generateMakefile(list);
			FileUtil.write().file(file, string);
		}

		
		analyze(list);
	}
}
