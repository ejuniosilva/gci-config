package gci

class GciConfigDefinition {
    String name
    String namespace
    GciConfigDataType dataType
    String description
    String defaultValue

    static constraints = {
        description nullable:  true
        defaultValue nullable: true
        name unique: 'namespace'
    }

    static mapping = {
        defaultValue type: 'text'
    }

    static create(namespace, name, description, defaultValue, dataType) {
        def configDefinition = GciConfigDefinition.findByNamespaceAndName(namespace,name)

        if (configDefinition)
            throw new RuntimeException("ConfigDefinition ${namespace}.${name} already exists!")

        if (!configDefinition) {
            configDefinition = new GciConfigDefinition()
            configDefinition.namespace = namespace
            configDefinition.name = name
            configDefinition.dataType = dataType
            configDefinition.description = description
            configDefinition.defaultValue = defaultValue

            if (defaultValue != null && dataType != inferDataType(defaultValue))
                throw new RuntimeException("Datatype of default value is different!")

            configDefinition.save(flush: true)
        }
    }

    static inferDataType(value) {
        if (!value)
            return GciConfigDataType.String

        value = value.toString()

        if (value.toLowerCase() == "false" || value.toLowerCase() == "true")
            return GciConfigDataType.Boolean

        try {
            Integer.parseInt(value)
            return GciConfigDataType.Integer
        }
        catch(e) {}

        try {
            Float.parseFloat(value)
            return GciConfigDataType.Float
        }
        catch(e) {}

        try {
            Double.parseDouble(value)
            return GciConfigDataType.Double
        }
        catch(e) {}

        try {
            def evalResult

            value = value.trim()

            if (value.startsWith("[") && value.endsWith("]")) {
                evalResult = Eval.me(value)

                if (evalResult instanceof List)
                    return GciConfigDataType.List
                else if (evalResult instanceof Map)
                    return GciConfigDataType.Map
            }
        }
        catch(e) {}

        return GciConfigDataType.String
    }

    static castConfigValue (value, GciConfigDataType type) {
        def result = null


        switch (type) {
            case GciConfigDataType.Boolean:
                result = value ? value.toBoolean() : false
                break

            case GciConfigDataType.Integer:
                result = value ? value.toInteger() : null
                break

            case GciConfigDataType.Double:
                result = value ? value.toDouble() : null
                break

            case GciConfigDataType.Float:
                result = value ? value.toFloat() : null
                break

            case GciConfigDataType.Character:
                result = value ? value.toCharacter() : null
                break

            case (GciConfigDataType.List || GciConfigDataType.Map):
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

enum GciConfigDataType {
    Boolean,
    Integer,
    Float,
    Double,
    Character,
    List,
    Map,
    String
}