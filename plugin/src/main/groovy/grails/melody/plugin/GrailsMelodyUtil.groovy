package grails.melody.plugin

import grails.config.Config
import grails.core.GrailsApplication
import grails.util.Environment
import groovy.transform.CompileStatic

@CompileStatic
class GrailsMelodyUtil {

	static Config getGrailsMelodyConfig(GrailsApplication application) {
		Config config = application.config
		GroovyClassLoader classLoader = new GroovyClassLoader(application.getClassLoader())
		try {
			config.merge(new ConfigSlurper(Environment.current.name().toLowerCase()).parse(classLoader.loadClass('GrailsMelodyConfig')) as Map)
		} catch (Exception e) {
			// ignored, use defaults
		}
		return config
	}
}
