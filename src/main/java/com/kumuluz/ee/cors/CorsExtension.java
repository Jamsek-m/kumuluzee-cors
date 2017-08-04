package com.kumuluz.ee.cors;

import com.kumuluz.ee.common.Extension;
import com.kumuluz.ee.common.ServletServer;
import com.kumuluz.ee.common.config.EeConfig;
import com.kumuluz.ee.common.dependencies.*;
import com.kumuluz.ee.common.runtime.EeRuntime;
import com.kumuluz.ee.common.wrapper.KumuluzServerWrapper;
import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import com.kumuluz.ee.cors.config.CorsConfig;
import com.kumuluz.ee.cors.filters.DynamicCorsFilter;
import com.thetransactioncompany.cors.CORSFilter;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author Zvone Gazvoda
 */
@EeExtensionDef(name = "Cors", type = EeExtensionType.CORS)
@EeComponentDependencies({@EeComponentDependency(EeComponentType.SERVLET)})
public class CorsExtension implements Extension {

    private static final Logger log = Logger.getLogger(CorsExtension.class.getName());

    public void init(KumuluzServerWrapper kumuluzServerWrapper, EeConfig eeConfig) {

        if (kumuluzServerWrapper.getServer() instanceof ServletServer) {

            log.info("Initializing CORS filter.");

            ServletServer servletServer = (ServletServer) kumuluzServerWrapper.getServer();

            ConfigurationUtil cfg = ConfigurationUtil.getInstance();

            Optional<String> corsFilterOpt = cfg.get("kumuluzee.cors-filter.servlet");

            CorsConfig corsConfig = null;

            boolean isCrossOriginAnnotationsPresent = isCrossOriginAnnotationUsed();

            if (corsFilterOpt.isPresent() && !isCrossOriginAnnotationsPresent) {

                log.info("CORS filter configuration detected.");

                corsConfig = new CorsConfig();

                Optional<String> allowGenericHttpRequests = cfg.get("kumuluzee.cors-filter.servlet.allow-generic-http-requests");
                allowGenericHttpRequests.ifPresent(corsConfig::setAllowGenericHttpRequests);

                Optional<String> allowOrigin = cfg.get("kumuluzee.cors-filter.servlet.allow-origin");
                allowOrigin.ifPresent(corsConfig::setAllowOrigin);

                Optional<String> allowSubdomains = cfg.get("kumuluzee.cors-filter.servlet.allow-subdomains");
                allowSubdomains.ifPresent(corsConfig::setAllowSubdomains);

                Optional<String> supportedMethods = cfg.get("kumuluzee.cors-filter.servlet.supported-methods");
                supportedMethods.ifPresent(corsConfig::setSupportedMethods);

                Optional<String> supportedHeaders = cfg.get("kumuluzee.cors-filter.servlet.supported-headers");
                supportedHeaders.ifPresent(corsConfig::setSupportedHeaders);

                Optional<String> exposedHeaders = cfg.get("kumuluzee.cors-filter.servlet.exposed-headers");
                exposedHeaders.ifPresent(corsConfig::setExposedHeaders);

                Optional<String> supportsCredentials = cfg.get("kumuluzee.cors-filter.servlet.supports-credentials");
                supportsCredentials.ifPresent(corsConfig::setSupportsCredentials);

                Optional<String> maxAge = cfg.get("kumuluzee.cors-filter.servlet.max-age");
                maxAge.ifPresent(corsConfig::setMaxAge);

                Optional<String> tagRequest = cfg.get("kumuluzee.cors-filter.servlet.tag-requests");
                tagRequest.ifPresent(corsConfig::setTagRequests);

                Optional<String> urlPattern = cfg.get("kumuluzee.cors-filter.servlet.url-pattern");
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

            boolean isJaxRS = EeRuntime.getInstance().getEeComponents().stream().anyMatch(c -> c.getType().equals(EeComponentType.JAX_RS));

            if (!isCrossOriginAnnotationsPresent && corsConfig != null) {
                servletServer.registerFilter(CORSFilter.class, pathSpec, corsFilterParams);

            } else {
                Map<String, String> dynamicCorsFilterParams = new HashMap<>();
                dynamicCorsFilterParams.put("isJaxRS", Boolean.toString(isJaxRS));

                servletServer.registerFilter(DynamicCorsFilter.class, "/*", dynamicCorsFilterParams);
            }

            log.info("Initialized CORS filter.");
        }
    }

    private Boolean isCrossOriginAnnotationUsed() {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceFileUrl = classLoader.getResource("META-INF/resources/java.lang.Object");
        URL servletFileUrl = classLoader.getResource("META-INF/servlets/java.lang.Object");

        if (resourceFileUrl != null) {
            File file = new File(resourceFileUrl.getFile());

            if (file.length() != 0) {
                return true;
            }
        }

        if (servletFileUrl != null) {
            File file = new File(servletFileUrl.getFile());

            if (file.length() != 0) {
                return true;
            }
        }

        return false;
    }

    public void load() {
    }

}