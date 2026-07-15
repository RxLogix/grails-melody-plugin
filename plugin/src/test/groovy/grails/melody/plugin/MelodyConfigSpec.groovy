package grails.melody.plugin

import grails.config.Config
import grails.core.GrailsApplication
import net.bull.javamelody.MonitoringFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.boot.web.servlet.ServletContextInitializer
import spock.lang.Specification

class MelodyConfigSpec extends Specification {

    private MelodyConfig melodyConfigFor(Map javamelody) {
        Config config = Mock()
        config.getProperty('javamelody', Map) >> javamelody
        GrailsApplication application = Mock()
        application.getConfig() >> config
        application.getClassLoader() >> this.class.classLoader

        MelodyConfig melodyConfig = new MelodyConfig()
        melodyConfig.grailsApplication = application
        melodyConfig
    }

    def "melodyInitializer produces a ServletContextInitializer"() {
        expect:
        melodyConfigFor([:]).melodyInitializer() instanceof ServletContextInitializer
    }

    def "melodyFilter registers the JavaMelody MonitoringFilter for all URLs"() {
        when:
        FilterRegistrationBean bean = melodyConfigFor([:]).melodyFilter()

        then:
        bean != null
        bean.filter instanceof MonitoringFilter
        bean.urlPatterns.contains('/*')
        bean.asyncSupported
    }

    def "melodyFilter passes javamelody config entries through as filter init parameters"() {
        given:
        Map javamelody = ['disabled': 'true', 'storage-directory': '/tmp/melody']

        when:
        FilterRegistrationBean bean = melodyConfigFor(javamelody).melodyFilter()

        then:
        bean.initParameters['disabled'] == 'true'
        bean.initParameters['storage-directory'] == '/tmp/melody'
    }

    def "melodyFilter adds no init parameters when there is no javamelody config"() {
        when:
        FilterRegistrationBean bean = melodyConfigFor(null).melodyFilter()

        then:
        bean.initParameters.isEmpty()
    }
}