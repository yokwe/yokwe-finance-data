package yokwe.finance.data.util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

import yokwe.finance.data.provider.Makefile;
import yokwe.util.ClassUtil;
import yokwe.util.UnexpectedException;

public class T001 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	

	public static void main(String[] args) {
		logger.info("START");
		var makefileList = new ArrayList<Makefile>();
		{
			logger.info("{}", T001.class.getClassLoader());
			logger.info("{}", ClassLoader.getPlatformClassLoader());
			logger.info("{}", ClassLoader.getSystemClassLoader());
			logger.info("{}", Thread.currentThread().getContextClassLoader());
			
			var classLoader = ClassLoader.getSystemClassLoader();
			
			var clazzList = ClassUtil.findClass(classLoader, "yokwe.finance.data");
			ClassUtil.sort(clazzList);
			logger.info("clazzList  {}", clazzList.size());
			for(var clazz: clazzList) {
				var fields = clazz.getDeclaredFields();
				for(var field: fields) {
					field.setAccessible(true);
					if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(Makefile.class)) {
//						logger.info("clazz  {}", clazz.getCanonicalName());
						
						try {
							var makefile = (Makefile)field.get(null);
							
//							logger.info("makefile  {}", makefile.toString());
							
							
							makefileList.add(makefile);
//							logger.info("clazz  {}", clazz.getCanonicalName());
//							logger.info("  {}", makefile.clazz.getCanonicalName());
//							logger.info("    {}", makefile.target);
						} catch (IllegalArgumentException | IllegalAccessException e) {
			    			String exceptionName = e.getClass().getSimpleName();
			    			logger.error("{} {}", exceptionName, e);
			    			throw new UnexpectedException(exceptionName, e);
						}
					}
				}
			}
		}
		logger.info("makefileList  {}", makefileList.size());
		for(var e: makefileList) {
			logger.info("{}", e.clazz.getTypeName());
			logger.info("  {}", e.target);
			logger.info("  {}", e.outputs[0]);
		}

		// consistency check of makefile
		//   any input file must appeared in output of makefile
		//   any output file must appeared in input of makefile
		//   only one makefile output the output file
		//   
		logger.info("STOP");
	}
	

}
