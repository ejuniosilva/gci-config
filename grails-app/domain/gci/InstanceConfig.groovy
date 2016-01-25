package gci

class InstanceConfig extends ClassConfig {
    Long instanceId

    static constraints = {
        instanceId nullable: true
    }

    static mapping = {
        discriminator value: "INSTANCE"
    }
}
