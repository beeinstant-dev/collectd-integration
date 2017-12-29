package com.beeinstant;

import java.io.BufferedReader;
import java.io.FileReader;

public class CpuState {
    private int user = 0;
    private int nice = 0;
    private int system = 0;
    private int idle = 0;
    private int iowait = 0;
    private int irq = 0;
    private int softirq = 0;
    private int steal = 0;

    CpuState() {
        try {
            BufferedReader stat = new BufferedReader(new FileReader("/proc/stat"));
            String cpuStat = stat.readLine();
            String[] fields = cpuStat.split("\\s+");

            if (fields.length >= 9) {
                this.user = Integer.parseInt(fields[1]);
                this.nice = Integer.parseInt(fields[2]);
                this.system = Integer.parseInt(fields[3]);
                this.idle = Integer.parseInt(fields[4]);
                this.iowait = Integer.parseInt(fields[5]);
                this.irq = Integer.parseInt(fields[6]);
                this.softirq = Integer.parseInt(fields[7]);
                this.steal = Integer.parseInt(fields[8]);
            }
        } catch (NumberFormatException | java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static double calculateCpuUtilization(CpuState now, CpuState prv) {
        return 100 * Math.max(0.0d, (double)(now.getActive() - prv.getActive()) / (double)(now.getTotal() - prv.getTotal()));
    }

    private long getActive() {
        return this.user + this.nice + this.system + this.irq + this.softirq + this.steal;
    }

    private long getTotal() {
        return getActive() + this.idle + this.iowait;
    }
}
