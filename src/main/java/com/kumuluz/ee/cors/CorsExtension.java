package com.kumuluz.ee.cors;


import com.kumuluz.ee.common.Extension;
import com.kumuluz.ee.common.ServletServer;
import com.kumuluz.ee.common.config.EeConfig;
import com.kumuluz.ee.common.dependencies.EeComponentDependency;
import com.kumuluz.ee.common.dependencies.EeComponentType;
import com.kumuluz.ee.common.dependencies.EeExtensionDef;
import com.kumuluz.ee.common.dependencies.EeExtensionType;
import com.kumuluz.ee.common.wrapper.KumuluzServerWrapper;
import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import com.kumuluz.ee.cors.common.config.CorsConfig;
import com.thetransactioncompany.cors.CORSFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author Zvone Gazvoda
 */
@EeExtensionDef(name = "Cors", type = EeExtensionType.CORS)
@EeComponentDependency(EeComponentType.SERVLET)
public class CorsExtension implements Extension {

    private static final Logger log = Logger.getLogger(CorsExtension.class.getName());

    public void init(KumuluzServerWrapper kumuluzServerWrapper, EeConfig eeConfig) {

        if (kumuluzServerWrapper.getServer() instanceof ServletServer) {

            log.info("Initializing CORS filter.");

            ServletServer servletServer = (ServletServer) kumuluzServerWrapper.getServer();

            ConfigurationUtil cfg = ConfigurationUtil.getInstance();

            Optional<String> corsFilterOpt = cfg.get("kumuluzee.servlet.cors-filter");

            CorsConfig corsConfig = null;

            if (corsFilterOpt.isPresent()) {

                log.info("CORS filter configuration detected.");

                corsConfig = new CorsConfig();

                Optional<String> allowGenericHttpRequests = cfg.get("kumuluzee.servlet.cors-filter.allow-generic-http-requests");
                allowGenericHttpRequests.ifPresent(corsConfig::setAllowGenericHttpRequests);

                Optional<String> allowOrigin = cfg.get("kumuluzee.servlet.cors-filter.allow-origin");
                allowOrigin.ifPresent(corsConfig::setAllowOrigin);

                Optional<String> allowSubdomains = cfg.get("kumuluzee.servlet.cors-filter.allow-subdomains");
                allowSubdomains.ifPresent(corsConfig::setAllowSubdomains);

                Optional<String> supportedMethods = cfg.get("kumuluzee.servlet.cors-filter.supported-methods");
                supportedMethods.ifPresent(corsConfig::setSupportedMethods);

                Optional<String> supportedHeaders = cfg.get("kumuluzee.servlet.cors-filter.supported-headers");
                supportedHeaders.ifPresent(corsConfig::setSupportedHeaders);

                Optional<String> exposedHeaders = cfg.get("kumuluzee.servlet.cors-filter.exposed-headers");
                exposedHeaders.ifPresent(corsConfig::setExposedHeaders);

                Optional<String> supportsCredentials = cfg.get("kumuluzee.servlet.cors-filter.supports-credentials");
                supportsCredentials.ifPresent(corsConfig::setSupportsCredentials);

                Optional<String> maxAge = cfg.get("kumuluzee.servlet.cors-filter.max-age");
                maxAge.ifPresent(corsConfig::setMaxAge);

                Optional<String> tagRequest = cfg.get("kumuluzee.servlet.cors-filter.tag-requests");
                tagRequest.ifPresent(corsConfig::setTagRequests);

                Optional<String> urlPattern = cfg.get("kumuluzee.servlet.cors-filter.url-pattern");
                urlPattern.ifPresent(corsConfig::setPathSpec);
            }

            Map<String, String> corsFilterParams = new HashMap<>();

            String pathSpec = "/*";

            if (corsConfig != null) {

                if (corsConfig.getAllowGenericHttpRequests() != null) {
                    corsFilterParams.put("cors.allowGenericHttpRequests", corsConfig.getAllowGenericHttpRequests().toString
                            ());
                }

                if (corsConfig.getAllowOrigin() != null) {
                    corsFilterParams.put("cors.allowOrigin", corsConfig.getAllowOrigin());
                }

                if (corsConfig.getAllowSubdomains() != null) {
                    corsFilterParams.put("cors.allowSubdomains", corsConfig.getAllowSubdomains().toString());
                }

                if (corsConfig.getSupportedMethods() != null) {
                    corsFilterParams.put("cors.supportedMethods", corsConfig.getSupportedMethods());
                }

                if (corsConfig.getSupportedHeaders() != null) {
                    corsFilterParams.put("cors.supportedHeaders", corsConfig.getSupportedHeaders());
                }

                if (corsConfig.getExposedHeaders() != null) {
                    corsFilterParams.put("cors.exposedHeaders", corsConfig.getExposedHeaders());
                }

                if (corsConfig.getSupportsCredentials() != null) {
                    corsFilterParams.put("cors.supportsCredentials", corsConfig.getSupportsCredentials().toString());
                }

                if (corsConfig.getMaxAge() != null) {
                    corsFilterParams.put("cors.maxAge", corsConfig.getMaxAge().toString());
                }

                if (corsConfig.getTagRequests() != null) {
                    corsFilterParams.put("cors.tagRequests", corsConfig.getTagRequests().toString());
                }

                if (corsConfig.getPathSpec() != null) {
                    pathSpec = corsConfig.getPathSpec();
                }
            }

            servletServer.registerFilter(CORSFilter.class, pathSpec, corsFilterParams);

            log.info("Initialized CORS filter.");
        }
    }

    public void load() {
    }

}
