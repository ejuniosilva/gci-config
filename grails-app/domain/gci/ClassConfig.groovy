package gci

class ClassConfig extends GlobalConfig {
    String className

    static constraints = {

    }

    static mapping = {
        value type: 'text'
        discriminator value: 'CLASS'
    }
}

