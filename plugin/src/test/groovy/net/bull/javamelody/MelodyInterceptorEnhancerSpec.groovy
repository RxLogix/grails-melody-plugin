package net.bull.javamelody

import grails.config.Config
import grails.core.GrailsApplication
import spock.lang.Specification

class MelodyInterceptorEnhancerSpec extends Specification {

    def "does not enhance service classes when JavaMelody is disabled via config"() {
        given:
        Config config = Mock()
        config.getProperty('javamelody.disabled') >> true
        GrailsApplication application = Mock()
        application.getConfig() >> config
        application.getClassLoader() >> this.class.classLoader

        when:
        new MelodyInterceptorEnhancer().enhance(application)

        then: "the disabled short-circuit returns before iterating service classes"
        0 * application.getServiceClasses()
    }
}