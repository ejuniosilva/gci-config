import gci.*
import grails.util.GrailsNameUtils


class GciConfigGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.5 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "GCI Config Plugin" // Headline display name of the plugin
    def author = "Luiz Cantoni"
    def authorEmail = "luiz.cantoni@gmail.com"
    def description = '''\
Brief summary/description of the plugin.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/gci-config"

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
    def scm = [ url: "https://github.com/luizcantoni/gci-config" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        for(domainClass in application.domainClasses) {
            if (Configurable.class.isAssignableFrom(domainClass.clazz)) {
                domainClass.clazz.metaClass {
                    'static' {
                        //### Class (static) methdos ###

                        addConfig { name ->
                            addConfig(name,null)
                        }

                        addConfig { name, value ->
                            if (!name.contains("."))
                                throw new RuntimeException("The name needs to contain the namespace. Format: namespace.name, example: quote.maxDays")

                            def aux = name.split("\\.")

                            addConfig (aux[0],aux[1],value)
                        }

                        addConfig { namespace, name, value ->
                            def isInstance, className, config, configDefinition

                            configDefinition = GciConfigDefinition.findByNamespaceAndName(namespace,name)

                            if (!configDefinition)
                                throw new RuntimeException("ConfigDefinition ${namespace}.${name} not found!")

                            if (value != null && GciConfigDefinition.inferDataType(value) != configDefinition.dataType)
                                throw new RuntimeException("Data type of parameter value is different of configDefintion's data type!")

                            isInstance = delegate.hasProperty("id") && delegate.id != null

                            if (isInstance) {
                                className = GrailsNameUtils.getPropertyName(delegate.class)
                                config = GciConfig.findByConfigDefinitionAndClassNameAndInstanceIdAndLevel(configDefinition, className, delegate.id, GciConfigLevel.INSTANCE)
                            }
                            else {
                                className = GrailsNameUtils.getPropertyName(delegate)
                                config = GciConfig.findByConfigDefinitionAndClassNameAndLevel(configDefinition, className, GciConfigLevel.CLASS)
                            }

                            if (config)
                                throw new RuntimeException("Config ${namespace}.${name} already exists for class ${className}!")

                            if (isInstance) {
                                config = new GciConfig()
                                config.className = className
                                config.instanceId = delegate.id
                                config.level = GciConfigLevel.INSTANCE
                            }
                            else {
                                config = new GciConfig()
                                config.className = className
                                config.level = GciConfigLevel.CLASS
                            }

                            config.value = value.toString()
                            config.configDefinition = configDefinition
                            config.save(flush: true)
                        }

                        getConfig { name ->
                            if (!name.contains("."))
                                throw new RuntimeException("The parameter name needs to contain the namespace. Format: namespace.name, example: quote.maxDays")

                            def aux = name.split("\\.")

                            return delegate.getConfig (aux[0],aux[1])
                        }

                        getConfig { namespace, name ->
                            def className, config, configDefinition, value

                            className = GrailsNameUtils.getPropertyName(delegate)

                            configDefinition = GciConfigDefinition.findByNamespaceAndName(namespace,name)

                            if (!configDefinition)
                                throw new RuntimeException("ConfigDefinition ${namespace}.${name} not found!")

                            config = GciConfig.findByConfigDefinitionAndClassNameAndLevel(configDefinition,className, GciConfigLevel.CLASS)

                            if (!config)
                                value = configDefinition.defaultValue
                            else
                                value = config.value

                            return GciConfigDefinition.castConfigValue(value,configDefinition.dataType)
                        }

                        setConfig { name, value ->
                            if (!name.contains("."))
                                throw new RuntimeException("The parameter name needs to contain the namespace. Format: namespace.name, example: quote.maxDays")

                            def aux = name.split("\\.")

                            delegate.setConfig (aux[0],aux[1],value)
                        }

                        setConfig { namespace, name, value ->
                            def className, configDefinition, config

                            className = GrailsNameUtils.getPropertyName(delegate)

                            configDefinition = GciConfigDefinition.findByNamespaceAndName(namespace,name)

                            if (!configDefinition)
                                throw new RuntimeException("ConfigDefinition ${namespace}.${name} not found!")

                            if (value != null && GciConfigDefinition.inferDataType(value) != configDefinition.dataType)
                                throw new RuntimeException("Data type of parameter value is different of configDefintion's data type!")

                            config = GciConfig.findByConfigDefinitionAndClassNameAndLevel(configDefinition,className, GciConfigLevel.CLASS)

                            if (!config)
                                throw new RuntimeException("Config ${namespace}.${name} not found for class ${className}!")

                            config.value = value.toString()
                            config.save(flush: true)
                        }

                        delConfig { name ->
                            if (!name.contains("."))
                                throw new RuntimeException("The parameter name needs to contain the namespace. Format: namespace.name, example: quote.maxDays")

                            def aux = name.split("\\.")

                            delegate.delConfig (aux[0],aux[1])
                        }

                        delConfig { namespace, name ->
                            def isInstance, className, configDefinition, config

                            configDefinition = GciConfigDefinition.findByNamespaceAndName(namespace,name)

                            if (!configDefinition)
                                throw new RuntimeException("ConfigDefinition ${namespace}.${name} not found!")

                            isInstance = delegate.hasProperty("id") && delegate.id != null

                            if (isInstance) {
                                className = GrailsNameUtils.getPropertyName(delegate.class)
                                config = GciConfig.findByConfigDefinitionAndClassNameAndInstanceIdAndLevel(configDefinition, className, delegate.id, GciConfigLevel.INSTANCE)
                            }
                            else {
                                className = GrailsNameUtils.getPropertyName(delegate)
                                config = GciConfig.findByConfigDefinitionAndClassNameAndLevel(configDefinition, className, GciConfigLevel.CLASS)
                            }

                            if (!config)
                                throw new RuntimeException("Config ${namespace}.${name} not found for class ${className}!")

                            config.delete(flush: true)
                        }
                    }

                    //### Instance methdos ###

                    getConfig { name ->
                        if (!name.contains("."))
                            throw new RuntimeException("The parameter name needs to contain the namespace. Format: namespace.name, example: quote.maxDays")

                        def aux = name.split("\\.")

                        return getConfig (aux[0],aux[1])
                    }

                    getConfig { namespace, name ->
                        def className, config, configDefinition, value

                        className = GrailsNameUtils.getPropertyName(delegate.class)
                        configDefinition = GciConfigDefinition.findByNamespaceAndName(namespace,name)

                        if (!configDefinition)
                            throw new RuntimeException("ConfigDefinition ${namespace}.${name} not found!")

                        //Try on the instance first
                        config = GciConfig.findByConfigDefinitionAndClassNameAndInstanceIdAndLevel(configDefinition, className, delegate.id, GciConfigLevel.INSTANCE)

                        if (!config)
                            config = GciConfig.findByConfigDefinitionAndClassNameAndLevel(configDefinition, className, GciConfigLevel.CLASS)

                        if (!config)
                            value = configDefinition.defaultValue
                        else
                            value = config.value

                        return GciConfigDefinition.castConfigValue(value,configDefinition.dataType)
                    }

                    setConfig { name, value ->
                        if (!name.contains("."))
                            throw new RuntimeException("The parameter name needs to contain the namespace. Format: namespace.name, example: quote.maxDays")

                        def aux = name.split("\\.")

                        setConfig (aux[0],aux[1],value)
                    }

                    setConfig { namespace, name, value ->
                        def className, config, configDefinition

                        className = GrailsNameUtils.getPropertyName(delegate.class)
                        configDefinition = GciConfigDefinition.findByNamespaceAndName(namespace,name)

                        if (value != null && GciConfigDefinition.inferDataType(value) != configDefinition.dataType)
                            throw new RuntimeException("Data type of parameter value is different of configDefintion's data type!")

                        if (!configDefinition)
                            throw new RuntimeException("ConfigDefinition ${namespace}.${name} not found!")

                        config = GciConfig.findByConfigDefinitionAndClassNameAndInstanceIdAndLevel(configDefinition, className, delegate.id, GciConfigLevel.INSTANCE)

                        if (!config)
                            throw new RuntimeException("Config ${namespace}.${name} not found for class ${className} and instanceId ${delegate.id}!")

                        config.value = value.toString()
                        config.save(flush:true)
                    }
                }
            }
        }
    }

    def doWithApplicationContext = { ctx ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
