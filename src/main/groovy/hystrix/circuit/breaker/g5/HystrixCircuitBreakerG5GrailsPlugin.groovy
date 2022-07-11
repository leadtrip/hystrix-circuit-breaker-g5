package hystrix.circuit.breaker.g5

import com.neidetcher.hcbp.HystrixService
import com.neidetcher.hcbp.util.HystrixConfigurationUtility
import com.netflix.config.ConfigurationManager
import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet
import com.netflix.turbine.streaming.servlet.TurbineStreamServlet
import grails.plugins.*
import hystrix.circuit.breaker.TurbineContextListener
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean
import org.springframework.boot.web.servlet.ServletRegistrationBean

class HystrixCircuitBreakerG5GrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "5.2.0 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/controllers/hystrix/circuit/breaker/TestController.groovy"
    ]

    // TODO Fill in these fields
    def title = "Hystrix Circuit Breaker G5" // Headline display name of the plugin
    def author = "MW"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/hystrix-circuit-breaker-g5"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    Closure doWithSpring() { {->
        hystrixMetricsStreamServlet(ServletRegistrationBean, new HystrixMetricsStreamServlet(), '/hystrix.stream')

        if (config.getProperty('turbine')) {
                turbineStreamServlet(ServletRegistrationBean, new TurbineStreamServlet(), '/turbine.stream')

                turbineContextListener(ServletListenerRegistrationBean, TurbineContextListener)
            }
        }
    }

    void doWithDynamicMethods() {
        addHystrixMethods(application, log)
    }

    void doWithApplicationContext() {
        HystrixConfigurationUtility.configureHystrix(application, ConfigurationManager.getConfigInstance())
    }


    void onChange(Map<String, Object> event) {
        addHystrixMethods(event.application, log)
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }

    private void addHystrixMethods(application, log) {
        HystrixService svc = application.mainContext.hystrixService

        for(artefactClass in application.controllerClasses + application.serviceClasses) {
            if (artefactClass.clazz == HystrixService.class) {
                continue
            }

            log.debug "Adding hystrix methods to ${artefactClass}"
            def mc = artefactClass.metaClass
            mc.hystrix =  { HystrixCommand command -> svc.hystrix(command) }
            mc.hystrix << { Closure c -> svc.hystrix(c) }
            mc.hystrix << { Map map, Closure c -> svc.hystrix(map, c) }
        }
    }
}
