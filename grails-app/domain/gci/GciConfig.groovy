package gci

class GciConfig {
    String className
    Long instanceId
    String value
    GciConfigLevel level

    static belongsTo = [configDefinition:GciConfigDefinition]

    static constraints = {
        instanceId nullable: true
        value nullable: true
    }

    static mapping = {
        value type: 'text'
    }
}

enum GciConfigLevel {
    CLASS,
    INSTANCE
}