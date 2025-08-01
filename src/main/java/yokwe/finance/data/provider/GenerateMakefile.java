package yokwe.finance.data.provider;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.TreeSet;

import yokwe.util.FileUtil;
import yokwe.util.Storage;

public class GenerateMakefile {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		var moduleName = "yokwe.finance.data";
		var file       = new File("tmp/update-data.make");
		
		logger.info("moduleName  {}", moduleName);
		
		var string = generate(moduleName);
		
		logger.info("save  {}  {}", string.length(), file.getAbsoluteFile());
		FileUtil.write().file(file, string);
		
		logger.info("STOP");
	}
	
	public static String generateMakefile() {
		return generate("yokwe.finance.data");
	}
	public static String generate(String moduleName) {
		var list = Makefile.scanModule(moduleName);

		var groupNameSet = new TreeSet<String>();
		list.stream().forEach(o -> groupNameSet.add(o.group));
		var groupUpdateSet = new TreeSet<String>();
		groupNameSet.forEach(o -> groupUpdateSet.add("update-" + o));
		
		var sw = new StringWriter();		
		try (var out = new PrintWriter(sw)) {
			out.println(
			"""
#
# update-data.make
#
DATA_PATH_FILE := data/DataPathLocation
DATA_PATH_     := $(shell cat $(DATA_PATH_FILE))
FINANCE_PATH   := $(DATA_PATH_)finance


.PHONY: all check-finance-path

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
			out.println("# module " + moduleName);
			out.println("#");
			out.println();
			out.println(".PHONY: update-data " + String.join(" ", groupUpdateSet));
			out.println();
			out.println("#");
			out.println("# update-data");
			out.println("#");
			out.println("update-data: \\");
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
		
		var storageFinance = Storage.storage.getStorage("finance");
		var string = sw.toString().replace(storageFinance.getFile().getAbsolutePath(), "$(FINANCE_PATH)");

		return string;
	}
	
}
