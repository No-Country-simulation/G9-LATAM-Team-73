package com.techmind.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "techmind.oci.language")
public class OciLanguageProperties {

    /**
     * When true, English texts are translated to Spanish via OCI Language.
     */
    private boolean enabled = false;

    private String compartmentId = "";

    private String configFile = "~/.oci/config";

    private String profile = "DEFAULT";
}
