package com.beeinstant;

import com.beeinstant.metrics.MetricsLogger;
import com.beeinstant.metrics.MetricsManager;
import com.beeinstant.metrics.Unit;
import org.collectd.api.*;

public class CollectdIntegration implements CollectdWriteInterface, CollectdConfigInterface, CollectdShutdownInterface {

    private CpuState cpuState = new CpuState();

    public CollectdIntegration() {
        Collectd.registerWrite("BeeInstant", this);
        Collectd.registerConfig("CollectdIntegration", this);
        Collectd.registerConfig("BeeInstantShutdown", this);
    }

    @Override
    public int write(ValueList valueList) {
        final StringBuilder sb = new StringBuilder("host=");
        sb.append(valueList.getHost());
        sb.append(",plugin=");
        sb.append(valueList.getPlugin());
        sb.append(",instance=");
        sb.append(valueList.getPluginInstance().isEmpty() ? "None" : valueList.getPluginInstance());

        final MetricsLogger metricsLogger = MetricsManager.getMetricsLogger(sb.toString());
        final String metric = valueList.getType() + (valueList.getTypeInstance().isEmpty() ? "" : "." + valueList.getTypeInstance());

        for (final Number value: valueList.getValues()) {
            metricsLogger.record(metric, Math.max(0, value.doubleValue()), Unit.NONE);
        }

        // calculate cpu utilization
        if (valueList.getPlugin().equalsIgnoreCase("cpu")) {
            CpuState curCpuState = new CpuState();
            double cpuUtilization = CpuState.calculateCpuUtilization(curCpuState, this.cpuState);
            this.cpuState = curCpuState;

            MetricsManager.getMetricsLogger("plugin=cpu,instance=All,host=" + valueList.getHost())
                    .record("Utilization", cpuUtilization, Unit.NONE);
        }

        return 0;
    }

    @Override
    public int config(OConfigItem oConfigItem) {
        String endpoint = null;
        String publicKey = null;
        String secretKey = null;

        for (final OConfigItem child: oConfigItem.getChildren()) {
            if (child.getKey().equals("BeeInstant")) {
                for (final OConfigItem entry: child.getChildren()) {
                    Collectd.logInfo("BeeInstant " + entry.getKey() + ":" + entry.getValues().get(0).getString());
                    if (entry.getKey().equals("Endpoint")) {
                        endpoint = entry.getValues().get(0).getString();
                    } else if (entry.getKey().equals("PublicKey")) {
                        publicKey = entry.getValues().get(0).getString();
                    } else if (entry.getKey().equals("SecretKey")) {
                        secretKey = entry.getValues().get(0).getString();
                    }
                }
                break;
            }
        }

        if (endpoint == null || publicKey == null || secretKey == null) {
            Collectd.logError("Missing BeeInstant configuration");
            return 1;
        }

        System.setProperty("beeinstant.endpoint", endpoint);
        System.setProperty("beeinstant.publicKey", publicKey);
        System.setProperty("beeinstant.secretKey", secretKey);
        MetricsManager.init("BeeInstant-Collectd");

        return 0;
    }

    @Override
    public int shutdown() {
        MetricsManager.shutdown();
        return 0;
    }
}
