package com.ecocity.esg.application.port.out;

public interface LicenseScanCoordinationPort {
    boolean runIfLeader(Runnable task);
}
