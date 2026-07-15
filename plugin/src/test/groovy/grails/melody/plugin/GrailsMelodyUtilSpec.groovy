package grails.melody.plugin

import grails.config.Config
import grails.core.GrailsApplication
import spock.lang.Specification

class GrailsMelodyUtilSpec extends Specification {

    def "returns the GrailsApplication config instance"() {
        given:
        Config config = Mock()
        GrailsApplication application = Mock()
        application.getConfig() >> config
        application.getClassLoader() >> this.class.classLoader

        when:
        Config result = GrailsMelodyUtil.getGrailsMelodyConfig(application)

        then:
        result.is(config)
    }

    def "swallows the failure and skips the merge when no GrailsMelodyConfig class is present"() {
        given: "the classpath has no GrailsMelodyConfig class, so loadClass will throw"
        Config config = Mock()
        GrailsApplication application = Mock()
        application.getConfig() >> config
        application.getClassLoader() >> this.class.classLoader

        when:
        Config result = GrailsMelodyUtil.getGrailsMelodyConfig(application)

        then: "the ClassNotFoundException is caught before merge() is reached"
        noExceptionThrown()
        0 * config.merge(_)
        result.is(config)
    }
}