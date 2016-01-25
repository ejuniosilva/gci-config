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
    def title = "Gci Config Plugin" // Headline display name of the plugin
    def author = "Your name"
    def authorEmail = ""
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
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

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
                            def isInstance, isGlobal, className, config, configDefinition

                            configDefinition = ConfigDefinition.findByNamespaceAndName(namespace,name)

                            if (!configDefinition)
                                throw new RuntimeException("ConfigDefinition ${namespace}.${name} not found!")

                            if (value != null && ConfigDefinition.inferDataType(value) != configDefinition.dataType)
                                throw new RuntimeException("Data type of parameter value is different of configDefintion's data type!")

                            isInstance = delegate.hasProperty("id") && delegate.id != null

                            if (isInstance)
                                className = GrailsNameUtils.getPropertyName(delegate.class)
                            else
                                className = GrailsNameUtils.getPropertyName(delegate)

                            isGlobal = className.contains("globalConfig")

                            if (isGlobal)
                                config = GlobalConfig.findByConfigDefinition(configDefinition)
                            else if (isInstance)
                                config = InstanceConfig.findByConfigDefinitionAndClassNameAndInstanceId(configDefinition, className, delegate.id)
                            else
                                config = ClassConfig.findByConfigDefinitionAndClassName(configDefinition, className)

                            if (config)
                                throw new RuntimeException("Config ${namespace}.${name} already exists for class ${className}!")

                            if (isGlobal)
                                config = new GlobalConfig()
                            else if (isInstance) {
                                config = new InstanceConfig()
                                config.className = className
                                config.instanceId = delegate.id
                            }
                            else {
                                config = new ClassConfig()
                                config.className = className
                            }

                            config.value = value
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
                            def className, config, globalConfig, configDefinition

                            className = GrailsNameUtils.getPropertyName(delegate)

                            configDefinition = ConfigDefinition.findByNamespaceAndName(namespace,name)

                            if (!configDefinition)
                                throw new RuntimeException("ConfigDefinition ${namespace}.${name} not found!")

                            config = ClassConfig.findByConfigDefinitionAndClassName(configDefinition,className)
                            globalConfig = GlobalConfig.findByConfigDefinition(configDefinition)

                            if (!config)
                                config = globalConfig

                            if (!config)
                                throw new RuntimeException("Config ${namespace}.${name} not found for class ${className}!")

                            return ConfigDefinition.castConfigValue(config.value,config.configDefinition.dataType)
                        }

                        setConfig { name, value ->
                            if (!name.contains("."))
                                throw new RuntimeException("The parameter name needs to contain the namespace. Format: namespace.name, example: quote.maxDays")

                            def aux = name.split("\\.")

                            delegate.setConfig (aux[0],aux[1],value)
                        }

                        setConfig { namespace, name, value ->
                            def isGlobal, className, configDefinition, config

                            className = GrailsNameUtils.getPropertyName(delegate)

                            configDefinition = ConfigDefinition.findByNamespaceAndName(namespace,name)

                            if (!configDefinition)
                                throw new RuntimeException("ConfigDefinition ${namespace}.${name} not found!")

                            if (value != null && ConfigDefinition.inferDataType(value) != configDefinition.dataType)
                                throw new RuntimeException("Data type of parameter value is different of configDefintion's data type!")

                            isGlobal = className.contains("globalConfig")

                            if (isGlobal)
                                config = GlobalConfig.findByConfigDefinition(configDefinition)
                            else
                                config = ClassConfig.findByConfigDefinitionAndClassName(configDefinition, className)

                            if (!config)
                                throw new RuntimeException("Config ${namespace}.${name} not found for class ${className}!")

                            config.value = value
                            config.save(flush: true)
                        }

                        delConfig { name ->
                            if (!name.contains("."))
                                throw new RuntimeException("The parameter name needs to contain the namespace. Format: namespace.name, example: quote.maxDays")

                            def aux = name.split("\\.")

                            delegate.delConfig (aux[0],aux[1])
                        }

                        delConfig { namespace, name ->
                            def isInstance, isGlobal, className, configDefinition, config

                            configDefinition = ConfigDefinition.findByNamespaceAndName(namespace,name)

                            if (!configDefinition)
                                throw new RuntimeException("ConfigDefinition ${namespace}.${name} not found!")

                            isInstance = delegate.hasProperty("id") && delegate.id != null

                            if (isInstance)
                                className = GrailsNameUtils.getPropertyName(delegate.class)
                            else
                                className = GrailsNameUtils.getPropertyName(delegate)

                            isGlobal = className.contains("globalConfig")

                            if (isGlobal)
                                config = GlobalConfig.findByConfigDefinition(configDefinition)
                            else if (isInstance)
                                config = InstanceConfig.findByConfigDefinitionAndClassNameAndInstanceId(configDefinition, className, delegate.id)
                            else
                                config = ClassConfig.findByConfigDefinitionAndClassName(configDefinition, className)

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
                        def className, config, configDefinition

                        className = GrailsNameUtils.getPropertyName(delegate.class)
                        configDefinition = ConfigDefinition.findByNamespaceAndName(namespace,name)

                        if (!configDefinition)
                            throw new RuntimeException("ConfigDefinition ${namespace}.${name} not found!")

                        //Try on the instance first
                        config = InstanceConfig.findByConfigDefinitionAndClassNameAndInstanceId(configDefinition, className, delegate.id)

                        if (!config)
                            config = ClassConfig.findByConfigDefinitionAndClassName(configDefinition, className)

                        if (!config)
                            config = GlobalConfig.findByConfigDefinition(configDefinition)

                        if (!config)
                            throw new RuntimeException("Config ${namespace}.${name} not found for class ${className}!")

                        return ConfigDefinition.castConfigValue(config.value,configDefinition.dataType)
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
                        configDefinition = ConfigDefinition.findByNamespaceAndName(namespace,name)

                        if (value != null && ConfigDefinition.inferDataType(value) != configDefinition.dataType)
                            throw new RuntimeException("Data type of parameter value is different of configDefintion's data type!")

                        if (!configDefinition)
                            throw new RuntimeException("ConfigDefinition ${namespace}.${name} not found!")

                        config = InstanceConfig.findByConfigDefinitionAndClassNameAndInstanceId(configDefinition, className, delegate.id)

                        if (!config)
                            throw new RuntimeException("Config ${namespace}.${name} not found for class ${className}!")

                        config.value = value
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
