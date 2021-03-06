# Collectd Integration
This is a Java Plugin for CollectD. It publishes CollectD metrics to BeeInstant by using [BeeInstant Java SDK](https://github.com/beeinstant-dev/beeinstant-java-sdk).

![Collectd metrics screenshot](collectd-metrics-screenshot.png)

## Setup
* Clone this repo `git@github.com:beeinstant-dev/collectd-integration.git`.
* Build `mvn package`
* Copy the built jar to Collectd java plugin folder. Example for Ubuntu `sudo cp target/collectd-integration.jar /usr/share/collectd/java/`.
* Configure the plugin in `/etc/collectd/collectd.conf`.
    ```
    LoadPlugin java
    <Plugin java>
        JVMArg "-verbose:jni"
        JVMArg "-Djava.class.path=/usr/share/collectd/java/collectd-api.jar:/usr/share/collectd/java/collectd-integration.jar"

        LoadPlugin "com.beeinstant.CollectdIntegration"
        <Plugin "CollectdIntegration">
            <BeeInstant>
                Endpoint "<beeinstant-endpoint>"
                PublicKey "<beeinstant-public-key>"
                SecretKey "<beeinstant-secret-key>"
                HostGroup "<group-name-of-the-host>"
            </BeeInstant>
        </Plugin>
    </Plugin>
    ```
* Restart collectd `sudo service collectd restart`.

### Troubleshooting
* Check collectd log in syslog `grep collectd /var/log/syslog` for any error.
* If `ldd /usr/lib/collectd/java.so` show `libjvm.so` not found, install jre `sudo apt install openjdk-8-jre`. Check output of `ldd /usr/lib/collectd/java.so` again to make sure `libjvm.so` is found.
* Restart collectd `sudo service collectd restart`.