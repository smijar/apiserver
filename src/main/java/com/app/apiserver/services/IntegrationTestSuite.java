package com.app.apiserver.services;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;

import java.io.IOException;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.secnod.shiro.test.integration.AnnotationAuthTest;
import org.secnod.shiro.test.integration.webapp.JettyServer;

import com.app.apiserver.core.AppConfiguration;

@RunWith(Suite.class)
@SuiteClasses({AnnotationAuthTest.class})
public class IntegrationTestSuite {

    private static int findPort() {
        try {
            int port = JettyServer.allocatePort();
            System.setProperty("org.secnod.shiro.test.port", Integer.toString(port));
            return port;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int findAdminPort() {
        try {
            return JettyServer.allocatePort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ClassRule
    public static DropwizardAppRule<AppConfiguration> app = new DropwizardAppRule<AppConfiguration>(
            AppMain.class,
            "src/main/resources/api.yml",
            ConfigOverride.config("server.applicationConnectors[0].port", Integer.toString(findPort())),
            ConfigOverride.config("server.adminConnectors[0].port", Integer.toString(findAdminPort())));

}
