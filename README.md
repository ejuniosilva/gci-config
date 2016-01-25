GCI-Config is a Grails Plugin that allows the developer to specify Configurations in a Global (default), Class and Instance levels.
The plugin uses two tables in the database to store the configurations, theses tables are called Gci_Config and Gci_Config_Definition.

GCI-Config was inspired in the same concepts of Commentable Plugin. Some methods are injected at the Class (static) and Instance levels.

These methods are:

* addConfig
* getConfig
* setConfig
* delConfig

Besides the methods described above there is other called `GciConfigDefinition.create`. This method is used to create a Config Definition.

The most important thing about this plugin is the possibility to easily specify configurations at instance level.

#### A concrete example:

##### Creating a ConfigDefinition

First of all it's necessary to create a Config Definition

```groovy
GciConfigDefinition.create("client","maxDaysToApprove","Specify the quantity of days that the client has to approve the Quote.",30,ConfigDataType.Integer)
```
* **namespace**: client
* **name**: maxDatsToApprove
* **description**: Specify the quantity...
* **defaultValue**: 30
* **datatype**: ConfigDataType.Integer

##### Adding a Config

Second, it's necessary to add the ConfigDefinition created in some level (or both):

```groovy

//Specifing (or not) the config value in a class level
Client.addConfig("client.maxDaysToApprove",50)

//Specifing (or not) the config value in a instance level
Client.get(200).addConfig("client.maxDaysToApprove",15)
```

##### Getting a Config

The `getConfig` method has different behaviours if called from Class or Instance level.

If called from instance, the the method first tries to get the config value at Instance level. If none config is found, then the method tries at Class level. Again, if none is found, then the method returns the defaultValue (stored in GciConfigDefinition). If the config passed as parameter isn't found then a RuntimeException is raised.

* Instance Level: Instance -> Class -> Global
* Class Level: Class -> Global

Following the example above, then the code below will return 50, since there is no `client.maxDaysToApprove` configuration at this instance level (300), but there is at class level:

```groovy
Client.get(300).getConfig("client.maxDaysToApprove")
```

The code below will also returns 50, since the call is performed at class level.

```groovy
Client.getConfig("client.maxDaysToApprove")
```

The code below will returns 15, since the call is performed at instance level and there is this configuration for the instance (200).

```groovy
Client.get(200).getConfig("client.maxDaysToApprove")
```

##### Setting a Config

The method `setConfig` is used to change the value of a specific config. As the `getConfig` this method works at the three levels (Global, Class and Instance). However, the change of value doesn't follow the same hirerachical logic used in `getConfig`. This means if `setConfig` is called by a instance and the respective config doesn't exists at this level, then an exception is raised. In other words, the change of value works only at the level that was called.

The code below will change the value of `client.maxDaysToApprove` from 15 to 60.

```groovy
Client.get(200).setConfig("client.maxDaysToApprove",60)
```

##### Deleting a Config

It's possible to remove a config of a one of the three levels using the method `delConfig`. As the `setConfig`, the remove of a config works only at the level that was called.

The code below will delete the config `client.maxDaysToApprove` of the instance 200.

```groovy
Client.get(200).delConfig("client.maxDaysToApprove")
```


