package yokwe.finance.data.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.util.FileUtil;
import yokwe.util.Storage;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.graphviz.Dot;

public class GenerateDot {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		process();
		
		logger.info("STOP");
	}
	
	private static class Task {
		String       target;
		String       group;
		List<String> input  = new ArrayList<>();
		List<String> output = new ArrayList<>();
		
		Task(Makefile makefile, String pathRoot) {
			target = makefile.target;
			group  = makefile.group;
			for(var e: makefile.inputs)  input.add(e.getAbsolutePath().replace(pathRoot, ""));
			for(var e: makefile.outputs) output.add(e.getAbsolutePath().replace(pathRoot, ""));
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	private static void process() {
		var moduleName = "yokwe.finance.data";
		var file       = new File("tmp/dot/a.dot");
		
		var pathRoot = Storage.storage.getFile().getAbsolutePath() + "/";
		logger.info("rootPath  {}", pathRoot);
		
		logger.info("moduleName  {}", moduleName);
		var makeList = Makefile.scanModule(moduleName);
		logger.info("makeList  {}", makeList.size());
		
		var taskList =  makeList.stream().map(o -> new Task(o, pathRoot)).collect(Collectors.toList());
		
		var taskMap = taskList.stream().collect(Collectors.groupingBy(o -> o.group, TreeMap::new, Collectors.toCollection(ArrayList::new)));
		logger.info("taskMap  {}", taskMap.size());

		var fileSet = new TreeSet<String>();
		taskList.stream().forEach(o -> {fileSet.addAll(o.input); fileSet.addAll(o.output);});
		
		var fileMap = fileSet.stream().collect(Collectors.groupingBy(o -> (o.substring(0, o.lastIndexOf("/"))), TreeMap::new, Collectors.toCollection(ArrayList::new)));
		logger.info("fileMap  {}", fileMap.size());
		
		var groupList = new ArrayList<String>(taskMap.keySet());
				
		var g = new Dot.Digraph("G");
		g.attr("ranksep", "1").attr("nodesep", "0.5");
		g.attr("rankdir", "LR");
//		g.attr("splines", "spline");
//		g.attr("splines", "polyline");
		g.attr("fontname", "Migu 1M");
		
		g.nodeAttr().attr("fontname", "Migu 1M");
//		g.nodeAttr().attr("fontsize", "72");
		g.nodeAttr().attr("colorscheme", "set312");
//		g.nodeAttr().attr("colorscheme", "paired12");
		g.nodeAttr().attr("style", "filled");
		
		
		// output ant task
		for(var group: taskMap.keySet()) {
			for(var task: taskMap.get(group)) {
				var colorIndex = getColorNumber(groupList, group);
				g.node(task.target).attr("shape",  "box").attr("fillcolor", colorIndex);
			}
		}
		
		// output file
		for(var group: fileMap.keySet()) {
			for(var e: fileMap.get(group)) {
				var name       = e.substring(e.lastIndexOf("/") + 1);
				var colorIndex = getColorNumber(groupList, group);
				g.node(e).attr("label", group + "/\\n" + name).attr("shape", "oval").attr("fillcolor", colorIndex);
			}
		}
		
		// connect file and task
		for(var group: taskMap.keySet()) {
			for(var task: taskMap.get(group)) {
				for(var e: task.input) {
					g.edge(e, task.target);
				}
				for(var e: task.output) {
					g.edge(task.target, e).attr("style", "bold");
				}
			}
		}
		
		var string = g.toString();
		logger.info("dot file   {}  {}", string.length(), file);
		FileUtil.write().file(file, string);
	}
	private static String getColorNumber(List<String>groupList, String name) {
		var nameString = name.replace("/", "-");
		
		int index = 0;
		for(var string: groupList) {
			index++;
			if (nameString.contains(string)) return String.valueOf(index);
		}
		logger.error("Unexpected name");
		logger.error("  groupList  {}", groupList);
		logger.error("  name       {}", name);
		throw new UnexpectedException("Unexpected name");
	}
	
}
