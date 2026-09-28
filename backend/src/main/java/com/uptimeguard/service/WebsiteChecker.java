package com.uptimeguard.service;

import com.uptimeguard.model.Monitor;

public interface WebsiteChecker {
    ProbeResult check(Monitor monitor);
}
