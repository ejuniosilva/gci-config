package gci

class ConfigDefinition {
    String name
    String namespace
    ConfigDataType dataType
    String description

    static constraints = {
        description nullable:  true
        name unique: 'namespace'
    }

    static create(namespace, name, description, dataType) {
        def configDefinition = ConfigDefinition.findByNamespaceAndName(namespace,name)

        if (configDefinition)
            throw new RuntimeException("ConfigDefinition ${namespace}.${name} already exists!")

        if (!configDefinition) {
            configDefinition = new ConfigDefinition()
            configDefinition.namespace = namespace
            configDefinition.name = name
            configDefinition.dataType = dataType
            configDefinition.description = description
            configDefinition.save(flush: true)
        }
    }

    static inferDataType(value) {
        if (!value)
            return ConfigDataType.String

        value = value.toString()

        if (value.toLowerCase() == "false" || value.toLowerCase() == "true")
            return ConfigDataType.Boolean

        try {
            Integer.parseInt(value)
            return ConfigDataType.Integer
        }
        catch(e) {}

        try {
            Float.parseFloat(value)
            return ConfigDataType.Float
        }
        catch(e) {}

        try {
            Double.parseDouble(value)
            return ConfigDataType.Double
        }
        catch(e) {}

        try {
            def evalResult

            value = value.trim()

            if (value.startsWith("[") && value.endsWith("]")) {
                evalResult = Eval.me(value)

                if (evalResult instanceof List)
                    return ConfigDataType.List
                else if (evalResult instanceof Map)
                    return ConfigDataType.Map
            }
        }
        catch(e) {}

        return ConfigDataType.String
    }

    static castConfigValue (value, ConfigDataType type) {
        def result = null


        switch (type) {
            case ConfigDataType.Boolean:
                result = value ? value.toBoolean() : false
                break

            case ConfigDataType.Integer:
                result = value ? value.toInteger() : null
                break

            case ConfigDataType.Double:
                result = value ? value.toDouble() : null
                break

            case ConfigDataType.Float:
                result = value ? value.toFloat() : null
                break

            case ConfigDataType.Character:
                result = value ? value.toCharacter() : null
                break

            case (ConfigDataType.List || ConfigDataType.Map):
                if (value) {
                    value = value.trim()

                    if (value.startsWith("[") && value.endsWith("]"))
                        result = Eval.me(value)
                    else
                        throw new RuntimeException("Cast of ${value} to (List or Map) isn't possible!")
                }

                break
        }

        return result
    }
}

enum ConfigDataType {
    Boolean,
    Integer,
    Float,
    Double,
    Character,
    List,
    Map,
    String
}