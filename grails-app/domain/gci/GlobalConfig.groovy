package gci

class GlobalConfig implements Configurable {
    String value

    static belongsTo = [configDefinition:ConfigDefinition]

    static constraints = {
        value nullable: true
    }

    static mapping = {
        discriminator column: "config_type", value: "GLOBAL"
    }
}

