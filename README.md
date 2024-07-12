grails-melody-plugin
====================

[JavaMelody](https://github.com/javamelody/javamelody/wiki) monitoring [plugin](http://plugins.grails.org/plugin/sergiomichels/grails-melody-plugin) for Grails 3, to monitor application performance.

<a href='Screenshots#charts'><img src='https://github.com/javamelody/javamelody/wiki/resources/screenshots/graphs.png' alt='Screenshots' width='50%' title='Screenshots' /></a>

_The goal of JavaMelody is to monitor applications in QA and production environments. It is not a tool to simulate requests from users, it is a tool to measure and calculate statistics on real operation of an application depending on the usage of the application by users._

### Installation ###

For Grails < 4.x
To install the plugin, just add a dependency as given at the top of [this page](http://plugins.grails.org/plugin/sergiomichels/grails-melody-plugin) (but runtime is enough instead of compile). For example:
```groovy
dependencies {
    runtime 'org.grails.plugins:grails-melody-plugin:1.xx.0'
}
```

For Grails 6.x
```groovy
repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/RxLogix/grails-melody-plugin")
            credentials {
                username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    

dependencies {
    runtime 'org.grails.plugins:grails-melody-plugin:6.0-JDK11-M3'
}
```

Then you will be able to monitor the application at ```http://localhost:8080/<YourContext>/monitoring```.

[![Download](https://api.bintray.com/packages/sergiomichels/plugins/grails-melody-plugin/images/download.svg) ](https://bintray.com/sergiomichels/plugins/grails-melody-plugin/_latestVersion)

[Release Notes](https://github.com/javamelody/javamelody/wiki/ReleaseNotes) 

### More configuration ###

A few things you might want to know:
* the plugin overwrites original grails 'dataSource' bean in spring context with a JavaMelody datasource proxy.
* the plugin uses groovy meta programming to intercept grails services method calls.
  
All parameters described in the [JavaMelody User's guide](https://github.com/javamelody/javamelody/wiki/UserGuide#6-optional-parameters)
can be configured in your grails-app/conf/application.yml file. For example, add the following to disable the monitoring:
```yaml
javamelody:
    disabled: true
```

JavaMelody uses URIs to resolve HTTP requests. This means that
```
/book/show/1 and 
/book/show/23 
```

will resolve as different requests.  While that's desirable in some cases, often you want the statistics to be gathered for the show action, irrespective of parameters. In that case, add the following configuration in your grails-app/conf/application.yml file and the above URIs will show up as /book/show/$. 
```yaml
javamelody:
    # filter out numbers from URI
    http-transform-pattern: \d+
```

Similar issue may come for SQL monitoring - you can use a similar Regex to filter it.
```yaml
javamelody:
    sql-transform-pattern: \d+
```

Other parameters such as storage-directory, url-exclude-pattern, log, monitoring-path, authorized-users or allowed-addr-pattern can also be configured.

You can also add [rules for the spring security plugin](https://grails-plugins.github.io/grails-spring-security-core/v3/index.html#requestMappings), if installed:
```yaml
grails.plugin.springsecurity.controllerAnnotations.staticRules = [ [pattern: '/monitoring', access: ['ROLE_ADMIN']] ]
```
or add an authorized-users parameter:
```yaml
javamelody:
    authorized-users: user1:pwd1, user2:pwd2
```

_Please submit github pull requests and github issues._
